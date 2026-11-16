package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.arquivos.ArquivoPerfil;
import br.pucpr.trabalho.arquivos.ArquivosUsuario;
import br.pucpr.trabalho.arquivos.PersistenciaException;
import br.pucpr.trabalho.arquivos.Repositorio;
import br.pucpr.trabalho.domains.FormaPagamento;
import br.pucpr.trabalho.domains.Pagamento;
import br.pucpr.trabalho.domains.Perfil;
import br.pucpr.trabalho.domains.Pix;
import br.pucpr.trabalho.domains.StatusPagamento;
import br.pucpr.trabalho.domains.TipoPerfil;
import br.pucpr.trabalho.domains.Usuario;
import br.pucpr.trabalho.domains.ValidacaoException;
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

// Se pagamento for null a tela é de inclusão; senão é de consulta
public class PagamentoFormController {

    private Repositorio<Pagamento> repositorio;
    private Pagamento pagamento;
    private TipoPerfil perfil;
    private int contratacaoId;
    private int idUsuario;
    private String nomeServico;
    private BigDecimal valor;
    private String txid = Pix.gerarTxid();

    private Stage stage;
    private TextField txtNomePagador = new TextField();
    private TextField txtCpfPagador = new TextField();
    private TextField txtGorjeta = new TextField();
    private Label lblTotal = new Label();
    private ComboBox<FormaPagamento> cbForma = new ComboBox<>();
    private Button btnPagar = new Button();

    private VBox areaPix = new VBox(8);
    private ImageView imgQrCode = new ImageView();
    private TextField txtCopiaECola = new TextField();
    private Label lblRecebedor = new Label();

    private VBox areaDinheiro = new VBox(8);
    private Label lblTotalDinheiro = new Label();
    private TextField txtValorPago = new TextField();
    private Label lblDiferenca = new Label();

    public PagamentoFormController(Repositorio<Pagamento> repositorio, int contratacaoId,
                                   String nomeServico, BigDecimal valorServico, Usuario usuario) {
        this.repositorio = repositorio;
        this.perfil = TipoPerfil.CLIENTE;
        this.contratacaoId = contratacaoId;
        this.nomeServico = nomeServico;
        this.valor = valorServico;
        this.idUsuario = usuario.getId();
    }

    public PagamentoFormController(Repositorio<Pagamento> repositorio, Pagamento pagamento, TipoPerfil perfil) {
        this.repositorio = repositorio;
        this.pagamento = pagamento;
        this.perfil = perfil;
        this.contratacaoId = pagamento.getContratacaoId();
        this.nomeServico = pagamento.getNomeServico();
        this.valor = pagamento.getValor();
        this.idUsuario = pagamento.getIdUsuario();
    }

    // "Nome - cidade" usando o cadastro de Usuario e Perfil
    public static String descreverUsuario(int idUsuario) {
        for (Usuario u : ArquivosUsuario.lerLista()) {
            if (u.getId() == idUsuario) {
                for (Perfil p : ArquivoPerfil.lerLista()) {
                    if (p.getIdUsuario() == idUsuario) {
                        return u.getNome() + " - " + p.getCidade();
                    }
                }
                return u.getNome();
            }
        }
        return "não informado";
    }

    // Só o cliente paga, e só se for um pagamento novo ou pendente
    private boolean podePagar() {
        if (perfil != TipoPerfil.CLIENTE) {
            return false;
        }
        return pagamento == null || pagamento.getStatusPagamento() == StatusPagamento.PENDENTE;
    }

    public void mostrar() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (pagamento == null) {
            stage.setTitle("Realizar pagamento");
        } else {
            stage.setTitle("Pagamento #" + pagamento.getId());
        }

