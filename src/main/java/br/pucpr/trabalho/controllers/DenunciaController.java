package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.RepositorioDenuncias;
import br.pucpr.trabalho.domains.Denuncia;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DenunciaController {

    private final RepositorioDenuncias repositorio = new RepositorioDenuncias();
    private final TableView<Denuncia> tabela = new TableView<>();
    private final TextField campoAlvo = new TextField();
    private final ComboBox<String> campoMotivo = new ComboBox<>();
    private final TextArea campoDescricao = new TextArea();

    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("Denúncias");

        Label titulo = new Label("Denúncias");
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
                        campoAlvo.setText(selecionada.getAlvo());
                        campoMotivo.setValue(selecionada.getMotivo());
                        campoDescricao.setText(selecionada.getDescricao());
                        botaoSalvar.setText("Salvar edição");
                        botaoExcluir.setDisable(false);
                    }
                });

        HBox botoes = new HBox(10, botaoSalvar, botaoExcluir, botaoLimpar);
        VBox conteudo = new VBox(10, titulo, campoAlvo, campoMotivo, campoDescricao, tabela, botoes);
        conteudo.setPadding(new Insets(24));
        VBox.setVgrow(tabela, Priority.ALWAYS);
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.setScene(new Scene(conteudo, 760, 580));
        try {
            carregarTabela();
        } catch (IOException e) {
            Alertas.erro("Não foi possível carregar as denúncias: " + e.getMessage());
        }
        stage.show();
    }

    private void configurarCampos() {
        campoAlvo.setPromptText("Alvo (ex.: perfil, anúncio ou contratação)");
        campoAlvo.setMaxWidth(Double.MAX_VALUE);
        campoMotivo.getItems().setAll("Conteúdo impróprio", "Golpe ou fraude",
                "Assédio ou comportamento abusivo", "Outro");
        campoMotivo.setPromptText("Selecione um motivo");
        campoMotivo.setMaxWidth(Double.MAX_VALUE);
        campoDescricao.setPromptText("Descreva o ocorrido (mínimo de 20 caracteres)");
        campoDescricao.setWrapText(true);
        campoDescricao.setPrefRowCount(3);
    }

    private void configurarTabela() {
        TableColumn<Denuncia, String> colunaId = new TableColumn<>("ID");
        colunaId.setCellValueFactory(c -> new SimpleStringProperty(
                Integer.toString(c.getValue().getId())));
        TableColumn<Denuncia, String> colunaAlvo = new TableColumn<>("Alvo");
        colunaAlvo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAlvo()));
        TableColumn<Denuncia, String> colunaMotivo = new TableColumn<>("Motivo");
        colunaMotivo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMotivo()));
        TableColumn<Denuncia, String> colunaDescricao = new TableColumn<>("Descrição");
        colunaDescricao.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDescricao()));
        tabela.getColumns().addAll(colunaId, colunaAlvo, colunaMotivo, colunaDescricao);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhuma denúncia cadastrada."));
    }

    private void salvar() {
        try {
            Denuncia denuncia = new Denuncia(
                    campoAlvo.getText(), campoMotivo.getValue(), campoDescricao.getText());
            Denuncia selecionada = tabela.getSelectionModel().getSelectedItem();
            List<Denuncia> atualizadas = new ArrayList<>(tabela.getItems());
            if (selecionada == null) {
                denuncia.setId(proximoId(atualizadas));
                atualizadas.add(denuncia);
            } else {
                denuncia.setId(selecionada.getId());
                atualizadas.set(encontrarIndice(selecionada.getId(), atualizadas), denuncia);
            }
            repositorio.salvar(atualizadas);
            carregarTabela();
            tabela.getSelectionModel().clearSelection();
            Alertas.info("Denúncia salva com sucesso!");
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (IOException e) {
            Alertas.erro("Erro ao salvar denúncia: " + e.getMessage());
        }
    }

    private void excluir() {
        Denuncia selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            Alertas.erro("Selecione uma denúncia na tabela.");
            return;
        }
        if (!Alertas.confirmar("Deseja realmente excluir a denúncia #" + selecionada.getId() + "?")) {
            return;
        }
        try {
            List<Denuncia> atualizadas = new ArrayList<>(tabela.getItems());
            atualizadas.removeIf(denuncia -> denuncia.getId() == selecionada.getId());
            repositorio.salvar(atualizadas);
            carregarTabela();
            limparCampos();
            Alertas.info("Denúncia excluída.");
        } catch (IOException e) {
            Alertas.erro("Erro ao excluir ou atualizar a lista de denúncias: " + e.getMessage());
        }
    }

    private void carregarTabela() throws IOException {
        List<Denuncia> denuncias = repositorio.listar();
        tabela.getItems().setAll(denuncias);
    }

    private int proximoId(List<Denuncia> denuncias) {
        return denuncias.stream().mapToInt(Denuncia::getId).max().orElse(0) + 1;
    }

    private int encontrarIndice(int id, List<Denuncia> denuncias) {
        for (int indice = 0; indice < denuncias.size(); indice++) {
            if (denuncias.get(indice).getId() == id) {
                return indice;
            }
        }
        throw new IllegalArgumentException("A denúncia selecionada não está mais na lista.");
    }

    private void limparCampos() {
        campoAlvo.clear();
        campoMotivo.getSelectionModel().clearSelection();
        campoDescricao.clear();
    }
}
