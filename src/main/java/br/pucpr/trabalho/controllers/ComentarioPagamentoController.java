package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.ArquivosUsuario;
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

public class ComentarioPagamentoController {

    // Provisório: simula contratações até a tela de Contratação ficar pronta
    private static class ContratacaoExemplo {
        int id;
        String nomeServico;
        BigDecimal preco;

        ContratacaoExemplo(int id, String nomeServico, String preco) {
            this.id = id;
            this.nomeServico = nomeServico;
            this.preco = new BigDecimal(preco);
        }

        @Override
        public String toString() {
            return nomeServico + " - " + Conversor.formatarMoeda(preco);
        }
    }

    private ArrayList<ContratacaoExemplo> contratacoes = new ArrayList<>();

    public ComentarioPagamentoController() {
        contratacoes.add(new ContratacaoExemplo(1, "Instalação de chuveiro", "150.00"));
        contratacoes.add(new ContratacaoExemplo(2, "Pintura de parede", "480.00"));
        contratacoes.add(new ContratacaoExemplo(3, "Limpeza residencial", "220.00"));
        contratacoes.add(new ContratacaoExemplo(4, "Montagem de móveis", "95.50"));
        contratacoes.add(new ContratacaoExemplo(5, "Reparo de vazamento", "1250.00"));
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
        ContratacaoExemplo c = escolherContratacao();
        if (c != null) {
            new ComentarioListaController(c.id, c.nomeServico).mostrar();
        }
    }

    private void abrirPagamentos(TipoPerfil perfil) {
        Usuario usuario = escolherUsuario();
        if (usuario == null) {
            return;
        }
        ContratacaoExemplo c = escolherContratacao();
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

    private ContratacaoExemplo escolherContratacao() {
        ChoiceDialog<ContratacaoExemplo> dialogo = new ChoiceDialog<>(contratacoes.get(0), contratacoes);
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
