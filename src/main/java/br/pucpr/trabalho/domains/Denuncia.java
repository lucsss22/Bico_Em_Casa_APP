package br.pucpr.trabalho.domains;

import br.pucpr.trabalho.arquivos.Identificavel;

public class Denuncia implements Identificavel {

    private static final long serialVersionUID = 1L;
    private int id;
    private String alvo;
    private String motivo;
    private String descricao;

    public Denuncia(String alvo, String motivo, String descricao) throws ValidacaoException {
        setAlvo(alvo);
        setMotivo(motivo);
        setDescricao(descricao);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("O identificador deve ser positivo.");
        }
        this.id = id;
    }

    public String getAlvo() {
        return alvo;
    }

    public void setAlvo(String alvo) throws ValidacaoException {
        this.alvo = campoObrigatorio(alvo, "Informe quem você quer denunciar.");
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) throws ValidacaoException {
        this.motivo = campoObrigatorio(motivo, "Selecione o motivo da denúncia.");
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) throws ValidacaoException {
        String valor = campoObrigatorio(descricao, "Descreva o motivo da denúncia.");
        if (valor.length() < 20) {
            throw new ValidacaoException("A descrição deve ter pelo menos 20 caracteres.");
        }
        this.descricao = valor;
    }

    private String campoObrigatorio(String valor, String mensagem) throws ValidacaoException {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException(mensagem);
        }
        return valor.trim();
    }
}
