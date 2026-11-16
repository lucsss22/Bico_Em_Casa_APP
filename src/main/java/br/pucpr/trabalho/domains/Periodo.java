package br.pucpr.trabalho.domains;

public enum Periodo {
    MANHA("Manhã"),
    TARDE("Tarde"),
    NOITE("Noite");

    private final String descricao;

    Periodo(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
