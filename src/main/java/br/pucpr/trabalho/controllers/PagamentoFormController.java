// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.domains.FormaPagamento;
import br.pucpr.trabalho.domains.Pagamento;
import br.pucpr.trabalho.domains.StatusPagamento;
import br.pucpr.trabalho.domains.TipoPerfil;
import br.pucpr.trabalho.domains.ValidacaoException;
import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.controllers.Conversor;
import br.pucpr.trabalho.domains.Pix;
import br.pucpr.trabalho.domains.Perfil;
import br.pucpr.trabalho.domains.Usuario;
import br.pucpr.trabalho.arquivos.ArquivoPerfil;
import br.pucpr.trabalho.arquivos.ArquivosUsuario;
import com.google.zxing.WriterException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tela onde o cliente realiza o pagamento de um serviço contratado.
 * O serviço (nome e preço) vem da contratação: o cliente só vê essas informações.
 *
 * PIX: mostra o QR Code e o "copia e cola" com o valor do serviço.
 *      Depois de pagar no app do banco, o cliente clica em "Já paguei" -> PAGO.
 *
 * DINHEIRO: o cliente informa que pagou ao profissional, confirmando se foi o valor
 *      do serviço ou um valor maior -> AGUARDANDO CONFIRMAÇÃO.
 *      Depois o profissional abre o pagamento e confirma (PAGO) ou recusa (volta a PENDENTE).
 *
 * "Pagar depois" registra o pagamento como PENDENTE.
 *
 * O que aparece depende do perfil de quem abre a tela:
 * - CLIENTE: pagar, alterar e excluir (só enquanto PENDENTE).
 * - PROFISSIONAL: confirmar recebimento / não recebi (dinheiro) e estornar. Não altera dados.
 */
public class PagamentoFormController {

    private static final int TAMANHO_QR = 220;

    private final Repositorio<Pagamento> repositorio;
    private final Pagamento pagamento;
    private final TipoPerfil perfil;
    private final int contratacaoId;
    private final int idUsuario;      // usuário cadastrado que está pagando (ligação com Usuario/Perfil)
    private final String nomeServico;
    private final BigDecimal valor;   // preço do serviço: vem do Serviço, o usuário não digita
    private final String txid = Pix.gerarTxid();  // identificador do QR Code exibido nesta tela

    private Stage stage;
    private final TextField txtNomePagador = new TextField();
    private final TextField txtCpfPagador = new TextField();
    private final TextField txtGorjeta = new TextField();
    private final Label lblTotal = new Label();
    private final ComboBox<FormaPagamento> cbForma = new ComboBox<>();
    private final Button btnPagar = new Button();

    // Área do Pix
    private final VBox areaPix = new VBox(8);
    private final ImageView imgQrCode = new ImageView();
    private final TextField txtCopiaECola = new TextField();
    private final Label lblRecebedor = new Label();

    // Área do dinheiro
    private final VBox areaDinheiro = new VBox(8);
    private final Label lblTotalDinheiro = new Label();
    private final TextField txtValorPago = new TextField();
    private final Label lblDiferenca = new Label();

    /** Modo inclusão: o usuário escolhido (cliente) paga o serviço da contratação informada. */
    public PagamentoFormController(Repositorio<Pagamento> repositorio, int contratacaoId,
                             String nomeServico, BigDecimal valorServico, Usuario usuario) {
        this.repositorio = repositorio;
        this.pagamento = null;
        this.perfil = TipoPerfil.CLIENTE;   // só o cliente realiza pagamento
        this.contratacaoId = contratacaoId;
        this.nomeServico = nomeServico;
        this.valor = valorServico;
        this.idUsuario = usuario.getId();
    }

    /** Modo consulta: abre um pagamento existente, com as ações permitidas ao perfil. */
    public PagamentoFormController(Repositorio<Pagamento> repositorio, Pagamento pagamento, TipoPerfil perfil) {
        this.repositorio = repositorio;
        this.pagamento = pagamento;
        this.perfil = perfil;
        this.contratacaoId = pagamento.getContratacaoId();
        this.nomeServico = pagamento.getNomeServico();
        this.valor = pagamento.getValor();
        this.idUsuario = pagamento.getIdUsuario();
    }

