package br.pucpr.trabalho.domains;

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
