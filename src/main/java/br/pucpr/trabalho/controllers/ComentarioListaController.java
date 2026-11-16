// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.domains.Comentario;
import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.controllers.Conversor;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Tela de consulta: mostra em uma tabela os comentários de uma contratação.
 * A partir dela o usuário cadastra um novo ou abre um existente
 * para alterar ou excluir.
 *
 * Quem abre esta tela (a tela de Contratação) informa o id da contratação
 * e o nome do serviço contratado.
 */
public class ComentarioListaController {

    private final Repositorio<Comentario> repositorio = new Repositorio<>("comentarios.dat");
    private final TableView<Comentario> tabela = new TableView<>();
    private final int contratacaoId;
    private final String nomeServico;

    public ComentarioListaController(int contratacaoId, String nomeServico) {
        this.contratacaoId = contratacaoId;
        this.nomeServico = nomeServico;
    }

    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("Comentários");

        Label titulo = new Label("Comentários – " + nomeServico);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

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
        colId.setPrefWidth(50);


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

        tabela.getColumns().addAll(List.of(colId, colAutor, colData, colPeriodo, colUrgente, colTexto));
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhum comentário cadastrado."));

        // Clique duplo na linha também abre a consulta
        tabela.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                consultarSelecionado();
            }
        });
    }

    private void carregarTabela() {
        try {
            // Mostra apenas os comentários desta contratação
            List<Comentario> daContratacao = new ArrayList<>();
            for (Comentario c : repositorio.listar()) {
                if (c.getContratacaoId() == contratacaoId) {
                    daContratacao.add(c);
                }
            }
            tabela.setItems(FXCollections.observableArrayList(daContratacao));
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