        txtNomePagador.setPromptText("Nome completo");
        txtNomePagador.setPrefColumnCount(20);
        txtCpfPagador.setPromptText("000.000.000-00");
        txtGorjeta.setPromptText("0,00");
        txtGorjeta.textProperty().addListener((obs, antigo, novo) -> atualizarTotal());
        cbForma.getItems().addAll(FormaPagamento.values());
        cbForma.setPromptText("Selecione");
        cbForma.valueProperty().addListener((obs, antigo, novo) -> atualizarAreaPorForma());
        lblTotal.setStyle("-fx-font-weight: bold;");

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.addRow(0, new Label("Nome do pagador:"), txtNomePagador);
        grade.addRow(1, new Label("CPF do pagador:"), txtCpfPagador);
        grade.addRow(2, new Label("Gorjeta (opcional):"), txtGorjeta);
        grade.addRow(3, new Label("Forma de pagamento:"), cbForma);
        grade.addRow(4, new Label("Total a pagar:"), lblTotal);

        VBox raiz = new VBox(12,
                Estilo.titulo("Serviço: " + nomeServico),
                new Label("Valor do serviço: " + Conversor.formatarMoeda(valor)),
                new Label("Usuário: " + descreverUsuario(idUsuario)),
                new Separator(), grade);

        if (podePagar()) {
            montarAreaPix();
            montarAreaDinheiro();
            raiz.getChildren().add(areaPix);
            raiz.getChildren().add(areaDinheiro);
        }

        if (pagamento != null) {
            preencherCampos();
            raiz.getChildren().add(new Separator());
            raiz.getChildren().add(criarDetalhes());

            boolean podeAlterar = perfil == TipoPerfil.CLIENTE && pagamento.podeEditar();
            txtNomePagador.setDisable(!podeAlterar);
            txtCpfPagador.setDisable(!podeAlterar);
            txtGorjeta.setDisable(!podeAlterar);
            cbForma.setDisable(!podeAlterar);
        }

        atualizarTotal();
        raiz.getChildren().add(criarBotoes());
        raiz.setPadding(new Insets(20));
        atualizarAreaPorForma();

