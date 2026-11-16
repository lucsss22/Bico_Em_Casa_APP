package br.pucpr.trabalho.domains;

public enum StatusPagamento {
    PENDENTE("Pendente"),
    AGUARDANDO_CONFIRMACAO("Aguardando confirmação"),
    PAGO("Pago"),
    ESTORNADO("Estornado");

    private final String descricao;

    StatusPagamento(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
