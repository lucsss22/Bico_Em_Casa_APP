// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.domains.Comentario;
import br.pucpr.trabalho.domains.Periodo;
import br.pucpr.trabalho.domains.ValidacaoException;
import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.controllers.Conversor;
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

/**
 * Formulário do comentário. Funciona em dois modos:
 * - Inclusão: recebe a contratação (id + nome do serviço); os campos vêm vazios.
 * - Consulta: recebe um comentário; os campos vêm preenchidos
 *   e aparecem os botões para alterar e excluir.
 *
 * O id da contratação nunca aparece para o usuário: ele vem da tela de Contratação.
 * O nome do serviço aparece apenas como informação (não é editável).
 */
public class ComentarioFormController {

    private final Repositorio<Comentario> repositorio;
    private final Comentario comentario;
    private final int contratacaoId;
    private final String nomeServico;

    private Stage stage;
    private final TextField txtAutor = new TextField();
    private final TextField txtData = new TextField();
    private final ComboBox<Periodo> cbPeriodo = new ComboBox<>();
    private final CheckBox chkUrgente = new CheckBox("Preciso do serviço com urgência");
    private final TextArea txtTexto = new TextArea();

    /** Modo inclusão: novo comentário para a contratação informada. */
    public ComentarioFormController(Repositorio<Comentario> repositorio, int contratacaoId, String nomeServico) {
        this.repositorio = repositorio;
        this.comentario = null;
        this.contratacaoId = contratacaoId;
        this.nomeServico = nomeServico;
    }

    /** Modo consulta: abre um comentário existente para alterar ou excluir. */
    public ComentarioFormController(Repositorio<Comentario> repositorio, Comentario comentario) {
        this.repositorio = repositorio;
        this.comentario = comentario;
        this.contratacaoId = comentario.getContratacaoId();
        this.nomeServico = comentario.getNomeServico();
    }

    private boolean isInclusao() {
        return comentario == null;
    }

    public void mostrar() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(isInclusao() ? "Novo comentário" : "Consultar comentário #" + comentario.getId());

        txtAutor.setPromptText("Seu nome ou apelido");
        txtData.setPromptText("DD/MM/AAAA");
        cbPeriodo.getItems().addAll(Periodo.values());
        txtTexto.setWrapText(true);
        txtTexto.setPrefRowCount(5);
        txtTexto.setPromptText("Descreva o que você precisa que o profissional faça...");

        Label titulo = Estilo.titulo("Serviço: " + nomeServico);

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.addRow(0, new Label("Como prefere ser chamado:"), txtAutor);
        grade.addRow(1, new Label("Data desejada:"), txtData);
        grade.addRow(2, new Label("Período:"), cbPeriodo);
        grade.addRow(3, new Label("Urgente:"), chkUrgente);
        grade.addRow(4, new Label("Comentário:"), txtTexto);
        cbPeriodo.setPromptText("Selecione");
        txtTexto.setPrefColumnCount(30);

        Button btnSalvar = new Button(isInclusao() ? "Cadastrar" : "Salvar alterações");
        btnSalvar.setOnAction(e -> salvar());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> stage.close());

        HBox botoes = new HBox(10, btnSalvar);

        if (!isInclusao()) {
            preencherCampos();
            grade.addRow(5, new Label("Criado em:"), new Label(Conversor.formatarData(comentario.getDataCriacao())));

            Button btnExcluir = new Button("Excluir");
            btnExcluir.setOnAction(e -> excluir());
            botoes.getChildren().add(btnExcluir);
        }
        botoes.getChildren().add(btnCancelar);

        VBox raiz = new VBox(15, titulo, new Separator(), grade, botoes);
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

            if (isInclusao()) {
                // contratacaoId e nomeServico vêm da contratação, não do que o usuário digitou
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
