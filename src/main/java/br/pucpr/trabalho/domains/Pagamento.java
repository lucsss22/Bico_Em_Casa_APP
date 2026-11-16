// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.domains;

import br.pucpr.trabalho.arquivos.Identificavel;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pagamento de um serviço contratado, feito pelo cliente ao profissional.
 *
 * Fluxo de status:
 *
 *   PIX:       PENDENTE --pagarComPix()--> PAGO
 *
 *   DINHEIRO:  PENDENTE --informarPagamentoEmDinheiro()--> AGUARDANDO_CONFIRMACAO
 *              AGUARDANDO_CONFIRMACAO --confirmarRecebimento()--> PAGO      (profissional recebeu)
 *              AGUARDANDO_CONFIRMACAO --recusarRecebimento()----> PENDENTE  (profissional não recebeu)
 *
 *   Qualquer:  PAGO --estornar()--> ESTORNADO (final, fica como histórico)
 *
 * Regras:
 * - Todo pagamento nasce PENDENTE.
 * - Só um pagamento PENDENTE pode ter os dados alterados ou ser excluído.
 * - Em dinheiro, o cliente não pode informar que pagou menos que o valor do serviço
 *   (pode pagar a mais, por exemplo uma gorjeta).
 */
public class Pagamento implements Identificavel {

    private static final long serialVersionUID = 1L;

    private int id;
    private int contratacaoId;       // vem da Contratação, não aparece para o usuário
    private String nomeServico;      // vem do Serviço contratado, só é exibido
    private String nomePagador;      // quem está pagando (nem sempre é o cliente)
    private String cpfPagador;       // guardado só com os 11 dígitos, sem pontos e traço
    private BigDecimal valor;        // preço do serviço
    private BigDecimal gorjeta;      // opcional, escolhida pelo cliente (zero se não der gorjeta)
    private FormaPagamento formaPagamento;
    private StatusPagamento statusPagamento;
    private LocalDate dataCriacao;
    private LocalDate dataPagamento;     // quando o cliente pagou
    private BigDecimal valorPago;        // dinheiro: quanto o cliente diz que entregou ao profissional
    private String codigoTransacao;      // pix: identificador (txid) do QR Code pago

    /**
     * Cria um pagamento novo, sempre PENDENTE.
     * Cada setter valida o seu campo; se algum falhar, o objeto não é criado.
     */
    public Pagamento(int contratacaoId, String nomeServico, String nomePagador, String cpfPagador,
                     BigDecimal valor, BigDecimal gorjeta, FormaPagamento formaPagamento) throws ValidacaoException {
        this.statusPagamento = StatusPagamento.PENDENTE;
        this.dataCriacao = LocalDate.now();
        setContratacaoId(contratacaoId);
        setNomeServico(nomeServico);
        setNomePagador(nomePagador);
        setCpfPagador(cpfPagador);
        setValor(valor);
        setGorjeta(gorjeta);
        setFormaPagamento(formaPagamento);
    }

    /** Total que o cliente paga: valor do serviço + gorjeta. */
    public BigDecimal getValorTotal() {
        return valor.add(getGorjeta());
    }

    // ------------------------------------------------------------------
    // Fluxo de status
    // ------------------------------------------------------------------

    /** PIX: o cliente pagou o QR Code. PENDENTE -> PAGO. */
    public void pagarComPix(LocalDate data, String txid) throws ValidacaoException {
        verificarStatus(StatusPagamento.PENDENTE, "Só é possível pagar um pagamento pendente.");
        if (formaPagamento != FormaPagamento.PIX) {
            throw new ValidacaoException("Este pagamento não é por Pix.");
        }
        if (txid == null || !txid.matches("[A-Za-z0-9]{1,25}")) {
            throw new ValidacaoException("Código da transação Pix inválido.");
        }
        validarDataPagamento(data);
        this.codigoTransacao = txid;
        this.dataPagamento = data;
        this.statusPagamento = StatusPagamento.PAGO;
    }