    /**
     * Texto "Nome do usuário – cidade", buscando o Usuario e o Perfil cadastrados
     * (classes Usuario e Perfil, do cadastro de usuários do grupo).
     */
    public static String descreverUsuario(int idUsuario) {
        for (Usuario u : ArquivosUsuario.lerLista()) {
            if (u.getId() == idUsuario) {
                for (Perfil p : ArquivoPerfil.lerLista()) {
                    if (p.getIdUsuario() == idUsuario) {
                        return u.getNome() + " – " + p.getCidade();
                    }
                }
                return u.getNome();
            }
        }
        return idUsuario > 0 ? "usuário #" + idUsuario : "não informado";
    }

    private boolean isInclusao() {
        return pagamento == null;
    }

    private boolean isCliente() {
        return perfil == TipoPerfil.CLIENTE;
    }

    /** Só o cliente paga, e só se for inclusão ou um pagamento ainda pendente. */
    private boolean podePagar() {
        return isCliente()
                && (isInclusao() || pagamento.getStatusPagamento() == StatusPagamento.PENDENTE);
    }

    public void mostrar() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(isInclusao() ? "Realizar pagamento" : "Pagamento #" + pagamento.getId());

        configurarCampos();

        // Cabeçalho: serviço e valor (vêm da contratação)
        Label lblServico = Estilo.titulo("Serviço: " + nomeServico);
        Label lblValor = new Label("Valor do serviço: " + Conversor.formatarMoeda(valor));
        Label lblUsuario = new Label("Usuário: " + descreverUsuario(idUsuario));

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.addRow(0, new Label("Nome do pagador:"), txtNomePagador);
        grade.addRow(1, new Label("CPF do pagador:"), txtCpfPagador);
        grade.addRow(2, new Label("Gorjeta (opcional):"), txtGorjeta);
        grade.addRow(3, new Label("Forma de pagamento:"), cbForma);
        grade.addRow(4, new Label("Total a pagar:"), lblTotal);
        txtNomePagador.setPrefColumnCount(20);
        lblTotal.setStyle("-fx-font-weight: bold;");

        VBox raiz = new VBox(12, lblServico, lblValor, lblUsuario, new Separator(), grade);

        if (podePagar()) {
            montarAreaPix();
            montarAreaDinheiro();
            raiz.getChildren().addAll(areaPix, areaDinheiro);
        }
        if (!isInclusao()) {
            preencherCampos();
            raiz.getChildren().addAll(new Separator(), criarDetalhes());
            // Só o cliente altera, e só enquanto estiver pendente
            boolean editavel = isCliente() && pagamento.podeEditar();
            txtNomePagador.setDisable(!editavel);
            txtCpfPagador.setDisable(!editavel);
            txtGorjeta.setDisable(!editavel);
            cbForma.setDisable(!editavel);
        }
        atualizarTotal();
        raiz.getChildren().add(criarBotoes());
        raiz.setPadding(new Insets(20));

        atualizarAreaPorForma();

