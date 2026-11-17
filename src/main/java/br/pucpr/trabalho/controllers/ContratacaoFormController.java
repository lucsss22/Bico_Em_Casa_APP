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
import java.time.LocalDate;

public class ContratacaoFormController {

    private final Repositorio<Contratacao> repositorio;
    private final Contratacao contratacao;

    private Stage stage;
    private final ComboBox<Servico> cbServico = new ComboBox<>();
    private final ComboBox<Usuario> cbCliente = new ComboBox<>();
    private final TextField txtData = new TextField();
    private final TextField txtEndereco = new TextField();
    private final ComboBox<String> cbStatus = new ComboBox<>();

    public ContratacaoFormController(Repositorio<Contratacao> repositorio) {
        this.repositorio = repositorio;
        this.contratacao = null;
    }

    public ContratacaoFormController(Repositorio<Contratacao> repositorio, Contratacao contratacao) {
        this.repositorio = repositorio;
        this.contratacao = contratacao;
    }

    private boolean isInclusao() {
        return contratacao == null;
    }

    public void mostrar() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(isInclusao() ? "Nova contratação" : "Consultar contratação #" + contratacao.getId());

        txtData.setPromptText("DD/MM/AAAA");
        txtEndereco.setPromptText("Ex.: Rua das Flores, 100");
        txtEndereco.setPrefColumnCount(25);
        configurarComboServico();
        configurarComboCliente();
        cbStatus.getItems().setAll("Agendada", "Concluída", "Cancelada");

        Label titulo = Estilo.titulo(isInclusao() ? "Cadastrar contratação" : "Contratação #" + contratacao.getId());

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.addRow(0, new Label("Serviço:"), cbServico);
        grade.addRow(1, new Label("Cliente:"), cbCliente);
        grade.addRow(2, new Label("Data do serviço:"), txtData);
        grade.addRow(3, new Label("Endereço:"), txtEndereco);

        Button btnSalvar = new Button(isInclusao() ? "Cadastrar" : "Salvar alterações");
        btnSalvar.setOnAction(e -> salvar());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> stage.close());

        HBox botoes = new HBox(10, btnSalvar);

        if (!isInclusao()) {
            preencherCampos();
            grade.addRow(4, new Label("Status:"), cbStatus);

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

    private void configurarComboServico() {
        try {
            cbServico.getItems().setAll(new Repositorio<Servico>("servicos.dat").listar());
        } catch (PersistenciaException e) {
            Alertas.erro(e.getMessage());
        }
        cbServico.setPromptText("Selecione");
        cbServico.setConverter(new StringConverter<>() {
            @Override
            public String toString(Servico servico) {
                if (servico == null) {
                    return "";
                }
                return servico.getTitulo() + " - " + Conversor.formatarMoeda(BigDecimal.valueOf(servico.getPrecoServico()));
            }

            @Override
            public Servico fromString(String texto) {
                return null;
            }
        });
        if (cbServico.getItems().isEmpty()) {
            cbServico.setPromptText("Nenhum serviço cadastrado");
        }
    }

    private void configurarComboCliente() {
        cbCliente.getItems().setAll(ArquivosUsuario.lerLista());
        cbCliente.setPromptText("Selecione");
        cbCliente.setConverter(new StringConverter<>() {
            @Override
            public String toString(Usuario usuario) {
                return usuario == null ? "" : usuario.getNome();
            }

            @Override
            public Usuario fromString(String texto) {
                return null;
            }
        });
        if (cbCliente.getItems().isEmpty()) {
            cbCliente.setPromptText("Nenhum usuário cadastrado");
        }
    }

    private void preencherCampos() {
        for (Servico s : cbServico.getItems()) {
            if (s.getId() == contratacao.getIdServico()) {
                cbServico.setValue(s);
                break;
            }
        }
        for (Usuario u : cbCliente.getItems()) {
            if (u.getId() == contratacao.getIdCliente()) {
                cbCliente.setValue(u);
                break;
            }
        }
        txtData.setText(Conversor.formatarData(contratacao.getDataServico()));
        txtEndereco.setText(contratacao.getEndereco());
        cbStatus.setValue(contratacao.getStatus());
    }

    private void salvar() {
        try {
            Servico servico = cbServico.getValue();
            if (servico == null) {
                throw new ValidacaoException("Selecione o serviço contratado.");
            }
            Usuario cliente = cbCliente.getValue();
            if (cliente == null) {
                throw new ValidacaoException("Selecione o cliente da contratação.");
            }
            LocalDate data = Conversor.paraData(txtData.getText(), "Data do serviço");

            if (isInclusao()) {
                Contratacao nova = new Contratacao(servico.getId(), cliente.getId(), data, txtEndereco.getText());
                repositorio.inserir(nova);
                Alertas.info("Contratação cadastrada com sucesso!");
            } else {
                contratacao.setIdServico(servico.getId());
                contratacao.setIdCliente(cliente.getId());
                if (!data.equals(contratacao.getDataServico())) {
                    contratacao.setDataServico(data);
                }
                contratacao.setEndereco(txtEndereco.getText());
                contratacao.setStatus(cbStatus.getValue());
                repositorio.atualizar(contratacao);
                Alertas.info("Contratação alterada com sucesso!");
            }
            stage.close();
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void excluir() {
        if (!Alertas.confirmar("Deseja realmente excluir a contratação #" + contratacao.getId() + "?")) {
            return;
        }
        try {
            repositorio.excluir(contratacao.getId());
            Alertas.info("Contratação excluída.");
            stage.close();
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao excluir: " + e.getMessage());
        }
    }
}
