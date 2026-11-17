package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.domains.Servico;
import br.pucpr.trabalho.domains.ValidacaoException;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

//CRUD da galeria de serviços realizados (foto + descrição de um serviço cadastrado em "Serviços")
public class ServicoController {
    private Stage stage;
    private Repositorio<Servico> repositorio = new Repositorio<>("servicos.dat");

    private TableView<Servico> tabela = new TableView<>();
    private ComboBox<Servico> cbServico = new ComboBox<>();
    private TextArea txtDescricao = new TextArea();
    private ImageView imgPreview = new ImageView();
    private String caminhoImagem;

    public ServicoController(Stage stage) {
        this.stage = stage;
    }

    public void mostrar() {
        montarTabela();
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, item) -> {
            if (item != null) {
                cbServico.getItems().setAll(item);
                cbServico.setValue(item);
                cbServico.setDisable(true);
                txtDescricao.setText(item.getDescricao());
                caminhoImagem = item.getCaminhoImagem();
                mostrarImagem();
            }
        });

        cbServico.setPromptText("Escolha o serviço");
        cbServico.setConverter(new StringConverter<>() {
            @Override
            public String toString(Servico s) {
                return s == null ? "" : s.getTitulo();
            }

            @Override
            public Servico fromString(String texto) {
                return null;
            }
        });
        txtDescricao.setPromptText("Descrição do serviço realizado");
        txtDescricao.setPrefRowCount(3);
        imgPreview.setFitWidth(300);
        imgPreview.setPreserveRatio(true);

        Button btnImagem = new Button("Escolher imagem");
        btnImagem.setOnAction(e -> escolherImagem());
        Button btnSalvar = new Button("Salvar");
        btnSalvar.setOnAction(e -> salvar());
        Button btnNovo = new Button("Novo");
        btnNovo.setOnAction(e -> limpar());
        Button btnExcluir = new Button("Excluir");
        btnExcluir.setOnAction(e -> excluir());
        Button btnVoltar = new Button("Voltar");
        btnVoltar.setOnAction(e -> new MainController(stage).mostrar());

        VBox formulario = new VBox(10,
                new Label("Serviço:"), cbServico,
                new Label("Descrição:"), txtDescricao,
                btnImagem, imgPreview,
                new HBox(10, btnSalvar, btnNovo, btnExcluir, btnVoltar));
        formulario.setPadding(new Insets(10));

        BorderPane layout = new BorderPane();
        layout.setLeft(tabela);
        layout.setCenter(formulario);

        carregar();
        limpar();

        stage.setTitle("Serviços realizados");
        stage.setScene(new Scene(layout, 850, 550));
        stage.show();
    }

    private void montarTabela() {
        TableColumn<Servico, String> colServico = new TableColumn<>("Serviço");
        colServico.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitulo()));
        colServico.setPrefWidth(140);

        TableColumn<Servico, String> colDescricao = new TableColumn<>("Descrição");
        colDescricao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescricao()));
        colDescricao.setPrefWidth(190);

        tabela.getColumns().add(colServico);
        tabela.getColumns().add(colDescricao);
        tabela.setPrefWidth(340);
        tabela.setPlaceholder(new Label("Nenhum serviço realizado na galeria."));
    }

    /** READ: a tabela mostra os serviços que têm foto; o combo, os que ainda não têm. */
    private void carregar() {
        try {
            ArrayList<Servico> naGaleria = new ArrayList<>();
            ArrayList<Servico> semFoto = new ArrayList<>();
            for (Servico s : repositorio.listar()) {
                if (s.getCaminhoImagem() != null) {
                    naGaleria.add(s);
                } else {
                    semFoto.add(s);
                }
            }
            tabela.getItems().setAll(naGaleria);
            cbServico.getItems().setAll(semFoto);
        } catch (PersistenciaException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    private void escolherImagem() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens (PNG, JPG)", "*.png", "*.jpg", "*.jpeg"));
        File arquivo = chooser.showOpenDialog(stage);
        if (arquivo == null) {
            return; // usuário cancelou
        }
        try {
            Files.createDirectories(Paths.get("imagens"));
            Path destino = Paths.get("imagens", System.currentTimeMillis() + "_" + arquivo.getName());
            Files.copy(arquivo.toPath(), destino);
            caminhoImagem = destino.toString();
            mostrarImagem();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erro ao copiar a imagem: " + e.getMessage()).showAndWait();
        }
    }

    private void mostrarImagem() {
        if (caminhoImagem == null) {
            imgPreview.setImage(null);
        } else {
            imgPreview.setImage(new Image(new File(caminhoImagem).toURI().toString(), 300, 0, true, true));
        }
    }

    /** CREATE (serviço escolhido no combo) ou UPDATE (serviço selecionado na tabela) */
    private void salvar() {
        Servico escolhido = cbServico.getValue();
        if (escolhido == null) {
            new Alert(Alert.AlertType.WARNING, "Escolha o serviço realizado.").showAndWait();
            return;
        }
        if (caminhoImagem == null) {
            new Alert(Alert.AlertType.WARNING, "Escolha uma imagem do serviço realizado.").showAndWait();
            return;
        }
        try {
            Servico servico = repositorio.buscarPorId(escolhido.getId());
            servico.setDescricao(txtDescricao.getText());
            servico.setCaminhoImagem(caminhoImagem);
            repositorio.atualizar(servico);
            carregar();
            limpar();
        } catch (ValidacaoException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        } catch (PersistenciaException e) {
            new Alert(Alert.AlertType.ERROR, "Erro ao salvar: " + e.getMessage()).showAndWait();
        }
    }

    /** DELETE: tira o serviço da galeria (o serviço continua cadastrado em "Serviços") */
    private void excluir() {
        Servico item = tabela.getSelectionModel().getSelectedItem();
        if (item == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecione um serviço na tabela.").showAndWait();
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION, "Excluir este serviço da galeria?", ButtonType.YES, ButtonType.NO);
        if (confirmacao.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }
        try {
            Servico servico = repositorio.buscarPorId(item.getId());
            String imagemAntiga = servico.getCaminhoImagem();
            servico.setDescricao("");
            servico.setCaminhoImagem(null);
            repositorio.atualizar(servico);
            if (imagemAntiga != null) {
                Files.deleteIfExists(Paths.get(imagemAntiga));
            }
        } catch (ValidacaoException | PersistenciaException e) {
            new Alert(Alert.AlertType.ERROR, "Erro ao excluir: " + e.getMessage()).showAndWait();
        } catch (IOException e) {
            System.err.println("Não foi possível apagar a imagem: " + e.getMessage());
        }
        carregar();
        limpar();
    }

    /** Esvazia o formulário para cadastrar um item novo. */
    private void limpar() {
        tabela.getSelectionModel().clearSelection();
        cbServico.setDisable(false);
        carregarComboSemFoto();
        cbServico.setValue(null);
        txtDescricao.clear();
        caminhoImagem = null;
        imgPreview.setImage(null);
    }

    private void carregarComboSemFoto() {
        try {
            ArrayList<Servico> semFoto = new ArrayList<>();
            for (Servico s : repositorio.listar()) {
                if (s.getCaminhoImagem() == null) {
                    semFoto.add(s);
                }
            }
            cbServico.getItems().setAll(semFoto);
        } catch (PersistenciaException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }
}
