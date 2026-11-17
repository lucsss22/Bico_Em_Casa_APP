package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.ArquivoPerfil;
import br.pucpr.trabalho.arquivos.ArquivosUsuario;
import br.pucpr.trabalho.domains.Perfil;
import br.pucpr.trabalho.domains.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Controller da tela de CONSULTA de perfis:
 * mostra a tabela e permite alterar (janela modal) e excluir.
 */
public class PerfilConsultaController {

    private Stage stage;
    private MainController mainController;
    private Scene cena;

    private TableView<Perfil> tabela;
    private ObservableList<Perfil> dados;
    private ArrayList<Usuario> usuarios; // usada para mostrar o nome do usuário na tabela
    private Label lblMensagem;

    public PerfilConsultaController(Stage stage, MainController mainController) {
        this.stage = stage;
        this.mainController = mainController;
    }

    public void mostrar() {
        criarUI();
        carregarTabela();
        stage.setTitle("Consultar Perfis");
        stage.setScene(cena);
        stage.show();
    }

    private void criarUI() {
        Label lblTitulo = new Label("Consultar Perfis");
        lblTitulo.setFont(new Font("Arial", 20));

        // ----- tabela -----
        tabela = new TableView<>();
        dados = FXCollections.observableArrayList();

        TableColumn<Perfil, String> colId = new TableColumn<>("Id");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));

        TableColumn<Perfil, String> colUsuario = new TableColumn<>("Usuário");
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(nomeDoUsuario(c.getValue().getIdUsuario())));
        colUsuario.setPrefWidth(160);

        TableColumn<Perfil, String> colTelefone = new TableColumn<>("Telefone");
        colTelefone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefone()));
        colTelefone.setPrefWidth(110);

        TableColumn<Perfil, String> colCidade = new TableColumn<>("Cidade");
        colCidade.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCidade()));
        colCidade.setPrefWidth(130);

        TableColumn<Perfil, String> colData = new TableColumn<>("Nascimento");
        colData.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataNascimento()));
        colData.setPrefWidth(100);

        tabela.getColumns().add(colId);
        tabela.getColumns().add(colUsuario);
        tabela.getColumns().add(colTelefone);
        tabela.getColumns().add(colCidade);
        tabela.getColumns().add(colData);
        tabela.setItems(dados);

        lblMensagem = new Label();

        // ----- botões -----
        Button btnAlterar = new Button("Alterar");
        btnAlterar.setOnAction((event) -> handleBtnAlterarOnClick(event));

        Button btnExcluir = new Button("Excluir");
        btnExcluir.setOnAction((event) -> handleBtnExcluirOnClick(event));

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setOnAction((event) -> mainController.mostrar());

        HBox botoes = new HBox(10);
        botoes.getChildren().addAll(btnAlterar, btnExcluir, btnVoltar);

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20;");
        layout.getChildren().addAll(lblTitulo, tabela, lblMensagem, botoes);

        cena = new Scene(layout, 700, 450);
    }

    /** Lê os arquivos e atualiza a tabela. */
    private void carregarTabela() {
        usuarios = ArquivosUsuario.lerLista();
        dados.setAll(ArquivoPerfil.lerLista());
    }

    /** Procura o nome do usuário pelo id. */
    private String nomeDoUsuario(int idUsuario) {
        int posicao = ArquivosUsuario.indiceDoId(usuarios, idUsuario);
        if (posicao < 0) {
            return "(usuário removido)";
        }
        return usuarios.get(posicao).getNome();
    }

    public void handleBtnAlterarOnClick(ActionEvent e) {
        Perfil selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            lblMensagem.setText("Selecione um perfil na tabela.");
            return;
        }
        // abre a janela modal; o código só continua depois que ela for fechada
        PerfilModalController modal = new PerfilModalController(this.stage, selecionado,
                nomeDoUsuario(selecionado.getIdUsuario()));
        modal.mostrar();
        carregarTabela();
        lblMensagem.setText("");
    }

    public void handleBtnExcluirOnClick(ActionEvent e) {
        Perfil selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            lblMensagem.setText("Selecione um perfil na tabela.");
            return;
        }

        // confirmação antes de apagar
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja realmente excluir o perfil " + selecionado.getId() + "?",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar exclusão");
        Optional<ButtonType> resposta = alerta.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.YES) {
            return;
        }

        ArrayList<Perfil> lista = ArquivoPerfil.lerLista();
        int posicao = ArquivoPerfil.indiceDoId(lista, selecionado.getId());
        if (posicao >= 0) {
            lista.remove(posicao);
        }
        if (ArquivoPerfil.salvarLista(lista)) {
            lblMensagem.setText("Perfil excluído.");
        } else {
            lblMensagem.setText("Erro ao gravar o arquivo de perfis.");
        }
        carregarTabela();
    }
}
