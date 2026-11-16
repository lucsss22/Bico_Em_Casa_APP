package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.ArquivoServico;
import br.pucpr.trabalho.domains.Servico;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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

//CRUD serviços
public class ServicoController {
    private Stage stage;
    private ArrayList<Servico> lista = ArquivoServico.lerLista(); // READ

    private ListView<Servico> listView = new ListView<>();
    private TextField txtTitulo = new TextField();
    private TextArea txtDescricao = new TextArea();
    private ImageView imgPreview = new ImageView();
    private String caminhoImagem;

    public ServicoController(Stage stage) {
        this.stage = stage;
    }

    public void mostrar() {
        listView.getItems().setAll(lista);
        listView.setPrefWidth(200);
        listView.getSelectionModel().selectedItemProperty().addListener((obs, antigo, item) -> {
            if (item != null) {
                txtTitulo.setText(item.getTitulo());
                txtDescricao.setText(item.getDescricao());
                caminhoImagem = item.getCaminhoImagem();
                mostrarImagem();
            }
        });

        txtTitulo.setPromptText("Nome do serviço");
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
                new Label("Serviço:"), txtTitulo,
                new Label("Descrição:"), txtDescricao,
                btnImagem, imgPreview,
                new HBox(10, btnSalvar, btnNovo, btnExcluir, btnVoltar));
        formulario.setPadding(new Insets(10));

        BorderPane layout = new BorderPane();
        layout.setLeft(listView);
        layout.setCenter(formulario);

        stage.setTitle("Serviços realizados");
        stage.setScene(new Scene(layout, 700, 550));
        stage.show();
    }

    private void escolherImagem() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens PNG", "*.png"));
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

    private void salvar() {
        if (txtTitulo.getText().isBlank() || caminhoImagem == null) {
            new Alert(Alert.AlertType.WARNING, "Informe o nome do serviço e escolha uma imagem.").showAndWait();
            return;
        }
        Servico item = listView.getSelectionModel().getSelectedItem();
        if (item == null) {
            item = new Servico(txtTitulo.getText(), txtDescricao.getText(), caminhoImagem);
            lista.add(item);
        } else {
            item.setTitulo(txtTitulo.getText());
            item.setDescricao(txtDescricao.getText());
            item.setCaminhoImagem(caminhoImagem);
        }
        ArquivoServico.salvarLista(lista);
        listView.getItems().setAll(lista);
        listView.getSelectionModel().select(item);
    }

    private void excluir() {
        Servico item = listView.getSelectionModel().getSelectedItem();
        if (item == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecione um serviço na lista.").showAndWait();
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION, "Excluir este serviço?", ButtonType.YES, ButtonType.NO);
        if (confirmacao.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                Files.deleteIfExists(Paths.get(item.getCaminhoImagem()));
            } catch (IOException e) {
                System.err.println("Não foi possível apagar a imagem: " + e.getMessage());
            }
            lista.remove(item);
            ArquivoServico.salvarLista(lista);
            listView.getItems().setAll(lista);
            limpar();
        }
    }

    /** Esvazia o formulário para cadastrar um item novo. */
    private void limpar() {
        listView.getSelectionModel().clearSelection();
        txtTitulo.clear();
        txtDescricao.clear();
        caminhoImagem = null;
        imgPreview.setImage(null);
    }
}
