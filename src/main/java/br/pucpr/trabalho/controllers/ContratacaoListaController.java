// Autor: Carlos Alberto
package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.ArquivosUsuario;
import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.domains.Contratacao;
import br.pucpr.trabalho.domains.Servico;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContratacaoListaController {

    private final Repositorio<Contratacao> repositorio = new Repositorio<>("contratacoes.dat");
    private final Repositorio<Servico> repositorioServicos = new Repositorio<>("servicos.dat");
    private final TableView<Contratacao> tabela = new TableView<>();

    private final Map<Integer, String> titulosServicos = new HashMap<>();
    private final Map<Integer, String> nomesClientes = new HashMap<>();

    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("Contratações");

        Label titulo = Estilo.titulo("Contratações cadastradas");

        montarTabela();

        Button btnNova = new Button("Nova contratação");
        btnNova.setOnAction(e -> {
            new ContratacaoFormController(repositorio).mostrar();
            carregarTabela();
        });

        Button btnConsultar = new Button("Consultar selecionada");
        btnConsultar.setOnAction(e -> consultarSelecionada());

        Button btnFechar = new Button("Fechar");
        btnFechar.setOnAction(e -> stage.close());

        HBox botoes = new HBox(10, btnNova, btnConsultar, btnFechar);

        VBox raiz = new VBox(10, titulo, tabela, botoes);
        raiz.setPadding(new Insets(20));
        VBox.setVgrow(tabela, Priority.ALWAYS);

        carregarTabela();

        stage.setScene(new Scene(raiz, 900, 450));
        stage.show();
    }

    private void montarTabela() {
        TableColumn<Contratacao, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colId.setPrefWidth(50);

        TableColumn<Contratacao, String> colServico = new TableColumn<>("Serviço");
        colServico.setCellValueFactory(c -> new SimpleStringProperty(tituloDoServico(c.getValue().getIdServico())));
        colServico.setPrefWidth(200);

        TableColumn<Contratacao, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(nomeDoCliente(c.getValue().getIdCliente())));
        colCliente.setPrefWidth(150);

        TableColumn<Contratacao, String> colData = new TableColumn<>("Data");
        colData.setCellValueFactory(c -> new SimpleStringProperty(Conversor.formatarData(c.getValue().getDataServico())));
        colData.setPrefWidth(100);

        TableColumn<Contratacao, String> colEndereco = new TableColumn<>("Endereço");
        colEndereco.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEndereco()));
        colEndereco.setPrefWidth(250);

        TableColumn<Contratacao, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        tabela.getColumns().addAll(List.of(colId, colServico, colCliente, colData, colEndereco, colStatus));
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhuma contratação cadastrada."));

        tabela.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                consultarSelecionada();
            }
        });
    }

    private void carregarTabela() {
        nomesClientes.clear();
        for (Usuario u : ArquivosUsuario.lerLista()) {
            nomesClientes.put(u.getId(), u.getNome());
        }

        try {
            titulosServicos.clear();
            for (Servico s : repositorioServicos.listar()) {
                titulosServicos.put(s.getId(), s.getTitulo());
            }
            tabela.setItems(FXCollections.observableArrayList(repositorio.listar()));
        } catch (PersistenciaException e) {
            Alertas.erro(e.getMessage());
        }
    }

    private String tituloDoServico(int idServico) {
        return titulosServicos.getOrDefault(idServico, "(serviço removido)");
    }

    private String nomeDoCliente(int idCliente) {
        return nomesClientes.getOrDefault(idCliente, "(cliente " + idCliente + " não encontrado)");
    }

    private void consultarSelecionada() {
        Contratacao selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            Alertas.erro("Selecione uma contratação na tabela.");
            return;
        }
        new ContratacaoFormController(repositorio, selecionada).mostrar();
        carregarTabela();
    }
}