    /**
     * DINHEIRO: o cliente informa que pagou ao profissional.
     * PENDENTE -> AGUARDANDO_CONFIRMACAO (o profissional ainda precisa confirmar).
     */
    public void informarPagamentoEmDinheiro(LocalDate data, BigDecimal valorPago) throws ValidacaoException {
        verificarStatus(StatusPagamento.PENDENTE, "Só é possível informar o pagamento de um pagamento pendente.");
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
            throw new ValidacaoException("O valor pago não pode ser menor que o total ("
                    + "serviço + gorjeta).");
        }
        validarDataPagamento(data);
        this.valorPago = valorPago;
        this.dataPagamento = data;
        this.statusPagamento = StatusPagamento.AGUARDANDO_CONFIRMACAO;
    }

    /** DINHEIRO: o profissional confirma que recebeu. AGUARDANDO_CONFIRMACAO -> PAGO. */
    public void confirmarRecebimento() throws ValidacaoException {
        verificarStatus(StatusPagamento.AGUARDANDO_CONFIRMACAO,
                "Só é possível confirmar o recebimento de um pagamento aguardando confirmação.");
        this.statusPagamento = StatusPagamento.PAGO;
    }

    /** DINHEIRO: o profissional diz que não recebeu. AGUARDANDO_CONFIRMACAO -> PENDENTE. */
    public void recusarRecebimento() throws ValidacaoException {
        verificarStatus(StatusPagamento.AGUARDANDO_CONFIRMACAO,
                "Só é possível recusar um pagamento aguardando confirmação.");
        this.valorPago = null;
        this.dataPagamento = null;
        this.statusPagamento = StatusPagamento.PENDENTE;
    }

    /** Estorna (devolve) o pagamento: PAGO -> ESTORNADO. */
    public void estornar() throws ValidacaoException {
        verificarStatus(StatusPagamento.PAGO, "Só é possível estornar um pagamento que já foi pago.");
        this.statusPagamento = StatusPagamento.ESTORNADO;
    }

    /** Só pagamento pendente pode ter os dados alterados. */
    public boolean podeEditar() {
        return statusPagamento == StatusPagamento.PENDENTE;
    }

    /** Só pagamento pendente pode ser excluído (pago precisa ser estornado; estornado fica de histórico). */
    public boolean podeExcluir() {
        return statusPagamento == StatusPagamento.PENDENTE;
    }

    /** Quanto o cliente pagou em dinheiro além do total (serviço + gorjeta); null se não se aplica. */
    public BigDecimal getValorExcedente() {
        return valorPago == null ? null : valorPago.subtract(getValorTotal());
    }

    private void verificarStatus(StatusPagamento esperado, String mensagem) throws ValidacaoException {
        if (statusPagamento != esperado) {
            throw new ValidacaoException(mensagem);
        }
    }

    private void verificarEditavel() throws ValidacaoException {
        if (!podeEditar()) {
            throw new ValidacaoException("Pagamento com status \"" + statusPagamento + "\" não pode ser alterado.");
        }
    }

    private void validarDataPagamento(LocalDate data) throws ValidacaoException {
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

    // ------------------------------------------------------------------
    // Getters e setters
    // ------------------------------------------------------------------

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
        if (nomePagador.trim().length() < 3) {
            throw new ValidacaoException("O nome do pagador deve ter pelo menos 3 letras.");
        }
        if (!nomePagador.trim().matches("[\\p{L} '.-]+")) {
            throw new ValidacaoException("O nome do pagador deve conter apenas letras.");
        }
        this.nomePagador = nomePagador.trim();
    }

    public String getCpfPagador() {
        return cpfPagador;
    }

    /** Aceita o CPF com ou sem pontuação (529.982.247-25 ou 52998224725) e guarda só os dígitos. */
    public void setCpfPagador(String cpfPagador) throws ValidacaoException {
        verificarEditavel();
        if (cpfPagador == null || cpfPagador.isBlank()) {
            throw new ValidacaoException("Informe o CPF do pagador.");
        }
        String digitos = cpfPagador.replaceAll("[.\\-\\s]", "");
        if (!digitos.matches("\\d{11}")) {
            throw new ValidacaoException("O CPF deve ter 11 números (ex.: 529.982.247-25).");
        }
        if (!cpfValido(digitos)) {
            throw new ValidacaoException("CPF inválido.");
        }
        this.cpfPagador = digitos;
    }

    /**
     * Confere os 2 dígitos verificadores do CPF (algoritmo da Receita Federal).
     * 1º dígito: soma dos 9 primeiros × pesos 10..2; resto da divisão por 11;
     *            se o resto for menor que 2 o dígito é 0, senão é 11 - resto.
     * 2º dígito: mesma conta com os 10 primeiros × pesos 11..2.
     */
    private static boolean cpfValido(String cpf) {
        // CPFs com todos os dígitos iguais (111.111.111-11) passam na conta, mas são inválidos
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }
        return calcularDigitoCpf(cpf, 9) == cpf.charAt(9) - '0'
                && calcularDigitoCpf(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int calcularDigitoCpf(String cpf, int quantidade) {
        int soma = 0;
        int peso = quantidade + 1;
        for (int i = 0; i < quantidade; i++) {
            soma += (cpf.charAt(i) - '0') * peso;
            peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) throws ValidacaoException {
        verificarEditavel();
        // Testa o null antes: chamar compareTo em null daria NullPointerException
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("O valor deve ser maior que zero.");
        }
        if (valor.scale() > 2) {
            throw new ValidacaoException("O valor pode ter no máximo 2 casas decimais.");
        }
        this.valor = valor;
    }

    /** Pagamentos salvos antes de existir a gorjeta voltam com null: tratamos como zero. */
    public BigDecimal getGorjeta() {
        return gorjeta == null ? BigDecimal.ZERO : gorjeta;
    }

    /**
     * Gorjeta é opcional: null vira zero.
     * Não pode ser negativa e, para evitar erro de digitação (ex.: 1500 em vez de 15,00),
     * não pode passar do valor do serviço.
     */
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
