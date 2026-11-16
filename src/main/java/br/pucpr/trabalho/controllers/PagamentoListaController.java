// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.domains.Pagamento;
import br.pucpr.trabalho.domains.StatusPagamento;
import br.pucpr.trabalho.domains.TipoPerfil;
import br.pucpr.trabalho.domains.Usuario;
import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.controllers.Conversor;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela de pagamentos de um serviço contratado: mostra o histórico em uma tabela
 * e permite realizar um novo pagamento ou abrir um existente.
 *
 * Quem abre esta tela (a tela de Contratação) informa o id da contratação,
 * o nome do serviço e o preço do serviço.
 *
 * Regra: um serviço só pode ter um pagamento ativo (pendente, aguardando ou pago).
 * Um novo pagamento só é liberado se não houver nenhum, ou se o anterior foi estornado.
 *
 * Perfil:
 * - CLIENTE vê "Pagamentos" e pode realizar pagamento.
 * - PROFISSIONAL vê "Recebimentos" e não pode realizar pagamento; ao abrir um
 *   pagamento, tem as ações de confirmar recebimento e estornar.
 */
public class PagamentoListaController {

    private final Repositorio<Pagamento> repositorio = new Repositorio<>("pagamentos.dat");
    private final TableView<Pagamento> tabela = new TableView<>();
    private final int contratacaoId;
    private final String nomeServico;
    private final BigDecimal valorServico;
    private final TipoPerfil perfil;
    private final Usuario usuario;        // usuário cadastrado que está usando a tela

    public PagamentoListaController(int contratacaoId, String nomeServico, BigDecimal valorServico,
                                    TipoPerfil perfil, Usuario usuario) {
        this.contratacaoId = contratacaoId;
        this.nomeServico = nomeServico;
        this.valorServico = valorServico;
        this.perfil = perfil;
        this.usuario = usuario;
    }

    public void mostrar() {
        boolean cliente = perfil == TipoPerfil.CLIENTE;

        Stage stage = new Stage();
        stage.setTitle(cliente ? "Pagamentos" : "Recebimentos");

        Label titulo = new Label((cliente ? "Pagamentos – " : "Recebimentos – ") + nomeServico);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label subtitulo = new Label("Valor do serviço: " + Conversor.formatarMoeda(valorServico)
                + "   •   Acesso: " + perfil + "   •   Usuário: " + PagamentoFormController.descreverUsuario(usuario.getId()));

        montarTabela();

        Button btnConsultar = new Button("Consultar selecionado");
        btnConsultar.setOnAction(e -> consultarSelecionado());

        Button btnFechar = new Button("Fechar");
        btnFechar.setOnAction(e -> stage.close());

        HBox botoes = new HBox(10, btnConsultar, btnFechar);
        if (cliente) {
            Button btnNovo = new Button("Realizar pagamento");
            btnNovo.setOnAction(e -> realizarPagamento());
            botoes.getChildren().add(0, btnNovo);
        }

        Label dica = new Label("Dica: dê um duplo clique em uma linha para abrir o pagamento.");

        VBox raiz = new VBox(12, new VBox(2, titulo, subtitulo), tabela, dica, botoes);
        raiz.setPadding(new Insets(20));
        VBox.setVgrow(tabela, Priority.ALWAYS);

        carregarTabela();

        stage.setScene(new Scene(raiz, 1120, 500));
        stage.show();
    }

    private void montarTabela() {
        TableColumn<Pagamento, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colId.setPrefWidth(50);


        TableColumn<Pagamento, String> colPagador = new TableColumn<>("Pagador");
        colPagador.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomePagador()));
        colPagador.setPrefWidth(140);

        TableColumn<Pagamento, String> colUsuario = new TableColumn<>("Usuário");
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(PagamentoFormController.descreverUsuario(c.getValue().getIdUsuario())));

        TableColumn<Pagamento, String> colValor = new TableColumn<>("Valor");
        colValor.setCellValueFactory(c -> new SimpleStringProperty(Conversor.formatarMoeda(c.getValue().getValor())));

        TableColumn<Pagamento, String> colGorjeta = new TableColumn<>("Gorjeta");
        colGorjeta.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getGorjeta().signum() == 0 ? "—" : Conversor.formatarMoeda(c.getValue().getGorjeta())));

        TableColumn<Pagamento, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(Conversor.formatarMoeda(c.getValue().getValorTotal())));

        TableColumn<Pagamento, String> colForma = new TableColumn<>("Forma");
        colForma.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormaPagamento().toString()));
        colForma.setPrefWidth(80);

        TableColumn<Pagamento, String> colDetalhes = new TableColumn<>("Detalhes");
        colDetalhes.setCellValueFactory(c -> new SimpleStringProperty(descreverDetalhes(c.getValue())));
        colDetalhes.setPrefWidth(230);

        TableColumn<Pagamento, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusPagamento().toString()));
        colStatus.setPrefWidth(190);
        // Célula personalizada: pinta o texto com a cor do status
        colStatus.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(String texto, boolean vazia) {
                super.updateItem(texto, vazia);
                Pagamento p = (vazia || getTableRow() == null) ? null : getTableRow().getItem();
                setText(p == null ? null : texto);
                Estilo.corDoStatus(this, p == null ? null : p.getStatusPagamento());
            }
        });

        TableColumn<Pagamento, String> colPagoEm = new TableColumn<>("Pago em");
        colPagoEm.setCellValueFactory(c -> new SimpleStringProperty(Conversor.formatarData(c.getValue().getDataPagamento())));

        tabela.getColumns().addAll(List.of(colId, colUsuario, colPagador, colValor, colGorjeta, colTotal, colForma, colDetalhes, colStatus, colPagoEm));
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhum pagamento cadastrado."));

        // Clique duplo na linha também abre a consulta
        tabela.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                consultarSelecionado();
            }
        });
    }

    /** Abre a tela de pagamento, se o serviço ainda não tiver um pagamento ativo. */
    private void realizarPagamento() {
        try {
            for (Pagamento p : repositorio.listar()) {
                if (p.getContratacaoId() == contratacaoId && p.getStatusPagamento() != StatusPagamento.ESTORNADO) {
                    Alertas.erro("Este serviço já tem um pagamento " + p.getStatusPagamento().toString().toLowerCase()
                            + " (#" + p.getId() + ").\nAbra-o na tabela para consultar.");
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

    /** Texto da coluna Detalhes, conforme a forma de pagamento. */
    private String descreverDetalhes(Pagamento p) {
        return switch (p.getFormaPagamento()) {
            case PIX -> p.getCodigoTransacao() == null ? "Aguardando pagamento" : "Código " + p.getCodigoTransacao();
            case DINHEIRO -> {
                if (p.getValorPago() == null) {
                    yield "Aguardando pagamento";
                }
                String texto = "Pago " + Conversor.formatarMoeda(p.getValorPago());
                if (p.getValorExcedente().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    texto += " (+" + Conversor.formatarMoeda(p.getValorExcedente()) + ")";
                }
                yield texto;
            }
        };
    }

    private void carregarTabela() {
        try {
            // Mostra apenas os pagamentos desta contratação
            List<Pagamento> daContratacao = new ArrayList<>();
            for (Pagamento p : repositorio.listar()) {
                if (p.getContratacaoId() == contratacaoId) {
                    daContratacao.add(p);
                }
            }
            tabela.setItems(FXCollections.observableArrayList(daContratacao));
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
