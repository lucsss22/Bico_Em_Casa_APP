package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.domains.Usuario;
import javafx.scene.Scene;
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















}
