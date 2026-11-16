// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.domains;

/**
 * Período do dia em que o cliente deseja que o serviço seja feito.
 */
public enum Periodo {
    MANHA("Manhã"),
    TARDE("Tarde"),
    NOITE("Noite");

    private final String descricao;

    Periodo(String descricao) {
        this.descricao = descricao;
    }

    /** O ComboBox usa o toString para exibir o texto. */
    @Override
    public String toString() {
        return descricao;
    }
}
