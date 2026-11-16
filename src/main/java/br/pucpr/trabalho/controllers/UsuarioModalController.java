package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.domains.Usuario;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
        stage.showAndWait();
    }

    // TODO (Lucas): montar aqui a tela do modal (campos, botões, ações).
    // Versão mínima só para o projeto compilar; ainda não é chamada por nenhuma tela.
    private void criarUI(){
        stage = new Stage();
        stage.initOwner(stageOwner);
        stage.setTitle("Usuario");
        lblMensagem = new Label("Tela em construção.");
        cena = new Scene(new javafx.scene.layout.VBox(lblMensagem), 300, 100);
        stage.setScene(cena);
    }















}
