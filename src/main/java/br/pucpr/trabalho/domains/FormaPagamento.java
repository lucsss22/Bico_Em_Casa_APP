// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.domains;

/** formas de pagamento que o cliente pode escolher */
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
