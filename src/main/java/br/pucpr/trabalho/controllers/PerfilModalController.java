package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.ArquivoPerfil;
import br.pucpr.trabalho.domains.Perfil;
import br.pucpr.trabalho.domains.ValidacaoException;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Janela MODAL para alterar um perfil (mesmo padrão do Exemplo 7).
 * O usuário dono do perfil não pode ser trocado: só é mostrado.
 */
public class PerfilModalController {
    /** Stage desta janela */
    private Stage stage;
    /** Stage "dono" desta janela, porque ela é modal */
    private Stage stageOwner;
    private Scene cena;

    private Perfil perfil;
    private String nomeUsuario;
    private TextField txtTelefone;
    private TextField txtCidade;
    private TextField txtData;
    private Label lblMensagem;

    public PerfilModalController(Stage stageOwner, Perfil perfil, String nomeUsuario) {
        this.stageOwner = stageOwner;
        this.perfil = perfil;
        this.nomeUsuario = nomeUsuario;
    }

    public void mostrar() {
        criarUI();
        stage.showAndWait(); // Mostra e espera o fechamento da janela modal
    }

    private void criarUI() {
        this.stage = new Stage();
        stage.setTitle("Alterar Perfil " + perfil.getId());

        // Torna a janela modal
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(this.stageOwner);

        GridPane pane = new GridPane();
        pane.setVgap(10);
        pane.setHgap(10);
        pane.setPadding(new Insets(20, 20, 20, 20));

        Label lblTitulo = new Label("Alterar Perfil");
        lblTitulo.setFont(new Font("Arial", 20));
        pane.add(lblTitulo, 0, 0, 2, 1);

        txtTelefone = new TextField(perfil.getTelefone());
        txtCidade = new TextField(perfil.getCidade());
        txtData = new TextField(perfil.getDataNascimento());
        pane.add(new Label("Usuário:"), 0, 1);
        pane.add(new Label(nomeUsuario), 1, 1);
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

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction((event) -> this.stage.close());
        pane.add(btnCancelar, 1, 6);

        this.cena = new Scene(pane, 480, 380);
        this.stage.setScene(this.cena);
    }

    public void handleBtnSalvarOnClick(ActionEvent e) {
        try {
            // cria um objeto novo com o mesmo id e o mesmo usuário: o construtor valida TUDO.
            // Se der erro, nada foi alterado.
            Perfil alterado = new Perfil(perfil.getId(), perfil.getIdUsuario(),
                    txtTelefone.getText(), txtCidade.getText(), txtData.getText());

            ArrayList<Perfil> lista = ArquivoPerfil.lerLista();
            int posicao = ArquivoPerfil.indiceDoId(lista, perfil.getId());
            if (posicao < 0) {
                throw new ValidacaoException("Esse perfil não existe mais.");
            }
            lista.set(posicao, alterado);

            if (ArquivoPerfil.salvarLista(lista)) {
                this.stage.close();
            } else {
                lblMensagem.setText("Erro ao gravar o arquivo de perfis.");
            }
        } catch (ValidacaoException ex) {
            lblMensagem.setText(ex.getMessage());
        }
    }
}
