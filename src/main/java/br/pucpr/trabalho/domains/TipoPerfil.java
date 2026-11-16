// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.domains;

/**
 * Quem está usando a tela. Define o que cada um pode fazer com o pagamento:
 * - CLIENTE: realiza o pagamento; altera ou exclui enquanto estiver pendente.
 * - PROFISSIONAL: confirma o recebimento em dinheiro, recusa ("não recebi") e estorna.
 */
public enum TipoPerfil {
    CLIENTE("Cliente"),
    PROFISSIONAL("Profissional");

    private final String descricao;

    TipoPerfil(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
