// Autor: Tayllor Vinicius Martins dos Santos
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Menu de Comentários e Pagamentos.
 * Para abrir a partir do menu principal (MainController), basta um botão:
 *
 *     new ComentarioPagamentoController().mostrar();
 */
public class ComentarioPagamentoController {

    // PROVISÓRIO: enquanto a tela de Contratação (de outro integrante) não existe,
    // simulamos algumas contratações para conseguir abrir os comentários e os pagamentos.
    // Quando ela existir, quem abre essas telas é a própria tela de Contratação:
    //     new ComentarioListaController(contratacao.getId(), contratacao.getNomeServico()).mostrar();
    //     new PagamentoListaController(contratacao.getId(), contratacao.getNomeServico(), contratacao.getPrecoServico(),
    //                                  usuarioLogado.getPerfil()).mostrar();   // CLIENTE ou PROFISSIONAL, vindo do login
    // e o record ContratacaoExemplo, a lista CONTRATACOES_EXEMPLO e o método escolherContratacao podem ser removidos.
    private record ContratacaoExemplo(int id, String nomeServico, BigDecimal precoServico) {
        @Override
        public String toString() {
            return nomeServico + " – " + Conversor.formatarMoeda(precoServico);
        }
    }

    private static final List<ContratacaoExemplo> CONTRATACOES_EXEMPLO = List.of(
            new ContratacaoExemplo(1, "Instalação de chuveiro", new BigDecimal("150.00")),
            new ContratacaoExemplo(2, "Pintura de parede", new BigDecimal("480.00")),
            new ContratacaoExemplo(3, "Limpeza residencial", new BigDecimal("220.00")),
            new ContratacaoExemplo(4, "Montagem de móveis", new BigDecimal("95.50")),
            new ContratacaoExemplo(5, "Reparo de vazamento", new BigDecimal("1250.00"))
    );

    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("Comentários e Pagamentos");

        Label titulo = new Label("Comentários e Pagamentos");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        Button btnComentarios = criarBotao("Comentários");
        btnComentarios.setOnAction(e -> escolherContratacao("Comentários").ifPresent(c ->
                new ComentarioListaController(c.id(), c.nomeServico()).mostrar()));

        // Cliente: realiza pagamentos (primeiro escolhe qual usuário cadastrado ele é)
        Button btnPagamentos = criarBotao("Pagamentos (cliente)");
        btnPagamentos.setOnAction(e -> escolherUsuario("Pagamentos").ifPresent(u ->
                escolherContratacao("Pagamentos").ifPresent(c ->
                        new PagamentoListaController(c.id(), c.nomeServico(), c.precoServico(),
                                TipoPerfil.CLIENTE, u).mostrar())));

        // Profissional: confirma recebimentos em dinheiro e estorna
        Button btnRecebimentos = criarBotao("Recebimentos (profissional)");
        btnRecebimentos.setOnAction(e -> escolherUsuario("Recebimentos").ifPresent(u ->
                escolherContratacao("Recebimentos").ifPresent(c ->
                        new PagamentoListaController(c.id(), c.nomeServico(), c.precoServico(),
                                TipoPerfil.PROFISSIONAL, u).mostrar())));

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

    /**
     * Pergunta qual usuário cadastrado (classe Usuario, cadastro "Incluir Usuario")
     * está usando a tela. Se ainda não houver usuários, avisa para cadastrar primeiro.
     */
    private Optional<Usuario> escolherUsuario(String tela) {
        List<Usuario> usuarios = ArquivosUsuario.lerLista();
        if (usuarios.isEmpty()) {
            Alertas.erro("Nenhum usuário cadastrado.\nCadastre um usuário em \"Incluir Usuario\" no menu principal.");
            return Optional.empty();
        }
        ChoiceDialog<Usuario> dialogo = new ChoiceDialog<>(usuarios.get(0), usuarios);
        dialogo.setTitle(tela);
        dialogo.setHeaderText("Quem é você?");
        dialogo.setContentText("Usuário:");
        return dialogo.showAndWait();
    }

    /** Pergunta de qual serviço contratado o usuário quer ver os comentários/pagamentos. */
    private Optional<ContratacaoExemplo> escolherContratacao(String tela) {
        ChoiceDialog<ContratacaoExemplo> dialogo =
                new ChoiceDialog<>(CONTRATACOES_EXEMPLO.get(0), CONTRATACOES_EXEMPLO);
        dialogo.setTitle(tela);
        dialogo.setHeaderText("Selecione o serviço contratado");
        dialogo.setContentText("Serviço:");
        return dialogo.showAndWait();
    }

    private Button criarBotao(String texto) {
        Button botao = new Button(texto);
        botao.setPrefWidth(220);
        return botao;
    }
}
