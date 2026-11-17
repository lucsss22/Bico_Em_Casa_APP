package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.RepositorioAvaliacao;
import br.pucpr.trabalho.domains.Avaliacao;
import br.pucpr.trabalho.domains.ValidacaoException;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AvaliacaoController {

    private final RepositorioAvaliacao repositorio = new RepositorioAvaliacao();
    private final TableView<Avaliacao> tabela = new TableView<>();
    private final ComboBox<String> campoProfissional = new ComboBox<>();
    private final ComboBox<Integer> campoNota = new ComboBox<>();
    private final TextArea campoComentario = new TextArea();

    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("Avaliações");

        Label titulo = new Label("Avaliações");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        configurarCampos();
        configurarTabela();

        Button botaoSalvar = new Button("Cadastrar");
        Button botaoExcluir = new Button("Excluir");
        botaoExcluir.setDisable(true);
        Button botaoLimpar = new Button("Limpar seleção");
        botaoSalvar.setOnAction(evento -> salvar());
        botaoExcluir.setOnAction(evento -> excluir());
        botaoLimpar.setOnAction(evento -> {
            tabela.getSelectionModel().clearSelection();
            limparCampos();
        });
        tabela.getSelectionModel().selectedItemProperty().addListener(
                (observavel, anterior, selecionada) -> {
                    if (selecionada == null) {
                        limparCampos();
                        botaoSalvar.setText("Cadastrar");
                        botaoExcluir.setDisable(true);
                    } else {
                        campoProfissional.setValue(selecionada.getProfissionalAvaliado());
                        campoNota.setValue(selecionada.getNota());
                        campoComentario.setText(selecionada.getComentario());
                        botaoSalvar.setText("Salvar edição");
                        botaoExcluir.setDisable(false);
                    }
                });

        HBox botoes = new HBox(10, botaoSalvar, botaoExcluir, botaoLimpar);
        VBox conteudo = new VBox(10, titulo, campoProfissional, campoNota, campoComentario,
                tabela, botoes);
        conteudo.setPadding(new Insets(24));
        VBox.setVgrow(tabela, Priority.ALWAYS);
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.setScene(new Scene(conteudo, 760, 580));
        try {
            carregarTabela();
        } catch (IOException e) {
            Alertas.erro("Não foi possível carregar as avaliações: " + e.getMessage());
        }
        stage.show();
    }

    private void configurarCampos() {
        campoProfissional.getItems().setAll("Eletricista", "Encanador", "Diarista", "Pintor",
                "Montador", "Pedreiro", "Reparos gerais");
        campoProfissional.setPromptText("Selecione o profissional avaliado");
        campoProfissional.setMaxWidth(Double.MAX_VALUE);
        campoNota.getItems().setAll(1, 2, 3, 4, 5);
        campoNota.setPromptText("Selecione uma nota de 1 a 5");
        campoNota.setMaxWidth(Double.MAX_VALUE);
        campoComentario.setPromptText("Comentário opcional (máximo de 1000 caracteres)");
        campoComentario.setWrapText(true);
        campoComentario.setPrefRowCount(3);
    }

    private void configurarTabela() {
        TableColumn<Avaliacao, String> colunaId = new TableColumn<>("ID");
        colunaId.setCellValueFactory(c -> new SimpleStringProperty(
                Integer.toString(c.getValue().getId())));
        TableColumn<Avaliacao, String> colunaProfissional = new TableColumn<>("Profissional");
        colunaProfissional.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getProfissionalAvaliado()));
        TableColumn<Avaliacao, String> colunaNota = new TableColumn<>("Nota");
        colunaNota.setCellValueFactory(c -> new SimpleStringProperty(
                Integer.toString(c.getValue().getNota())));
        TableColumn<Avaliacao, String> colunaComentario = new TableColumn<>("Comentário");
        colunaComentario.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getComentario()));
        tabela.getColumns().addAll(colunaId, colunaProfissional, colunaNota, colunaComentario);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhuma avaliação cadastrada."));
    }

    private void salvar() {
        try {
            Avaliacao avaliacao = new Avaliacao(
                    campoProfissional.getValue(),
                    campoNota.getValue() == null ? 0 : campoNota.getValue(),
                    campoComentario.getText());
            Avaliacao selecionada = tabela.getSelectionModel().getSelectedItem();
            List<Avaliacao> atualizadas = new ArrayList<>(tabela.getItems());
            if (selecionada == null) {
                avaliacao.setId(proximoId(atualizadas));
                atualizadas.add(avaliacao);
            } else {
                avaliacao.setId(selecionada.getId());
                atualizadas.set(encontrarIndice(selecionada.getId(), atualizadas), avaliacao);
            }
            repositorio.salvar(atualizadas);
            carregarTabela();
            tabela.getSelectionModel().clearSelection();
            Alertas.info("Avaliação salva com sucesso!");
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (IOException e) {
            Alertas.erro("Erro ao salvar avaliação: " + e.getMessage());
        }
    }

    private void excluir() {
        Avaliacao selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            Alertas.erro("Selecione uma avaliação na tabela.");
            return;
        }
        if (!Alertas.confirmar("Deseja realmente excluir a avaliação #" + selecionada.getId() + "?")) {
            return;
        }
        try {
            List<Avaliacao> atualizadas = new ArrayList<>(tabela.getItems());
            atualizadas.removeIf(avaliacao -> avaliacao.getId() == selecionada.getId());
            repositorio.salvar(atualizadas);
            carregarTabela();
            limparCampos();
            Alertas.info("Avaliação excluída.");
        } catch (IOException e) {
            Alertas.erro("Erro ao excluir ou atualizar a lista de avaliações: " + e.getMessage());
        }
    }

    private void carregarTabela() throws IOException {
        List<Avaliacao> avaliacoes = repositorio.listar();
        tabela.getItems().setAll(avaliacoes);
    }

    private int proximoId(List<Avaliacao> avaliacoes) {
        return avaliacoes.stream().mapToInt(Avaliacao::getId).max().orElse(0) + 1;
    }

    private int encontrarIndice(int id, List<Avaliacao> avaliacoes) {
        for (int indice = 0; indice < avaliacoes.size(); indice++) {
            if (avaliacoes.get(indice).getId() == id) {
                return indice;
            }
        }
        throw new IllegalArgumentException("A avaliação selecionada não está mais na lista.");
    }

    private void limparCampos() {
        campoProfissional.getSelectionModel().clearSelection();
        campoNota.getSelectionModel().clearSelection();
        campoComentario.clear();
    }
}
