package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.domains.Pagamento;
import br.pucpr.trabalho.domains.StatusPagamento;
import br.pucpr.trabalho.domains.TipoPerfil;
import br.pucpr.trabalho.domains.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PagamentoListaController {

    private Repositorio<Pagamento> repositorio = new Repositorio<>("pagamentos.dat");
    private TableView<Pagamento> tabela = new TableView<>();
    private int contratacaoId;
    private String nomeServico;
    private BigDecimal valorServico;
    private TipoPerfil perfil;
    private Usuario usuario;

    public PagamentoListaController(int contratacaoId, String nomeServico, BigDecimal valorServico,
                                    TipoPerfil perfil, Usuario usuario) {
        this.contratacaoId = contratacaoId;
        this.nomeServico = nomeServico;
        this.valorServico = valorServico;
        this.perfil = perfil;
        this.usuario = usuario;
    }

    public void mostrar() {
        Stage stage = new Stage();
        Label titulo;
        if (perfil == TipoPerfil.CLIENTE) {
            stage.setTitle("Pagamentos");
            titulo = Estilo.titulo("Pagamentos - " + nomeServico);
        } else {
            stage.setTitle("Recebimentos");
            titulo = Estilo.titulo("Recebimentos - " + nomeServico);
        }

        Label subtitulo = new Label("Valor do serviço: " + Conversor.formatarMoeda(valorServico)
                + "   |   Acesso: " + perfil
                + "   |   Usuário: " + PagamentoFormController.descreverUsuario(usuario.getId()));

        montarTabela();

        Button btnConsultar = new Button("Consultar selecionado");
        btnConsultar.setOnAction(e -> consultarSelecionado());

        Button btnFechar = new Button("Fechar");
        btnFechar.setOnAction(e -> stage.close());

        HBox botoes = new HBox(10);
        if (perfil == TipoPerfil.CLIENTE) {
            Button btnNovo = new Button("Realizar pagamento");
            btnNovo.setOnAction(e -> realizarPagamento());
            botoes.getChildren().add(btnNovo);
        }
        botoes.getChildren().add(btnConsultar);
        botoes.getChildren().add(btnFechar);

        VBox raiz = new VBox(12, titulo, subtitulo, tabela, botoes);
        raiz.setPadding(new Insets(20));
        VBox.setVgrow(tabela, Priority.ALWAYS);

        carregarTabela();

        stage.setScene(new Scene(raiz, 1000, 500));
        stage.show();
    }

    private void montarTabela() {
        TableColumn<Pagamento, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));

        TableColumn<Pagamento, String> colUsuario = new TableColumn<>("Usuário");
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(
                PagamentoFormController.descreverUsuario(c.getValue().getIdUsuario())));

        TableColumn<Pagamento, String> colPagador = new TableColumn<>("Pagador");
        colPagador.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomePagador()));

        TableColumn<Pagamento, String> colValor = new TableColumn<>("Valor");
        colValor.setCellValueFactory(c -> new SimpleStringProperty(Conversor.formatarMoeda(c.getValue().getValor())));

        TableColumn<Pagamento, String> colGorjeta = new TableColumn<>("Gorjeta");
        colGorjeta.setCellValueFactory(c -> new SimpleStringProperty(Conversor.formatarMoeda(c.getValue().getGorjeta())));

        TableColumn<Pagamento, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(Conversor.formatarMoeda(c.getValue().getValorTotal())));

        TableColumn<Pagamento, String> colForma = new TableColumn<>("Forma");
        colForma.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormaPagamento().toString()));

        TableColumn<Pagamento, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusPagamento().toString()));

        TableColumn<Pagamento, String> colPagoEm = new TableColumn<>("Pago em");
        colPagoEm.setCellValueFactory(c -> new SimpleStringProperty(Conversor.formatarData(c.getValue().getDataPagamento())));

        tabela.getColumns().add(colId);
        tabela.getColumns().add(colUsuario);
        tabela.getColumns().add(colPagador);
        tabela.getColumns().add(colValor);
        tabela.getColumns().add(colGorjeta);
        tabela.getColumns().add(colTotal);
        tabela.getColumns().add(colForma);
        tabela.getColumns().add(colStatus);
        tabela.getColumns().add(colPagoEm);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhum pagamento cadastrado."));

        tabela.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                consultarSelecionado();
            }
        });
    }

    // Um serviço só pode ter um pagamento ativo (só libera outro se o anterior foi estornado)
    private void realizarPagamento() {
        try {
            for (Pagamento p : repositorio.listar()) {
                if (p.getContratacaoId() == contratacaoId && p.getStatusPagamento() != StatusPagamento.ESTORNADO) {
                    Alertas.erro("Este serviço já tem um pagamento (" + p.getStatusPagamento() + ").\n"
                            + "Abra-o na tabela para consultar.");
                    return;
                }
            }
        } catch (PersistenciaException e) {
            Alertas.erro(e.getMessage());
            return;
        }
        new PagamentoFormController(repositorio, contratacaoId, nomeServico, valorServico, usuario).mostrar();
        carregarTabela();
    }

    // Mostra só os pagamentos desta contratação
    private void carregarTabela() {
        try {
            ArrayList<Pagamento> lista = new ArrayList<>();
            for (Pagamento p : repositorio.listar()) {
                if (p.getContratacaoId() == contratacaoId) {
                    lista.add(p);
                }
            }
            tabela.setItems(FXCollections.observableArrayList(lista));
        } catch (PersistenciaException e) {
            Alertas.erro(e.getMessage());
        }
    }

    private void consultarSelecionado() {
        Pagamento selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Alertas.erro("Selecione um pagamento na tabela.");
            return;
        }
        new PagamentoFormController(repositorio, selecionado, perfil).mostrar();
        carregarTabela();
    }
}
