package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.ArquivosUsuario;
import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.domains.Contratacao;
import br.pucpr.trabalho.domains.Servico;
import br.pucpr.trabalho.domains.TipoPerfil;
import br.pucpr.trabalho.domains.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComentarioPagamentoController {

    // As contratações são as reais, cadastradas no módulo de Contratações
    // (contratacoes.dat). O preço e o título vêm do serviço contratado (servicos.dat).
    // Esta classe guarda só o que as telas de comentários e pagamentos precisam.
    static class OpcaoContratacao {
        int id;
        String nomeServico;
        BigDecimal preco;
        String descricao;

        OpcaoContratacao(Contratacao contratacao, Servico servico) {
            this.id = contratacao.getId();
            this.nomeServico = servico.getTitulo();
            this.preco = BigDecimal.valueOf(servico.getPrecoServico());
            this.descricao = "#" + id + " - " + nomeServico
                    + " - " + Conversor.formatarData(contratacao.getDataServico())
                    + " - " + Conversor.formatarMoeda(preco);
        }

        @Override
        public String toString() {
            return descricao;
        }
    }

    // Monta as opções do diálogo. Ignora contratações canceladas e as de serviço removido.
    // Se idCliente não for null, mostra só as contratações desse cliente.
    static ArrayList<OpcaoContratacao> montarOpcoes(List<Contratacao> contratacoes, List<Servico> servicos,
                                                    Integer idCliente) {
        Map<Integer, Servico> servicosPorId = new HashMap<>();
        for (Servico s : servicos) {
            servicosPorId.put(s.getId(), s);
        }

        ArrayList<OpcaoContratacao> opcoes = new ArrayList<>();
        for (Contratacao c : contratacoes) {
            if ("Cancelada".equals(c.getStatus())) {
                continue;
            }
            if (idCliente != null && c.getIdCliente() != idCliente) {
                continue;
            }
            Servico servico = servicosPorId.get(c.getIdServico());
            if (servico == null) {
                continue;
            }
            opcoes.add(new OpcaoContratacao(c, servico));
        }
        return opcoes;
    }

    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("Comentários e Pagamentos");

        Label titulo = Estilo.titulo("Comentários e Pagamentos");

        Button btnComentarios = criarBotao("Comentários");
        btnComentarios.setOnAction(e -> abrirComentarios());

        Button btnPagamentos = criarBotao("Pagamentos (cliente)");
        btnPagamentos.setOnAction(e -> abrirPagamentos(TipoPerfil.CLIENTE));

        Button btnRecebimentos = criarBotao("Recebimentos (profissional)");
        btnRecebimentos.setOnAction(e -> abrirPagamentos(TipoPerfil.PROFISSIONAL));

        Button btnFechar = criarBotao("Fechar");
        btnFechar.setOnAction(e -> stage.close());

        VBox raiz = new VBox(12, titulo, new Separator(),
                new Label("Cliente"), btnComentarios, btnPagamentos,
                new Label("Profissional"), btnRecebimentos,
                new Separator(), btnFechar);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(25));

        stage.setScene(new Scene(raiz, 380, 420));
        stage.show();
    }

    private void abrirComentarios() {
        OpcaoContratacao c = escolherContratacao(null);
        if (c != null) {
            new ComentarioListaController(c.id, c.nomeServico).mostrar();
        }
    }

    private void abrirPagamentos(TipoPerfil perfil) {
        Usuario usuario = escolherUsuario();
        if (usuario == null) {
            return;
        }
        // O cliente só vê as próprias contratações; o profissional escolhe entre todas
        Integer idCliente = perfil == TipoPerfil.CLIENTE ? usuario.getId() : null;
        OpcaoContratacao c = escolherContratacao(idCliente);
        if (c != null) {
            new PagamentoListaController(c.id, c.nomeServico, c.preco, perfil, usuario).mostrar();
        }
    }

    // Devolve null se o usuário cancelar
    private Usuario escolherUsuario() {
        ArrayList<Usuario> usuarios = ArquivosUsuario.lerLista();
        if (usuarios.isEmpty()) {
            Alertas.erro("Nenhum usuário cadastrado.\nCadastre um usuário em \"Incluir Usuario\" no menu principal.");
            return null;
        }
        ChoiceDialog<Usuario> dialogo = new ChoiceDialog<>(usuarios.get(0), usuarios);
        dialogo.setTitle("Usuário");
        dialogo.setHeaderText("Quem é você?");
        dialogo.setContentText("Usuário:");
        return dialogo.showAndWait().orElse(null);
    }

    // Devolve null se não houver contratação ou se o usuário cancelar
    private OpcaoContratacao escolherContratacao(Integer idCliente) {
        ArrayList<OpcaoContratacao> opcoes;
        try {
            List<Contratacao> contratacoes = new Repositorio<Contratacao>("contratacoes.dat").listar();
            List<Servico> servicos = new Repositorio<Servico>("servicos.dat").listar();
            opcoes = montarOpcoes(contratacoes, servicos, idCliente);
        } catch (PersistenciaException e) {
            Alertas.erro(e.getMessage());
            return null;
        }

        if (opcoes.isEmpty()) {
            Alertas.erro("Nenhuma contratação disponível. Cadastre uma em Contratações.");
            return null;
        }

        ChoiceDialog<OpcaoContratacao> dialogo = new ChoiceDialog<>(opcoes.get(0), opcoes);
        dialogo.setTitle("Serviço");
        dialogo.setHeaderText("Selecione o serviço contratado");
        dialogo.setContentText("Serviço:");
        return dialogo.showAndWait().orElse(null);
    }

    private Button criarBotao(String texto) {
        Button botao = new Button(texto);
        botao.setPrefWidth(220);
        return botao;
    }
}