        stage.setScene(new Scene(raiz));
        stage.showAndWait();
    }

    private void configurarCampos() {
        txtNomePagador.setPromptText("Nome completo");
        txtCpfPagador.setPromptText("000.000.000-00");
        txtGorjeta.setPromptText("0,00");
        txtGorjeta.textProperty().addListener((obs, antigo, novo) -> atualizarTotal());
        cbForma.getItems().addAll(FormaPagamento.values());
        cbForma.setPromptText("Selecione");
        cbForma.valueProperty().addListener((obs, antigo, novo) -> atualizarAreaPorForma());
    }

    /** Gorjeta digitada: vazio = zero; texto inválido = null (só para o cálculo ao vivo). */
    private BigDecimal lerGorjetaOuNull() {
        try {
            return Conversor.paraDecimalOpcional(txtGorjeta.getText(), "Gorjeta");
        } catch (ValidacaoException e) {
            return null;
        }
    }

    /**
     * Recalcula o total (serviço + gorjeta) a cada tecla e atualiza tudo que depende dele:
     * o rótulo do total, o QR Code do Pix e o valor pago sugerido no dinheiro.
     */
    private void atualizarTotal() {
        BigDecimal gorjeta = lerGorjetaOuNull();
        if (gorjeta == null || gorjeta.compareTo(BigDecimal.ZERO) < 0) {
            lblTotal.setText("Gorjeta inválida");
            lblTotal.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            return;
        }
        BigDecimal total = valor.add(gorjeta);
        lblTotal.setText(Conversor.formatarMoeda(total));
        lblTotal.setStyle("-fx-font-weight: bold;");

        if (podePagar()) {
            atualizarPix(total);
            lblTotalDinheiro.setText("Total a pagar: " + Conversor.formatarMoeda(total));
            txtValorPago.setText(Conversor.formatarDecimal(total));
        }
    }

    /** QR Code + copia e cola, gerados com o total e a chave Pix do recebedor. */
    private void montarAreaPix() {
        txtCopiaECola.setEditable(false);
        txtCopiaECola.setPrefColumnCount(30);

        Button btnCopiar = new Button("Copiar");
        btnCopiar.setOnAction(e -> {
            ClipboardContent conteudo = new ClipboardContent();
            conteudo.putString(txtCopiaECola.getText());
            Clipboard.getSystemClipboard().setContent(conteudo);
            btnCopiar.setText("Copiado!");
        });

        Label lblInstrucao = new Label("Escaneie o QR Code no app do seu banco ou use o Pix copia e cola:");

        HBox copiaECola = new HBox(8, txtCopiaECola, btnCopiar);
        copiaECola.setAlignment(Pos.CENTER);

        areaPix.getChildren().addAll(new Separator(), lblInstrucao, imgQrCode, copiaECola, lblRecebedor);
        areaPix.setAlignment(Pos.CENTER);
    }

    /** Gera de novo o código e a imagem do QR Code com o total informado. */
    private void atualizarPix(BigDecimal total) {
        String copiaECola = Pix.gerarCopiaECola(total, txid);
        try {
            imgQrCode.setImage(QrCode.gerar(copiaECola, TAMANHO_QR));
        } catch (WriterException e) {
            Alertas.erro("Não foi possível gerar o QR Code: " + e.getMessage());
        }
        txtCopiaECola.setText(copiaECola);
        lblRecebedor.setText("Recebedor: " + Pix.NOME_RECEBEDOR + "  •  " + Conversor.formatarMoeda(total));
    }

    /** Valor pago em dinheiro: começa com o total; o cliente pode informar um valor maior. */
    private void montarAreaDinheiro() {
        Label lblInstrucao = new Label("Pague o profissional em mãos e informe abaixo quanto você entregou.");

        txtValorPago.setPromptText("Ex.: 150,00");
        txtValorPago.textProperty().addListener((obs, antigo, novo) -> atualizarDiferenca());

        Label lblAviso = new Label("Depois de informar, o profissional precisa confirmar que recebeu.");

        HBox linhaValor = new HBox(10, new Label("Valor pago (R$):"), txtValorPago, lblDiferenca);
        linhaValor.setAlignment(Pos.CENTER_LEFT);

        areaDinheiro.getChildren().addAll(new Separator(), lblInstrucao, lblTotalDinheiro, linhaValor, lblAviso);
    }

    /** Mostra, enquanto o cliente digita, se o valor é exato, a mais ou insuficiente. */
    private void atualizarDiferenca() {
        BigDecimal gorjeta = lerGorjetaOuNull();
        BigDecimal pago;
        try {
            pago = Conversor.paraDecimal(txtValorPago.getText(), "");
        } catch (ValidacaoException e) {
            lblDiferenca.setText("");
            return;
        }
        if (gorjeta == null) {
            lblDiferenca.setText("");
            return;
        }
        BigDecimal total = valor.add(gorjeta);
        int comparacao = pago.compareTo(total);
        if (comparacao == 0) {
            lblDiferenca.setText("Valor exato");
            lblDiferenca.setStyle("-fx-text-fill: green;");
        } else if (comparacao > 0) {
            lblDiferenca.setText("Você pagou " + Conversor.formatarMoeda(pago.subtract(total)) + " a mais");
            lblDiferenca.setStyle("-fx-text-fill: blue;");
        } else {
            lblDiferenca.setText("Menor que o total a pagar");
            lblDiferenca.setStyle("-fx-text-fill: red;");
        }
    }

    /** Mostra só a área da forma escolhida e ajusta o texto do botão principal. */
    private void atualizarAreaPorForma() {
        FormaPagamento forma = cbForma.getValue();
        mostrar(areaPix, forma == FormaPagamento.PIX);
        mostrar(areaDinheiro, forma == FormaPagamento.DINHEIRO);

        btnPagar.setText(forma == FormaPagamento.DINHEIRO ? "Informar pagamento" : "Já paguei");
        btnPagar.setDisable(forma == null);

        if (stage != null && stage.getScene() != null) {
            stage.sizeToScene();
        }
    }

    /** setVisible esconde; setManaged faz o layout não reservar espaço para o componente. */
    private void mostrar(VBox area, boolean visivel) {
        area.setVisible(visivel);
        area.setManaged(visivel);
    }

    private void preencherCampos() {
        txtNomePagador.setText(pagamento.getNomePagador());
        txtCpfPagador.setText(Conversor.formatarCpf(pagamento.getCpfPagador()));
        if (pagamento.getGorjeta().compareTo(BigDecimal.ZERO) > 0) {
            txtGorjeta.setText(Conversor.formatarDecimal(pagamento.getGorjeta()));
        }
        cbForma.setValue(pagamento.getFormaPagamento());
    }

    /** Informações do pagamento já registrado (modo consulta). */
    private GridPane criarDetalhes() {
        GridPane detalhes = new GridPane();
        detalhes.setHgap(10);
        detalhes.setVgap(8);
        int linha = 0;
        Label lblStatus = new Label(pagamento.getStatusPagamento().toString());
        Estilo.corDoStatus(lblStatus, pagamento.getStatusPagamento());
        detalhes.addRow(linha++, new Label("Status:"), lblStatus);
        detalhes.addRow(linha++, new Label("Criado em:"), new Label(Conversor.formatarData(pagamento.getDataCriacao())));
        if (pagamento.getGorjeta().compareTo(BigDecimal.ZERO) > 0) {
            detalhes.addRow(linha++, new Label("Gorjeta:"), new Label(Conversor.formatarMoeda(pagamento.getGorjeta())));
        }
        detalhes.addRow(linha++, new Label("Total:"), new Label(Conversor.formatarMoeda(pagamento.getValorTotal())));
        if (pagamento.getDataPagamento() != null) {
            detalhes.addRow(linha++, new Label("Pago em:"), new Label(Conversor.formatarData(pagamento.getDataPagamento())));
        }
        if (pagamento.getValorPago() != null) {
            String texto = Conversor.formatarMoeda(pagamento.getValorPago());
            if (pagamento.getValorExcedente().compareTo(BigDecimal.ZERO) > 0) {
                texto += "  (" + Conversor.formatarMoeda(pagamento.getValorExcedente()) + " a mais)";
            }
            detalhes.addRow(linha++, new Label("Valor pago:"), new Label(texto));
        }
        if (pagamento.getCodigoTransacao() != null) {
            detalhes.addRow(linha, new Label("Código Pix:"), new Label(pagamento.getCodigoTransacao()));
        }
        return detalhes;
    }

    /** Os botões dependem do perfil, do modo (inclusão/consulta) e do status do pagamento. */
    private VBox criarBotoes() {
        HBox botoes = new HBox(10);
        Label lblAviso = new Label();
        StatusPagamento status = isInclusao() ? null : pagamento.getStatusPagamento();

        if (isCliente()) {
            if (podePagar()) {
                btnPagar.setOnAction(e -> pagar());
                botoes.getChildren().add(btnPagar);
            }
            if (isInclusao()) {
                Button btnDepois = new Button("Pagar depois");
                btnDepois.setOnAction(e -> pagarDepois());
                botoes.getChildren().add(btnDepois);
            } else if (status == StatusPagamento.PENDENTE) {
                Button btnSalvar = new Button("Salvar alterações");
                btnSalvar.setOnAction(e -> salvarAlteracoes());
                Button btnExcluir = new Button("Excluir");
                btnExcluir.setOnAction(e -> excluir());
                botoes.getChildren().addAll(btnSalvar, btnExcluir);
            } else if (status == StatusPagamento.AGUARDANDO_CONFIRMACAO) {
                lblAviso.setText("Aguardando o profissional confirmar que recebeu o pagamento.");
            }
        } else {
            // PROFISSIONAL
            if (status == StatusPagamento.AGUARDANDO_CONFIRMACAO) {
                Button btnConfirmar = new Button("Confirmar recebimento");
                btnConfirmar.setOnAction(e -> confirmarRecebimento());
                Button btnRecusar = new Button("Não recebi");
                btnRecusar.setOnAction(e -> recusarRecebimento());
                botoes.getChildren().addAll(btnConfirmar, btnRecusar);
            } else if (status == StatusPagamento.PAGO) {
                Button btnEstornar = new Button("Estornar");
                btnEstornar.setOnAction(e -> estornar());
                botoes.getChildren().add(btnEstornar);
            } else if (status == StatusPagamento.PENDENTE) {
                lblAviso.setText("O cliente ainda não realizou este pagamento.");
            }
        }

        Button btnFechar = new Button(isInclusao() ? "Cancelar" : "Fechar");
        btnFechar.setOnAction(e -> stage.close());
        botoes.getChildren().add(btnFechar);

        VBox rodape = new VBox(8, botoes);
        if (!lblAviso.getText().isEmpty()) {
            rodape.getChildren().add(0, lblAviso);
        }
        return rodape;
    }

    // ------------------------------------------------------------------
    // Ações
    // ------------------------------------------------------------------

    /** Na inclusão cria o pagamento; na consulta usa o pendente, aplicando o que estiver na tela. */
    private Pagamento obterPagamentoComDadosDaTela() throws ValidacaoException {
        if (isInclusao()) {
            // contratacaoId, nomeServico e valor vêm do serviço contratado, não do usuário
            return new Pagamento(contratacaoId, idUsuario, nomeServico, txtNomePagador.getText(),
                    txtCpfPagador.getText(), valor, lerGorjeta(), cbForma.getValue());
        }
        aplicarCampos();
        return pagamento;
    }

    private BigDecimal lerGorjeta() throws ValidacaoException {
        return Conversor.paraDecimalOpcional(txtGorjeta.getText(), "Gorjeta");
    }

    private void aplicarCampos() throws ValidacaoException {
        pagamento.setNomePagador(txtNomePagador.getText());
        pagamento.setCpfPagador(txtCpfPagador.getText());
        pagamento.setGorjeta(lerGorjeta());
        pagamento.setFormaPagamento(cbForma.getValue());
    }

    /** "Já paguei" (Pix) ou "Informar pagamento" (dinheiro). */
    private void pagar() {
        try {
            Pagamento p = obterPagamentoComDadosDaTela();

            if (p.getFormaPagamento() == FormaPagamento.PIX) {
                if (!Alertas.confirmar("Você já fez o Pix de " + Conversor.formatarMoeda(p.getValorTotal()) + " no app do banco?")) {
                    return;
                }
                p.pagarComPix(LocalDate.now(), txid);                    // PENDENTE -> PAGO
            } else {
                BigDecimal valorPago = Conversor.paraDecimal(txtValorPago.getText(), "Valor pago");
                if (!Alertas.confirmar("Confirma que pagou " + Conversor.formatarMoeda(valorPago)
                        + " em dinheiro ao profissional?")) {
                    return;
                }
                p.informarPagamentoEmDinheiro(LocalDate.now(), valorPago); // PENDENTE -> AGUARDANDO
            }

            if (isInclusao()) {
                repositorio.inserir(p);
            } else {
                repositorio.atualizar(p);
            }
            Alertas.info(montarComprovante(p));
            stage.close();
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao salvar: " + e.getMessage());
        }
    }

    /** Registra o pagamento como PENDENTE, para pagar depois. */
    private void pagarDepois() {
        try {
            repositorio.inserir(obterPagamentoComDadosDaTela());
            Alertas.info("Pagamento registrado como pendente.\nAbra-o na tabela quando for pagar.");
            stage.close();
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void salvarAlteracoes() {
        try {
            aplicarCampos();
            repositorio.atualizar(pagamento);
            Alertas.info("Pagamento alterado com sucesso!");
            stage.close();
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void confirmarRecebimento() {
        if (!Alertas.confirmar("Confirmar que você (profissional) recebeu "
                + Conversor.formatarMoeda(pagamento.getValorPago()) + " em dinheiro?")) {
            return;
        }
        try {
            pagamento.confirmarRecebimento();     // AGUARDANDO -> PAGO
            repositorio.atualizar(pagamento);
            Alertas.info("Recebimento confirmado. Pagamento concluído!");
            stage.close();
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void recusarRecebimento() {
        if (!Alertas.confirmar("Informar que você NÃO recebeu este pagamento? Ele voltará a ficar pendente.")) {
            return;
        }
        try {
            pagamento.recusarRecebimento();       // AGUARDANDO -> PENDENTE
            repositorio.atualizar(pagamento);
            Alertas.info("Pagamento voltou para pendente.");
            stage.close();
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void estornar() {
        if (!Alertas.confirmar("Estornar este pagamento? O valor será devolvido ao cliente e a ação não pode ser desfeita.")) {
            return;
        }
        try {
            pagamento.estornar();                  // PAGO -> ESTORNADO
            repositorio.atualizar(pagamento);
            Alertas.info("Pagamento estornado.");
            stage.close();
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void excluir() {
        if (!Alertas.confirmar("Deseja realmente excluir o pagamento #" + pagamento.getId() + "?")) {
            return;
        }
        try {
            if (!pagamento.podeExcluir()) {
                throw new ValidacaoException("Só pagamentos pendentes podem ser excluídos.");
            }
            repositorio.excluir(pagamento.getId());
            Alertas.info("Pagamento excluído.");
            stage.close();
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao excluir: " + e.getMessage());
        }
    }

    /** Texto do comprovante mostrado após o pagamento. */
    private String montarComprovante(Pagamento p) {
        StringBuilder texto = new StringBuilder();
        texto.append(p.getStatusPagamento() == StatusPagamento.PAGO
                ? "Pagamento realizado com sucesso!\n\n"
                : "Pagamento informado! Aguardando o profissional confirmar o recebimento.\n\n");
        texto.append("Serviço: ").append(p.getNomeServico()).append('\n');
        texto.append("Pagador: ").append(p.getNomePagador())
                .append(" (CPF ").append(Conversor.formatarCpf(p.getCpfPagador())).append(")\n");
        texto.append("Valor do serviço: ").append(Conversor.formatarMoeda(p.getValor())).append('\n');
        if (p.getGorjeta().compareTo(BigDecimal.ZERO) > 0) {
            texto.append("Gorjeta: ").append(Conversor.formatarMoeda(p.getGorjeta())).append('\n');
        }
        texto.append("Total: ").append(Conversor.formatarMoeda(p.getValorTotal())).append('\n');
        texto.append("Forma: ").append(p.getFormaPagamento()).append('\n');
        if (p.getValorPago() != null) {
            texto.append("Valor pago: ").append(Conversor.formatarMoeda(p.getValorPago())).append('\n');
        }
        if (p.getCodigoTransacao() != null) {
            texto.append("Código Pix: ").append(p.getCodigoTransacao()).append('\n');
        }
        texto.append("Data: ").append(Conversor.formatarData(p.getDataPagamento()));
        return texto.toString();
    }
}
