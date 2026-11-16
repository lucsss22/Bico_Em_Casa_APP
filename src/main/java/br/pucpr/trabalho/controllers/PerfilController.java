package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.ArquivoPerfil;
import br.pucpr.trabalho.arquivos.ArquivosUsuario;
import br.pucpr.trabalho.domains.Perfil;
import br.pucpr.trabalho.domains.Usuario;
import br.pucpr.trabalho.domains.ValidacaoException;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Controller da tela de INCLUSÃO de perfil.
 * Cada usuário pode ter no máximo um perfil.
 */
public class PerfilController {

    private Stage stage;
    private MainController mainController;
    private Scene cena;

    private TextField txtIdUsuario;
    private TextField txtTelefone;
    private TextField txtCidade;
    private TextField txtData;
    private Label lblMensagem;

    public PerfilController(Stage stage, MainController mainController) {
        this.stage = stage;
        this.mainController = mainController;
    }

    public void mostrar() {
        criarUI();
        stage.setTitle("Incluir Perfil");
        stage.setScene(cena);
        stage.show();
    }

    private void criarUI() {
        GridPane pane = new GridPane();
        pane.setVgap(10);
        pane.setHgap(10);
        pane.setPadding(new Insets(20, 20, 20, 20));

        Label lblTitulo = new Label("Incluir Perfil");
        lblTitulo.setFont(new Font("Arial", 20));
        pane.add(lblTitulo, 0, 0, 2, 1);

        txtIdUsuario = new TextField();
        txtTelefone = new TextField();
        txtCidade = new TextField();
        txtData = new TextField();
        txtData.setPromptText("DD/MM/AAAA");
        pane.add(new Label("Id do usuário:"), 0, 1);
        pane.add(txtIdUsuario, 1, 1);
        pane.add(new Label("Telefone:"), 0, 2);
        pane.add(txtTelefone, 1, 2);
        pane.add(new Label("Cidade:"), 0, 3);
        pane.add(txtCidade, 1, 3);
        pane.add(new Label("Nascimento:"), 0, 4);
        pane.add(txtData, 1, 4);

        lblMensagem = new Label();
        pane.add(lblMensagem, 0, 5, 2, 1);

        Button btnSalvar = new Button("Salvar");
        btnSalvar.setOnAction((event) -> handleBtnSalvarOnClick(event));
        pane.add(btnSalvar, 0, 6);

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setOnAction((event) -> mainController.mostrar());
        pane.add(btnVoltar, 1, 6);

        cena = new Scene(pane, 480, 380);
    }

    public void handleBtnSalvarOnClick(ActionEvent e) {
        try {
            // campo numérico: se não for um número inteiro, lança NumberFormatException
            int idUsuario = Integer.parseInt(txtIdUsuario.getText().trim());

            ArrayList<Usuario> usuarios = ArquivosUsuario.lerLista();
            if (ArquivosUsuario.indiceDoId(usuarios, idUsuario) < 0) {
                throw new ValidacaoException("Não existe usuário com o id " + idUsuario + ".");
            }

            ArrayList<Perfil> perfis = ArquivoPerfil.lerLista();
            if (ArquivoPerfil.usuarioTemPerfil(perfis, idUsuario)) {
                throw new ValidacaoException("Esse usuário já possui um perfil.");
            }

            int id = ArquivoPerfil.proximoId(perfis);
            // o construtor valida os dados; se algo estiver errado lança ValidacaoException
            Perfil perfil = new Perfil(id, idUsuario, txtTelefone.getText(),
                    txtCidade.getText(), txtData.getText());

            perfis.add(perfil);
            if (ArquivoPerfil.salvarLista(perfis)) {
                lblMensagem.setText("Perfil salvo com o id " + id + ".");
                txtIdUsuario.clear();
                txtTelefone.clear();
                txtCidade.clear();
                txtData.clear();
            } else {
                lblMensagem.setText("Erro ao gravar o arquivo de perfis.");
            }
        } catch (NumberFormatException ex) {
            lblMensagem.setText("O id do usuário deve ser um número inteiro.");
        } catch (ValidacaoException ex) {
            lblMensagem.setText(ex.getMessage());
        }
    }
}
