package br.pucpr.trabalho.domains;

public enum FormaPagamento {
    PIX("Pix"),
    DINHEIRO("Dinheiro");

    private final String descricao;

    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
