// Autor: Carlos Alberto
package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.ArquivosUsuario;
import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.domains.Servico;
import br.pucpr.trabalho.domains.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServicoListaController {

    private final Repositorio<Servico> repositorio = new Repositorio<>("servicos.dat");
    private final TableView<Servico> tabela = new TableView<>();

    private final Map<Integer, String> nomesUsuarios = new HashMap<>();

    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("Serviços");

        Label titulo = Estilo.titulo("Serviços cadastrados");

        montarTabela();

        Button btnNovo = new Button("Novo serviço");
        btnNovo.setOnAction(e -> {
            new ServicoFormController(repositorio).mostrar();
            carregarTabela();
        });

        Button btnConsultar = new Button("Consultar selecionado");
        btnConsultar.setOnAction(e -> consultarSelecionado());

        Button btnFechar = new Button("Fechar");
        btnFechar.setOnAction(e -> stage.close());

        HBox botoes = new HBox(10, btnNovo, btnConsultar, btnFechar);

        VBox raiz = new VBox(10, titulo, tabela, botoes);
        raiz.setPadding(new Insets(20));
        VBox.setVgrow(tabela, Priority.ALWAYS);

        carregarTabela();

        stage.setScene(new Scene(raiz, 800, 450));
        stage.show();
    }

    private void montarTabela() {
        TableColumn<Servico, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colId.setPrefWidth(50);

        TableColumn<Servico, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitulo()));
        colTitulo.setPrefWidth(250);

        TableColumn<Servico, String> colUsuario = new TableColumn<>("Profissional");
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(nomeDoUsuario(c.getValue().getIdUsuario())));

        TableColumn<Servico, String> colPreco = new TableColumn<>("Preço");
        colPreco.setCellValueFactory(c -> new SimpleStringProperty(
                Conversor.formatarMoeda(BigDecimal.valueOf(c.getValue().getPrecoServico()))));

        TableColumn<Servico, String> colCriado = new TableColumn<>("Criado em");
        colCriado.setCellValueFactory(c -> new SimpleStringProperty(Conversor.formatarData(c.getValue().getCriadoEm())));

        TableColumn<Servico, String> colAtualizado = new TableColumn<>("Atualizado em");
        colAtualizado.setCellValueFactory(c -> new SimpleStringProperty(Conversor.formatarData(c.getValue().getAtualizadoEm())));

        tabela.getColumns().addAll(List.of(colId, colTitulo, colUsuario, colPreco, colCriado, colAtualizado));
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhum serviço cadastrado."));

        tabela.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                consultarSelecionado();
            }
        });
    }

    private void carregarTabela() {
        nomesUsuarios.clear();
        for (Usuario u : ArquivosUsuario.lerLista()) {
            nomesUsuarios.put(u.getId(), u.getNome());
        }

        try {
            tabela.setItems(FXCollections.observableArrayList(repositorio.listar()));
        } catch (PersistenciaException e) {
            Alertas.erro(e.getMessage());
        }
    }

    private String nomeDoUsuario(int idUsuario) {
        return nomesUsuarios.getOrDefault(idUsuario, "(usuário " + idUsuario + " não encontrado)");
    }

    private void consultarSelecionado() {
        Servico selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Alertas.erro("Selecione um serviço na tabela.");
            return;
        }
        new ServicoFormController(repositorio, selecionado).mostrar();
        carregarTabela();
    }
}
