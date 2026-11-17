package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.ArquivoPortfolio;
import br.pucpr.trabalho.domains.ItemPortfolio;
import br.pucpr.trabalho.domains.ValidacaoException;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

//CRUD portfólio
public class PortfolioController {
    private Stage stage;
    private ArrayList<ItemPortfolio> lista = ArquivoPortfolio.lerLista(); // READ

    private TableView<ItemPortfolio> tabela = new TableView<>();
    private TextField txtTitulo = new TextField();
    private TextArea txtDescricao = new TextArea();
    private ImageView imgPreview = new ImageView();
    private String caminhoImagem;

    public PortfolioController(Stage stage) {
        this.stage = stage;
    }

    public void mostrar() {
        montarTabela();
        tabela.getItems().setAll(lista);
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, item) -> {
            if (item != null) {
                txtTitulo.setText(item.getTitulo());
                txtDescricao.setText(item.getDescricao());
                caminhoImagem = item.getCaminhoImagem();
                mostrarImagem();
            }
        });

        txtTitulo.setPromptText("Título");
        txtDescricao.setPromptText("Descrição");
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
                new Label("Título:"), txtTitulo,
                new Label("Descrição:"), txtDescricao,
                btnImagem, imgPreview,
                new HBox(10, btnSalvar, btnNovo, btnExcluir, btnVoltar));
        formulario.setPadding(new Insets(10));

        BorderPane layout = new BorderPane();
        layout.setLeft(tabela);
        layout.setCenter(formulario);

        stage.setTitle("Portfólio");
        stage.setScene(new Scene(layout, 850, 550));
        stage.show();
    }

    private void montarTabela() {
        TableColumn<ItemPortfolio, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitulo()));
        colTitulo.setPrefWidth(140);

        TableColumn<ItemPortfolio, String> colDescricao = new TableColumn<>("Descrição");
        colDescricao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescricao()));
        colDescricao.setPrefWidth(190);

        tabela.getColumns().add(colTitulo);
        tabela.getColumns().add(colDescricao);
        tabela.setPrefWidth(340);
        tabela.setPlaceholder(new Label("Nenhum item no portfólio."));
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

    /** CREATE (nada selecionado) ou UPDATE (item selecionado) */
    private void salvar() {
        ItemPortfolio novo;
        try {
            novo = new ItemPortfolio(txtTitulo.getText(), txtDescricao.getText(), caminhoImagem);
        } catch (ValidacaoException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
            return;
        }

        ItemPortfolio selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            lista.add(novo);
        } else {
            lista.set(lista.indexOf(selecionado), novo);
        }

        if (!ArquivoPortfolio.salvarLista(lista)) {
            new Alert(Alert.AlertType.ERROR, "Erro ao gravar o arquivo do portfólio.").showAndWait();
            lista = ArquivoPortfolio.lerLista();
        }
        tabela.getItems().setAll(lista);
        tabela.getSelectionModel().select(novo);
    }

    private void excluir() {
        ItemPortfolio item = tabela.getSelectionModel().getSelectedItem();
        if (item == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecione um item na tabela.").showAndWait();
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION, "Excluir este item?", ButtonType.YES, ButtonType.NO);
        if (confirmacao.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            lista.remove(item);
            if (!ArquivoPortfolio.salvarLista(lista)) {
                new Alert(Alert.AlertType.ERROR, "Erro ao gravar o arquivo do portfólio.").showAndWait();
                lista = ArquivoPortfolio.lerLista();
                tabela.getItems().setAll(lista);
                return;
            }
            try {
                if (item.getCaminhoImagem() != null) {
                    Files.deleteIfExists(Paths.get(item.getCaminhoImagem()));
                }
            } catch (IOException e) {
                System.err.println("Não foi possível apagar a imagem: " + e.getMessage());
            }
            tabela.getItems().setAll(lista);
            limpar();
        }
    }

    /** Esvazia o formulário para cadastrar um item novo. */
    private void limpar() {
        tabela.getSelectionModel().clearSelection();
        txtTitulo.clear();
        txtDescricao.clear();
        caminhoImagem = null;
        imgPreview.setImage(null);
    }
}
