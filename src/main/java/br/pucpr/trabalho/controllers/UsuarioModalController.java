package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.ArquivosUsuario;
import br.pucpr.trabalho.domains.Usuario;
import br.pucpr.trabalho.domains.ValidacaoException;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Janela MODAL para alterar um usuário (mesmo padrão do PerfilModalController).
 * A senha já vem preenchida com a atual; se o usuário não mexer, ela continua a mesma.
 */
public class UsuarioModalController {
    private Stage stage;
    private Stage stageOwner;
    private Scene cena;

    private Usuario usuario;
    private TextField txtNome;
    private TextField txtEmail;
    private PasswordField txtSenha;
    private Label lblMensagem;

    public  UsuarioModalController(Stage stageOwner , Usuario usuario){
        this.stageOwner = stageOwner;
        this.usuario = usuario;
    }

    public void mostrar(){
        criarUI();
        stage.showAndWait(); // Mostra e espera o fechamento da janela modal
    }

    private void criarUI(){
        this.stage = new Stage();
        stage.setTitle("Alterar Usuário " + usuario.getId());

        // Torna a janela modal
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(this.stageOwner);

        GridPane pane = new GridPane();
        pane.setVgap(10);
        pane.setHgap(10);
        pane.setPadding(new Insets(20, 20, 20, 20));

        Label lblTitulo = new Label("Alterar Usuário");
        lblTitulo.setFont(new Font("Arial", 20));
        pane.add(lblTitulo, 0, 0, 2, 1);

        txtNome = new TextField(usuario.getNome());
        txtEmail = new TextField(usuario.getEmail());
        txtSenha = new PasswordField();
        txtSenha.setText(usuario.getSenha());
        pane.add(new Label("Nome:"), 0, 1);
        pane.add(txtNome, 1, 1);
        pane.add(new Label("Email:"), 0, 2);
        pane.add(txtEmail, 1, 2);
        pane.add(new Label("Senha:"), 0, 3);
        pane.add(txtSenha, 1, 3);

        lblMensagem = new Label();
        pane.add(lblMensagem, 0, 4, 2, 1);

        Button btnSalvar = new Button("Salvar");
        btnSalvar.setOnAction((event) -> handleBtnSalvarOnClick(event));
        pane.add(btnSalvar, 0, 5);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction((event) -> this.stage.close());
        pane.add(btnCancelar, 1, 5);

        this.cena = new Scene(pane, 420, 300);
        this.stage.setScene(this.cena);
    }

    public void handleBtnSalvarOnClick(ActionEvent e) {
        try {
            // cria um objeto novo com o mesmo id: o construtor valida TUDO.
            // Se der erro, nada foi alterado.
            Usuario alterado = new Usuario(usuario.getId(), txtNome.getText(),
                    txtEmail.getText(), txtSenha.getText());

            ArrayList<Usuario> lista = ArquivosUsuario.lerLista();
            if (ArquivosUsuario.emailExiste(lista, alterado.getEmail(), usuario.getId())) {
                throw new ValidacaoException("Já existe um usuário com esse email.");
            }
            int posicao = ArquivosUsuario.indiceDoId(lista, usuario.getId());
            if (posicao < 0) {
                throw new ValidacaoException("Esse usuário não existe mais.");
            }
            lista.set(posicao, alterado);

            if (ArquivosUsuario.salvarLista(lista)) {
                this.stage.close();
            } else {
                lblMensagem.setText("Erro ao gravar o arquivo de usuários.");
            }
        } catch (ValidacaoException ex) {
            lblMensagem.setText(ex.getMessage());
        }
    }
}
