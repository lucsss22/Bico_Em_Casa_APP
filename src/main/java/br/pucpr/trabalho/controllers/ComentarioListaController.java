package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.domains.Comentario;
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

import java.util.ArrayList;

public class ComentarioListaController {

    private Repositorio<Comentario> repositorio = new Repositorio<>("comentarios.dat");
    private TableView<Comentario> tabela = new TableView<>();
    private int contratacaoId;
    private String nomeServico;

    public ComentarioListaController(int contratacaoId, String nomeServico) {
        this.contratacaoId = contratacaoId;
        this.nomeServico = nomeServico;
    }

    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("Comentários");

        Label titulo = Estilo.titulo("Comentários - " + nomeServico);

        montarTabela();

        Button btnNovo = new Button("Novo comentário");
        btnNovo.setOnAction(e -> {
            new ComentarioFormController(repositorio, contratacaoId, nomeServico).mostrar();
            carregarTabela();
        });

        Button btnConsultar = new Button("Consultar selecionado");
        btnConsultar.setOnAction(e -> consultarSelecionado());

        Button btnFechar = new Button("Fechar");
        btnFechar.setOnAction(e -> stage.close());

        HBox botoes = new HBox(10, btnNovo, btnConsultar, btnFechar);

        VBox raiz = new VBox(10, titulo, tabela, botoes);
        raiz.setPadding(new Insets(20));
        VBox.setVgrow(tabela, Priority.ALWAYS);

        carregarTabela();

        stage.setScene(new Scene(raiz, 900, 480));
        stage.show();
    }

    private void montarTabela() {
        TableColumn<Comentario, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));

        TableColumn<Comentario, String> colAutor = new TableColumn<>("Chamar de");
        colAutor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAutor()));

        TableColumn<Comentario, String> colData = new TableColumn<>("Data desejada");
        colData.setCellValueFactory(c -> new SimpleStringProperty(Conversor.formatarData(c.getValue().getDataDesejada())));

        TableColumn<Comentario, String> colPeriodo = new TableColumn<>("Período");
        colPeriodo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPeriodo().toString()));

        TableColumn<Comentario, String> colUrgente = new TableColumn<>("Urgente");
        colUrgente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isUrgente() ? "Sim" : "Não"));

        TableColumn<Comentario, String> colTexto = new TableColumn<>("Comentário");
        colTexto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTexto()));
        colTexto.setPrefWidth(300);

        tabela.getColumns().add(colId);
        tabela.getColumns().add(colAutor);
        tabela.getColumns().add(colData);
        tabela.getColumns().add(colPeriodo);
        tabela.getColumns().add(colUrgente);
        tabela.getColumns().add(colTexto);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhum comentário cadastrado."));

        tabela.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                consultarSelecionado();
            }
        });
    }

    // Mostra só os comentários desta contratação
    private void carregarTabela() {
        try {
            ArrayList<Comentario> lista = new ArrayList<>();
            for (Comentario c : repositorio.listar()) {
                if (c.getContratacaoId() == contratacaoId) {
                    lista.add(c);
                }
            }
            tabela.setItems(FXCollections.observableArrayList(lista));
        } catch (PersistenciaException e) {
            Alertas.erro(e.getMessage());
        }
    }

    private void consultarSelecionado() {
        Comentario selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Alertas.erro("Selecione um comentário na tabela.");
            return;
        }
        new ComentarioFormController(repositorio, selecionado).mostrar();
        carregarTabela();
    }
}
