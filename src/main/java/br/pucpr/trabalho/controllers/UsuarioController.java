package br.pucpr.trabalho.controllers;

import java.util.ArrayList;

import br.pucpr.trabalho.arquivos.ArquivosUsuario;
import br.pucpr.trabalho.domains.Usuario;
import br.pucpr.trabalho.domains.ValidacaoExceptio;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class UsuarioController {
    private Stage stage;
    private MainController mainController;
    private Scene cena;

    private TextField txtNome;
    private TextField txtEmail;
    private PasswordField txtSenha;
    private Label lblMensagem;

    public UsuarioController(Stage stage , MainController mainController){
        this.stage=stage;
        this.mainController = mainController;
    }

    public void mostrar(){
        criarUI();
        stage.setTitle("Incluir Usuario");
        stage.setScene(cena);
        stage.show();
    }

    private void criarUI(){
        GridPane pane = new GridPane();
        pane.setVgap(10);
        pane.setHgap(10);
        pane.setPadding(new Insets(20,20,20,20));

        Label lblTitulo = new Label("Incluir Usuario");
        lblTitulo.setFont(new  Font("Arial" , 20));
        pane.add(lblTitulo, 0, 0 ,  2 ,1 );



        txtNome = new TextField();
        txtEmail = new TextField();
        txtSenha = new PasswordField();
        pane.add(new Label( "Nome:"), 0, 1);
        pane.add(txtNome,1,1);

        pane.add(new  Label("Email:"), 0, 2);
        pane.add(txtEmail,1,2);

        pane.add(new  Label("Senha:"), 0, 3);
        pane.add(txtSenha,1,3);


        lblMensagem = new Label();
        pane.add(lblMensagem, 0, 4, 2,1);

        Button btnSalvar = new Button("Salvar");
        btnSalvar.setOnAction((event)-> handleBtnSalvarOnClick(event));
        pane.add(btnSalvar , 0 , 5 );

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setOnAction((event)-> mainController.mostrar());
        pane.add(btnVoltar , 1, 5 );

        cena = new Scene(pane,450 , 320);


    }
    public void handleBtnSalvarOnClick(ActionEvent e ){
        try{
            ArrayList<Usuario> lista = ArquivosUsuario.lerLista();

            if (ArquivosUsuario.emailExiste(lista, txtEmail.getText() , 0)) {
                throw new ValidacaoExceptio("Ja existe um usuario com esse email");
                
            }
            int id = ArquivosUsuario.proximoId((lista));
            Usuario usuario = new Usuario(id, txtNome.getText(), txtEmail.getText(), txtSenha.getText());

            lista.add(usuario);
            if (ArquivosUsuario.salvarLista(lista)) {
                lblMensagem.setText("Usuario salvo com o id " + id + ".");
                txtNome.clear();
                txtEmail.clear();
                txtSenha.clear();
                
            }else{
                lblMensagem.setText("Erro ao gravar o arquivo de usuario.");
            }

        }catch(ValidacaoExceptio ex) {
            lblMensagem.setText(ex.getMessage());
        }
    }
}
