package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.ArquivoPerfil;
import br.pucpr.trabalho.arquivos.ArquivosUsuario;
import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.domains.Contratacao;
import br.pucpr.trabalho.domains.Perfil;
import br.pucpr.trabalho.domains.Servico;
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
 * Controller da tela de CONSULTA de usuários:
 * mostra a tabela e permite alterar (janela modal) e excluir.
 */
public class UsuarioConsultaController {

    private Stage stage;
    private MainController mainController;
    private Scene cena;

    private TableView<Usuario> tabela;
    private ObservableList<Usuario> dados;
    private Label lblMensagem;

    public UsuarioConsultaController(Stage stage, MainController mainController) {
        this.stage = stage;
        this.mainController = mainController;
    }

    public void mostrar() {
        criarUI();
        carregarTabela();
        stage.setTitle("Consultar Usuários");
        stage.setScene(cena);
        stage.show();
    }

    private void criarUI() {
        Label lblTitulo = new Label("Consultar Usuários");
        lblTitulo.setFont(new Font("Arial", 20));

        // ----- tabela -----
        tabela = new TableView<>();
        dados = FXCollections.observableArrayList();

        TableColumn<Usuario, String> colId = new TableColumn<>("Id");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));

        TableColumn<Usuario, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colNome.setPrefWidth(200);

        TableColumn<Usuario, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colEmail.setPrefWidth(250);

        tabela.getColumns().add(colId);
        tabela.getColumns().add(colNome);
        tabela.getColumns().add(colEmail);
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

        cena = new Scene(layout, 650, 450);
    }

    /** Lê o arquivo e atualiza a tabela. */
    private void carregarTabela() {
        dados.setAll(ArquivosUsuario.lerLista());
    }

    public void handleBtnAlterarOnClick(ActionEvent e) {
        Usuario selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            lblMensagem.setText("Selecione um usuário na tabela.");
            return;
        }
        // abre a janela modal; o código só continua depois que ela for fechada
        UsuarioModalController modal = new UsuarioModalController(this.stage, selecionado);
        modal.mostrar();
        carregarTabela();
        lblMensagem.setText("");
    }

    public void handleBtnExcluirOnClick(ActionEvent e) {
        Usuario selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            lblMensagem.setText("Selecione um usuário na tabela.");
            return;
        }

        // regra: não exclui usuário que ainda tem perfil
        ArrayList<Perfil> perfis = ArquivoPerfil.lerLista();
        if (ArquivoPerfil.usuarioTemPerfil(perfis, selecionado.getId())) {
            lblMensagem.setText("Este usuário possui um perfil. Exclua o perfil primeiro.");
            return;
        }

        // regra: não exclui usuário que é profissional de um serviço ou cliente de uma contratação
        try {
            ArrayList<Servico> servicos = new Repositorio<Servico>("servicos.dat").listar();
            for (Servico s : servicos) {
                if (s.getIdUsuario() == selecionado.getId()) {
                    lblMensagem.setText("Este usuário possui serviços ou contratações e não pode ser excluído.");
                    return;
                }
            }
            ArrayList<Contratacao> contratacoes = new Repositorio<Contratacao>("contratacoes.dat").listar();
            for (Contratacao c : contratacoes) {
                if (c.getIdCliente() == selecionado.getId()) {
                    lblMensagem.setText("Este usuário possui serviços ou contratações e não pode ser excluído.");
                    return;
                }
            }
        } catch (PersistenciaException ex) {
            lblMensagem.setText(ex.getMessage());
            return;
        }

        // confirmação antes de apagar
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja realmente excluir o usuário " + selecionado.getNome() + "?",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar exclusão");
        Optional<ButtonType> resposta = alerta.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.YES) {
            return;
        }

        ArrayList<Usuario> lista = ArquivosUsuario.lerLista();
        int posicao = ArquivosUsuario.indiceDoId(lista, selecionado.getId());
        if (posicao >= 0) {
            lista.remove(posicao);
        }
        if (ArquivosUsuario.salvarLista(lista)) {
            lblMensagem.setText("Usuário excluído.");
        } else {
            lblMensagem.setText("Erro ao gravar o arquivo de usuários.");
        }
        carregarTabela();
    }
}
