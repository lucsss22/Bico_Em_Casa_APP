package br.pucpr.trabalho.domains;

import br.pucpr.trabalho.arquivos.Identificavel;

public class Avaliacao implements Identificavel {

    private static final long serialVersionUID = 1L;
    private int id;
    private String profissionalAvaliado;
    private int nota;
    private String comentario;

    public Avaliacao(String profissionalAvaliado, int nota, String comentario)
            throws ValidacaoException {
        setProfissionalAvaliado(profissionalAvaliado);
        setNota(nota);
        setComentario(comentario);
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

    public String getProfissionalAvaliado() {
        return profissionalAvaliado;
    }

    public void setProfissionalAvaliado(String profissionalAvaliado) throws ValidacaoException {
        if (profissionalAvaliado == null || profissionalAvaliado.isBlank()) {
            throw new ValidacaoException("Selecione o profissional avaliado.");
        }
        this.profissionalAvaliado = profissionalAvaliado.trim();
    }

    public int getNota() {
        return nota;
    }

    public void setNota(int nota) throws ValidacaoException {
        if (nota < 1 || nota > 5) {
            throw new ValidacaoException("A nota deve estar entre 1 e 5.");
        }
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) throws ValidacaoException {
        String valor = comentario == null ? "" : comentario.trim();
        if (valor.length() > 1000) {
            throw new ValidacaoException("O comentário não pode ultrapassar 1000 caracteres.");
        }
        this.comentario = valor;
    }
}
