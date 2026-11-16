package br.pucpr.trabalho.domains;

import br.pucpr.trabalho.arquivos.Identificavel;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Pagamento implements Identificavel {

    private static final long serialVersionUID = 1L;

    private int id;
    private int contratacaoId;
    private int idUsuario;
    private String nomeServico;
    private String nomePagador;
    private String cpfPagador;
    private BigDecimal valor;
    private BigDecimal gorjeta;
    private FormaPagamento formaPagamento;
    private StatusPagamento statusPagamento;
    private LocalDate dataCriacao;
    private LocalDate dataPagamento;
    private BigDecimal valorPago;
    private String codigoTransacao;

    public Pagamento(int contratacaoId, int idUsuario, String nomeServico, String nomePagador, String cpfPagador,
                     BigDecimal valor, BigDecimal gorjeta, FormaPagamento formaPagamento) throws ValidacaoException {
        this.statusPagamento = StatusPagamento.PENDENTE;
        this.dataCriacao = LocalDate.now();
        setContratacaoId(contratacaoId);
        setIdUsuario(idUsuario);
        setNomeServico(nomeServico);
        setNomePagador(nomePagador);
        setCpfPagador(cpfPagador);
        setValor(valor);
        setGorjeta(gorjeta);
        setFormaPagamento(formaPagamento);
    }

    public BigDecimal getValorTotal() {
        return valor.add(getGorjeta());
    }

    // Pix: PENDENTE -> PAGO
    public void pagarComPix(LocalDate data, String txid) throws ValidacaoException {
        if (statusPagamento != StatusPagamento.PENDENTE) {
            throw new ValidacaoException("Só é possível pagar um pagamento pendente.");
        }
        if (formaPagamento != FormaPagamento.PIX) {
            throw new ValidacaoException("Este pagamento não é por Pix.");
        }
        if (txid == null || txid.isBlank() || txid.length() > 25) {
            throw new ValidacaoException("Código da transação Pix inválido.");
        }
        validarData(data);
        this.codigoTransacao = txid;
        this.dataPagamento = data;
        this.statusPagamento = StatusPagamento.PAGO;
    }

    // Dinheiro: PENDENTE -> AGUARDANDO_CONFIRMACAO
    public void informarPagamentoEmDinheiro(LocalDate data, BigDecimal valorPago) throws ValidacaoException {
        if (statusPagamento != StatusPagamento.PENDENTE) {
            throw new ValidacaoException("Só é possível informar o pagamento de um pagamento pendente.");
        }
        if (formaPagamento != FormaPagamento.DINHEIRO) {
            throw new ValidacaoException("Este pagamento não é em dinheiro.");
        }
        if (valorPago == null) {
            throw new ValidacaoException("Informe quanto você pagou ao profissional.");
        }
        if (valorPago.scale() > 2) {
            throw new ValidacaoException("O valor pago pode ter no máximo 2 casas decimais.");
        }
        if (valorPago.compareTo(getValorTotal()) < 0) {
            throw new ValidacaoException("O valor pago não pode ser menor que o total (serviço + gorjeta).");
        }
        validarData(data);
        this.valorPago = valorPago;
        this.dataPagamento = data;
        this.statusPagamento = StatusPagamento.AGUARDANDO_CONFIRMACAO;
    }

    // Profissional confirma: AGUARDANDO_CONFIRMACAO -> PAGO
    public void confirmarRecebimento() throws ValidacaoException {
        if (statusPagamento != StatusPagamento.AGUARDANDO_CONFIRMACAO) {
            throw new ValidacaoException("Este pagamento não está aguardando confirmação.");
        }
        this.statusPagamento = StatusPagamento.PAGO;
    }

    // Profissional não recebeu: AGUARDANDO_CONFIRMACAO -> PENDENTE
    public void recusarRecebimento() throws ValidacaoException {
        if (statusPagamento != StatusPagamento.AGUARDANDO_CONFIRMACAO) {
            throw new ValidacaoException("Este pagamento não está aguardando confirmação.");
        }
        this.valorPago = null;
        this.dataPagamento = null;
        this.statusPagamento = StatusPagamento.PENDENTE;
    }

    // PAGO -> ESTORNADO
    public void estornar() throws ValidacaoException {
        if (statusPagamento != StatusPagamento.PAGO) {
            throw new ValidacaoException("Só é possível estornar um pagamento que já foi pago.");
        }
        this.statusPagamento = StatusPagamento.ESTORNADO;
    }

    public boolean podeEditar() {
        return statusPagamento == StatusPagamento.PENDENTE;
    }

    public boolean podeExcluir() {
        return statusPagamento == StatusPagamento.PENDENTE;
    }

    public BigDecimal getValorExcedente() {
        if (valorPago == null) {
            return null;
        }
        return valorPago.subtract(getValorTotal());
    }

    private void verificarEditavel() throws ValidacaoException {
        if (!podeEditar()) {
            throw new ValidacaoException("Pagamento com status \"" + statusPagamento + "\" não pode ser alterado.");
        }
    }

    private void validarData(LocalDate data) throws ValidacaoException {
        if (data == null) {
            throw new ValidacaoException("Informe a data do pagamento.");
        }
        if (data.isBefore(dataCriacao)) {
            throw new ValidacaoException("A data do pagamento não pode ser anterior à criação do pagamento.");
        }
        if (data.isAfter(LocalDate.now())) {
            throw new ValidacaoException("A data do pagamento não pode estar no futuro.");
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getContratacaoId() {
        return contratacaoId;
    }

    public void setContratacaoId(int contratacaoId) throws ValidacaoException {
        if (contratacaoId <= 0) {
            throw new ValidacaoException("O ID da contratação deve ser maior que zero.");
        }
        this.contratacaoId = contratacaoId;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) throws ValidacaoException {
        if (idUsuario <= 0) {
            throw new ValidacaoException("Selecione o usuário que está realizando o pagamento.");
        }
        this.idUsuario = idUsuario;
    }

    public String getNomeServico() {
        return nomeServico;
    }

    public void setNomeServico(String nomeServico) throws ValidacaoException {
        if (nomeServico == null || nomeServico.isBlank()) {
            throw new ValidacaoException("O pagamento precisa estar ligado a um serviço.");
        }
        this.nomeServico = nomeServico.trim();
    }

    public String getNomePagador() {
        return nomePagador;
    }

    public void setNomePagador(String nomePagador) throws ValidacaoException {
        verificarEditavel();
        if (nomePagador == null || nomePagador.isBlank()) {
            throw new ValidacaoException("Informe o nome do pagador.");
        }
        nomePagador = nomePagador.trim();
        if (nomePagador.length() < 3) {
            throw new ValidacaoException("O nome do pagador deve ter pelo menos 3 letras.");
        }
        for (int i = 0; i < nomePagador.length(); i++) {
            char c = nomePagador.charAt(i);
            if (!Character.isLetter(c) && c != ' ') {
                throw new ValidacaoException("O nome do pagador deve conter apenas letras.");
            }
        }
        this.nomePagador = nomePagador;
    }

    public String getCpfPagador() {
        return cpfPagador;
    }

    // Aceita com ou sem pontuação e guarda só os 11 números
    public void setCpfPagador(String cpfPagador) throws ValidacaoException {
        verificarEditavel();
        if (cpfPagador == null || cpfPagador.isBlank()) {
            throw new ValidacaoException("Informe o CPF do pagador.");
        }
        String cpf = cpfPagador.replace(".", "").replace("-", "").trim();
        if (cpf.length() != 11) {
            throw new ValidacaoException("O CPF deve ter 11 números (ex.: 529.982.247-25).");
        }
        for (int i = 0; i < cpf.length(); i++) {
            if (!Character.isDigit(cpf.charAt(i))) {
                throw new ValidacaoException("O CPF deve ter 11 números (ex.: 529.982.247-25).");
            }
        }
        if (!cpfValido(cpf)) {
            throw new ValidacaoException("CPF inválido.");
        }
        this.cpfPagador = cpf;
    }

    // Confere os 2 dígitos verificadores do CPF
    private boolean cpfValido(String cpf) {
        boolean todosIguais = true;
        for (int i = 1; i < 11; i++) {
            if (cpf.charAt(i) != cpf.charAt(0)) {
                todosIguais = false;
            }
        }
        if (todosIguais) {
            return false;
        }
        int digito1 = calcularDigito(cpf, 9);
        int digito2 = calcularDigito(cpf, 10);
        return digito1 == cpf.charAt(9) - '0' && digito2 == cpf.charAt(10) - '0';
    }

    private int calcularDigito(String cpf, int quantidade) {
        int soma = 0;
        int peso = quantidade + 1;
        for (int i = 0; i < quantidade; i++) {
            soma += (cpf.charAt(i) - '0') * peso;
            peso--;
        }
        int resto = soma % 11;
        if (resto < 2) {
            return 0;
        }
        return 11 - resto;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) throws ValidacaoException {
        verificarEditavel();
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("O valor deve ser maior que zero.");
        }
        if (valor.scale() > 2) {
            throw new ValidacaoException("O valor pode ter no máximo 2 casas decimais.");
        }
        this.valor = valor;
    }

    public BigDecimal getGorjeta() {
        if (gorjeta == null) {
            return BigDecimal.ZERO;
        }
        return gorjeta;
    }

    // Gorjeta é opcional: vazio vale zero
    public void setGorjeta(BigDecimal gorjeta) throws ValidacaoException {
        verificarEditavel();
        if (gorjeta == null) {
            gorjeta = BigDecimal.ZERO;
        }
        if (gorjeta.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacaoException("A gorjeta não pode ser negativa.");
        }
        if (gorjeta.scale() > 2) {
            throw new ValidacaoException("A gorjeta pode ter no máximo 2 casas decimais.");
        }
        if (gorjeta.compareTo(valor) > 0) {
            throw new ValidacaoException("A gorjeta não pode ser maior que o valor do serviço.");
        }
        this.gorjeta = gorjeta;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) throws ValidacaoException {
        verificarEditavel();
        if (formaPagamento == null) {
            throw new ValidacaoException("Selecione a forma de pagamento.");
        }
        this.formaPagamento = formaPagamento;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public String getCodigoTransacao() {
        return codigoTransacao;
    }
}