        stage.setScene(new Scene(raiz));
        stage.showAndWait();
    }

    // Lê a gorjeta para o cálculo da tela; devolve null se o texto for inválido
    private BigDecimal lerGorjetaOuNull() {
        try {
            return Conversor.paraDecimalOpcional(txtGorjeta.getText(), "Gorjeta");
        } catch (ValidacaoException e) {
            return null;
        }
    }

    // Recalcula o total a cada tecla (atualiza também o QR Code e o valor do dinheiro)
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

        HBox linhaCopiar = new HBox(8, txtCopiaECola, btnCopiar);
        linhaCopiar.setAlignment(Pos.CENTER);

        areaPix.getChildren().addAll(new Separator(),
                new Label("Escaneie o QR Code no app do seu banco ou use o Pix copia e cola:"),
                imgQrCode, linhaCopiar, lblRecebedor);
        areaPix.setAlignment(Pos.CENTER);
    }

    private void atualizarPix(BigDecimal total) {
        String copiaECola = Pix.gerarCopiaECola(total, txid);
        try {
            imgQrCode.setImage(QrCode.gerar(copiaECola, 220));
        } catch (WriterException e) {
            Alertas.erro("Não foi possível gerar o QR Code.");
        }
        txtCopiaECola.setText(copiaECola);
        lblRecebedor.setText("Recebedor: " + Pix.NOME_RECEBEDOR + " | " + Conversor.formatarMoeda(total));
    }

    private void montarAreaDinheiro() {
        txtValorPago.setPromptText("Ex.: 150,00");
        txtValorPago.textProperty().addListener((obs, antigo, novo) -> atualizarDiferenca());

        HBox linhaValor = new HBox(10, new Label("Valor pago (R$):"), txtValorPago, lblDiferenca);
        linhaValor.setAlignment(Pos.CENTER_LEFT);

        areaDinheiro.getChildren().addAll(new Separator(),
                new Label("Pague o profissional em mãos e informe abaixo quanto você entregou."),
                lblTotalDinheiro, linhaValor,
                new Label("Depois de informar, o profissional precisa confirmar que recebeu."));
    }

    // Mostra se o valor pago em dinheiro é exato, maior ou menor que o total
    private void atualizarDiferenca() {
        BigDecimal gorjeta = lerGorjetaOuNull();
        BigDecimal pago;
        try {
            pago = Conversor.paraDecimal(txtValorPago.getText(), "Valor pago");
        } catch (ValidacaoException e) {
            lblDiferenca.setText("");
            return;
        }
        if (gorjeta == null) {
            lblDiferenca.setText("");
            return;
        }
        BigDecimal total = valor.add(gorjeta);
        if (pago.compareTo(total) == 0) {
            lblDiferenca.setText("Valor exato");
            lblDiferenca.setStyle("-fx-text-fill: green;");
        } else if (pago.compareTo(total) > 0) {
            lblDiferenca.setText("Você pagou " + Conversor.formatarMoeda(pago.subtract(total)) + " a mais");
            lblDiferenca.setStyle("-fx-text-fill: blue;");
        } else {
            lblDiferenca.setText("Menor que o total a pagar");
            lblDiferenca.setStyle("-fx-text-fill: red;");
        }
    }

    // Mostra só a área da forma escolhida (setManaged false faz a área não ocupar espaço)
    private void atualizarAreaPorForma() {
        FormaPagamento forma = cbForma.getValue();
        areaPix.setVisible(forma == FormaPagamento.PIX);
        areaPix.setManaged(forma == FormaPagamento.PIX);
        areaDinheiro.setVisible(forma == FormaPagamento.DINHEIRO);
        areaDinheiro.setManaged(forma == FormaPagamento.DINHEIRO);

        if (forma == FormaPagamento.DINHEIRO) {
            btnPagar.setText("Informar pagamento");
        } else {
            btnPagar.setText("Já paguei");
        }
        btnPagar.setDisable(forma == null);

        if (stage != null && stage.getScene() != null) {
            stage.sizeToScene();
        }
    }

    private void preencherCampos() {
        txtNomePagador.setText(pagamento.getNomePagador());
        txtCpfPagador.setText(Conversor.formatarCpf(pagamento.getCpfPagador()));
        if (pagamento.getGorjeta().compareTo(BigDecimal.ZERO) > 0) {
            txtGorjeta.setText(Conversor.formatarDecimal(pagamento.getGorjeta()));
        }
        cbForma.setValue(pagamento.getFormaPagamento());
    }

    private GridPane criarDetalhes() {
        GridPane detalhes = new GridPane();
        detalhes.setHgap(10);
        detalhes.setVgap(8);

        Label lblStatus = new Label(pagamento.getStatusPagamento().toString());
        Estilo.corDoStatus(lblStatus, pagamento.getStatusPagamento());

        int linha = 0;
        detalhes.addRow(linha++, new Label("Status:"), lblStatus);
        detalhes.addRow(linha++, new Label("Criado em:"), new Label(Conversor.formatarData(pagamento.getDataCriacao())));
        detalhes.addRow(linha++, new Label("Total:"), new Label(Conversor.formatarMoeda(pagamento.getValorTotal())));
        if (pagamento.getDataPagamento() != null) {
            detalhes.addRow(linha++, new Label("Pago em:"), new Label(Conversor.formatarData(pagamento.getDataPagamento())));
        }
        if (pagamento.getValorPago() != null) {
            detalhes.addRow(linha++, new Label("Valor pago:"), new Label(Conversor.formatarMoeda(pagamento.getValorPago())));
        }
        if (pagamento.getCodigoTransacao() != null) {
            detalhes.addRow(linha++, new Label("Código Pix:"), new Label(pagamento.getCodigoTransacao()));
        }
        return detalhes;
    }

    // Os botões mudam conforme o perfil (cliente ou profissional) e o status do pagamento
    private VBox criarBotoes() {
        HBox botoes = new HBox(10);
        Label lblAviso = new Label();
        StatusPagamento status = null;
        if (pagamento != null) {
            status = pagamento.getStatusPagamento();
        }

        if (perfil == TipoPerfil.CLIENTE) {
            if (podePagar()) {
                btnPagar.setOnAction(e -> pagar());
                botoes.getChildren().add(btnPagar);
            }
            if (pagamento == null) {
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

        Button btnFechar = new Button("Fechar");
        btnFechar.setOnAction(e -> stage.close());
        botoes.getChildren().add(btnFechar);

        VBox rodape = new VBox(8, botoes);
        if (!lblAviso.getText().isEmpty()) {
            rodape.getChildren().add(0, lblAviso);
        }
        return rodape;
    }

    // Cria o pagamento novo ou aplica os dados da tela no pagamento pendente
    private Pagamento pegarPagamentoDaTela() throws ValidacaoException {
        BigDecimal gorjeta = Conversor.paraDecimalOpcional(txtGorjeta.getText(), "Gorjeta");
        if (pagamento == null) {
            return new Pagamento(contratacaoId, idUsuario, nomeServico, txtNomePagador.getText(),
                    txtCpfPagador.getText(), valor, gorjeta, cbForma.getValue());
        }
        pagamento.setNomePagador(txtNomePagador.getText());
        pagamento.setCpfPagador(txtCpfPagador.getText());
        pagamento.setGorjeta(gorjeta);
        pagamento.setFormaPagamento(cbForma.getValue());
        return pagamento;
    }

    private void salvarNoArquivo(Pagamento p) throws PersistenciaException {
        if (pagamento == null) {
            repositorio.inserir(p);
        } else {
            repositorio.atualizar(p);
        }
    }

    private void pagar() {
        try {
            Pagamento p = pegarPagamentoDaTela();

            if (p.getFormaPagamento() == FormaPagamento.PIX) {
                if (!Alertas.confirmar("Você já fez o Pix de " + Conversor.formatarMoeda(p.getValorTotal()) + " no app do banco?")) {
                    return;
                }
                p.pagarComPix(LocalDate.now(), txid);
            } else {
                BigDecimal valorPago = Conversor.paraDecimal(txtValorPago.getText(), "Valor pago");
                if (!Alertas.confirmar("Confirma que pagou " + Conversor.formatarMoeda(valorPago) + " em dinheiro ao profissional?")) {
                    return;
                }
                p.informarPagamentoEmDinheiro(LocalDate.now(), valorPago);
            }

            salvarNoArquivo(p);
            Alertas.info(montarComprovante(p));
            stage.close();
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void pagarDepois() {
        try {
            salvarNoArquivo(pegarPagamentoDaTela());
            Alertas.info("Pagamento registrado como pendente.");
            stage.close();
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void salvarAlteracoes() {
        try {
            salvarNoArquivo(pegarPagamentoDaTela());
            Alertas.info("Pagamento alterado com sucesso!");
            stage.close();
        } catch (ValidacaoException e) {
            Alertas.erro(e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void confirmarRecebimento() {
        if (!Alertas.confirmar("Confirmar que você recebeu " + Conversor.formatarMoeda(pagamento.getValorPago()) + " em dinheiro?")) {
            return;
        }
        try {
            pagamento.confirmarRecebimento();
            repositorio.atualizar(pagamento);
            Alertas.info("Recebimento confirmado!");
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
            pagamento.recusarRecebimento();
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
        if (!Alertas.confirmar("Estornar este pagamento? O valor será devolvido ao cliente.")) {
            return;
        }
        try {
            pagamento.estornar();
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
            repositorio.excluir(pagamento.getId());
            Alertas.info("Pagamento excluído.");
            stage.close();
        } catch (PersistenciaException e) {
            Alertas.erro("Erro ao excluir: " + e.getMessage());
        }
    }

    private String montarComprovante(Pagamento p) {
        String texto;
        if (p.getStatusPagamento() == StatusPagamento.PAGO) {
            texto = "Pagamento realizado com sucesso!\n\n";
        } else {
            texto = "Pagamento informado! Aguardando o profissional confirmar.\n\n";
        }
        texto += "Serviço: " + p.getNomeServico() + "\n";
        texto += "Pagador: " + p.getNomePagador() + " (CPF " + Conversor.formatarCpf(p.getCpfPagador()) + ")\n";
        texto += "Total: " + Conversor.formatarMoeda(p.getValorTotal()) + "\n";
        texto += "Forma: " + p.getFormaPagamento() + "\n";
        if (p.getCodigoTransacao() != null) {
            texto += "Código Pix: " + p.getCodigoTransacao() + "\n";
        }
        texto += "Data: " + Conversor.formatarData(p.getDataPagamento());
        return texto;
    }
}
