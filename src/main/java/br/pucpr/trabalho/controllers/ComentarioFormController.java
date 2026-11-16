package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.domains.Comentario;
import br.pucpr.trabalho.domains.Periodo;
import br.pucpr.trabalho.domains.ValidacaoException;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;

// Se comentario for null a tela é de inclusão; senão é de consulta (alterar/excluir)
public class ComentarioFormController {

    private Repositorio<Comentario> repositorio;
    private Comentario comentario;
    private int contratacaoId;
    private String nomeServico;

    private Stage stage;
    private TextField txtAutor = new TextField();
    private TextField txtData = new TextField();
    private ComboBox<Periodo> cbPeriodo = new ComboBox<>();
    private CheckBox chkUrgente = new CheckBox("Preciso do serviço com urgência");
    private TextArea txtTexto = new TextArea();

    public ComentarioFormController(Repositorio<Comentario> repositorio, int contratacaoId, String nomeServico) {
        this.repositorio = repositorio;
        this.contratacaoId = contratacaoId;
        this.nomeServico = nomeServico;
    }

    public ComentarioFormController(Repositorio<Comentario> repositorio, Comentario comentario) {
        this.repositorio = repositorio;
        this.comentario = comentario;
        this.contratacaoId = comentario.getContratacaoId();
        this.nomeServico = comentario.getNomeServico();
    }

    public void mostrar() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (comentario == null) {
            stage.setTitle("Novo comentário");
        } else {
            stage.setTitle("Consultar comentário #" + comentario.getId());
        }

        txtAutor.setPromptText("Seu nome ou apelido");
        txtData.setPromptText("DD/MM/AAAA");
        cbPeriodo.getItems().addAll(Periodo.values());
        cbPeriodo.setPromptText("Selecione");
        txtTexto.setWrapText(true);
        txtTexto.setPrefRowCount(5);
        txtTexto.setPrefColumnCount(30);

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.addRow(0, new Label("Como prefere ser chamado:"), txtAutor);
        grade.addRow(1, new Label("Data desejada:"), txtData);
        grade.addRow(2, new Label("Período:"), cbPeriodo);
        grade.addRow(3, new Label("Urgente:"), chkUrgente);
        grade.addRow(4, new Label("Comentário:"), txtTexto);

        Button btnSalvar = new Button("Cadastrar");
        btnSalvar.setOnAction(e -> salvar());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> stage.close());

        HBox botoes = new HBox(10, btnSalvar);

        if (comentario != null) {
            preencherCampos();
            grade.addRow(5, new Label("Criado em:"), new Label(Conversor.formatarData(comentario.getDataCriacao())));
            btnSalvar.setText("Salvar alterações");
            Button btnExcluir = new Button("Excluir");
            btnExcluir.setOnAction(e -> excluir());
            botoes.getChildren().add(btnExcluir);
        }
        botoes.getChildren().add(btnCancelar);

        VBox raiz = new VBox(15, Estilo.titulo("Serviço: " + nomeServico), new Separator(), grade, botoes);
        raiz.setPadding(new Insets(20));

        stage.setScene(new Scene(raiz));
        stage.showAndWait();
    }

    private void preencherCampos() {
        txtAutor.setText(comentario.getAutor());
        txtData.setText(Conversor.formatarData(comentario.getDataDesejada()));
        cbPeriodo.setValue(comentario.getPeriodo());
        chkUrgente.setSelected(comentario.isUrgente());
        txtTexto.setText(comentario.getTexto());
    }

    private void salvar() {
        try {
            LocalDate data = Conversor.paraData(txtData.getText(), "Data desejada");
            if (comentario == null) {
                Comentario novo = new Comentario(contratacaoId, nomeServico, txtAutor.getText(),
                        txtTexto.getText(), data, cbPeriodo.getValue(), chkUrgente.isSelected());
                repositorio.inserir(novo);
                Alertas.info("Comentário cadastrado com sucesso!");
            } else {
                comentario.setAutor(txtAutor.getText());
                comentario.setTexto(txtTexto.getText());
                comentario.setDataDesejada(data);
                comentario.setPeriodo(cbPeriodo.getValue());
                comentario.setUrgente(chkUrgente.isSelected());
                repositorio.atualizar(comentario);
                Alertas.info("Comentário alterado com sucesso!");
            }
            stage.close();
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void excluir() {
        if (!Alertas.confirmar("Deseja realmente excluir o comentário #" + comentario.getId() + "?")) {
            return;
        }
        try {
            repositorio.excluir(comentario.getId());
            Alertas.info("Comentário excluído.");
            stage.close();
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao excluir: " + e.getMessage());
        }
    }
}
