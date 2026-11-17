// Autor: Carlos Alberto
package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.ArquivosUsuario;
import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.domains.Contratacao;
import br.pucpr.trabalho.domains.Servico;
import br.pucpr.trabalho.domains.Usuario;
import br.pucpr.trabalho.domains.ValidacaoException;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;

public class ServicoFormController {

    private final Repositorio<Servico> repositorio;
    private final Servico servico;

    private Stage stage;
    private final TextField txtTitulo = new TextField();
    private final TextField txtPreco = new TextField();
    private final ComboBox<Usuario> cbUsuario = new ComboBox<>();

    public ServicoFormController(Repositorio<Servico> repositorio) {
        this.repositorio = repositorio;
        this.servico = null;
    }

    public ServicoFormController(Repositorio<Servico> repositorio, Servico servico) {
        this.repositorio = repositorio;
        this.servico = servico;
    }

    private boolean isInclusao() {
        return servico == null;
    }

    public void mostrar() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(isInclusao() ? "Novo serviço" : "Consultar serviço #" + servico.getId());

        txtTitulo.setPromptText("Ex.: Instalação de chuveiro");
        txtTitulo.setPrefColumnCount(25);
        txtPreco.setPromptText("Ex.: 150,00");
        configurarComboUsuario();

        Label titulo = Estilo.titulo(isInclusao() ? "Cadastrar serviço" : "Serviço #" + servico.getId());

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.addRow(0, new Label("Título:"), txtTitulo);
        grade.addRow(1, new Label("Profissional:"), cbUsuario);
        grade.addRow(2, new Label("Preço (R$):"), txtPreco);

        Button btnSalvar = new Button(isInclusao() ? "Cadastrar" : "Salvar alterações");
        btnSalvar.setOnAction(e -> salvar());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> stage.close());

        HBox botoes = new HBox(10, btnSalvar);

        if (!isInclusao()) {
            preencherCampos();
            grade.addRow(3, new Label("Criado em:"), new Label(Conversor.formatarData(servico.getCriadoEm())));
            grade.addRow(4, new Label("Atualizado em:"), new Label(Conversor.formatarData(servico.getAtualizadoEm())));

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

    private void configurarComboUsuario() {
        cbUsuario.getItems().setAll(ArquivosUsuario.lerLista());
        cbUsuario.setPromptText("Selecione");
        cbUsuario.setConverter(new StringConverter<>() {
            @Override
            public String toString(Usuario usuario) {
                return usuario == null ? "" : usuario.getNome() + " (" + usuario.getEmail() + ")";
            }

            @Override
            public Usuario fromString(String texto) {
                return null;
            }
        });
        if (cbUsuario.getItems().isEmpty()) {
            cbUsuario.setPromptText("Nenhum usuário cadastrado");
        }
    }

    private void preencherCampos() {
        txtTitulo.setText(servico.getTitulo());
        txtPreco.setText(Conversor.formatarDecimal(BigDecimal.valueOf(servico.getPrecoServico())));
        for (Usuario u : cbUsuario.getItems()) {
            if (u.getId() == servico.getIdUsuario()) {
                cbUsuario.setValue(u);
                break;
            }
        }
    }

    private void salvar() {
        try {
            Usuario usuario = cbUsuario.getValue();
            if (usuario == null) {
                throw new ValidacaoException("Selecione o profissional responsável pelo serviço.");
            }
            double preco = Conversor.paraDecimal(txtPreco.getText(), "Preço").doubleValue();

            if (isInclusao()) {
                Servico novo = new Servico(txtTitulo.getText().trim(), usuario.getId(), preco);
                repositorio.inserir(novo);
                Alertas.info("Serviço cadastrado com sucesso!");
            } else {
                servico.setTitulo(txtTitulo.getText().trim());
                servico.setIdUsuario(usuario.getId());
                servico.setPrecoServico(preco);
                repositorio.atualizar(servico);
                Alertas.info("Serviço alterado com sucesso!");
            }
            stage.close();
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void excluir() {
        try {
            if (servicoEmUso()) {
                Alertas.erro("Este serviço possui contratações e não pode ser excluído.");
                return;
            }
            if (!Alertas.confirmar("Deseja realmente excluir o serviço \"" + servico.getTitulo() + "\"?")) {
                return;
            }
            repositorio.excluir(servico.getId());
            Alertas.info("Serviço excluído.");
            stage.close();
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao excluir: " + e.getMessage());
        }
    }

    private boolean servicoEmUso() throws PersistenciaException {
        Repositorio<Contratacao> contratacoes = new Repositorio<>("contratacoes.dat");
        for (Contratacao c : contratacoes.listar()) {
            if (c.getIdServico() == servico.getId()) {
                return true;
            }
        }
        return false;
    }
}
