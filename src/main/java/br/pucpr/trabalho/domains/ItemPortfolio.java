package br.pucpr.trabalho.domains;

import java.io.Serializable;

//portfólio do profissional.

public class ItemPortfolio implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int TAMANHO_MINIMO_TITULO = 3;
    public static final int TAMANHO_MAXIMO_TITULO = 100;
    public static final int TAMANHO_MAXIMO_DESCRICAO = 500;

    private String titulo;
    private String descricao;
    private String caminhoImagem;

    public ItemPortfolio(String titulo, String descricao, String caminhoImagem) throws ValidacaoException {
        setTitulo(titulo);
        setDescricao(descricao);
        setCaminhoImagem(caminhoImagem);
    }

    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) throws ValidacaoException {
        if (titulo == null || titulo.isBlank()) {
            throw new ValidacaoException("Informe o título do item.");
        }
        titulo = titulo.trim();
        if (titulo.length() < TAMANHO_MINIMO_TITULO || titulo.length() > TAMANHO_MAXIMO_TITULO) {
            throw new ValidacaoException("O título deve ter de " + TAMANHO_MINIMO_TITULO + " a "
                    + TAMANHO_MAXIMO_TITULO + " caracteres.");
        }
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) throws ValidacaoException {
        if (descricao == null) {
            descricao = "";
        }
        if (descricao.length() > TAMANHO_MAXIMO_DESCRICAO) {
            throw new ValidacaoException("A descrição pode ter no máximo " + TAMANHO_MAXIMO_DESCRICAO + " caracteres.");
        }
        this.descricao = descricao.trim();
    }

    public String getCaminhoImagem() {
        return caminhoImagem;
    }
    public void setCaminhoImagem(String caminhoImagem) throws ValidacaoException {
        if (caminhoImagem == null || caminhoImagem.isBlank()) {
            throw new ValidacaoException("Escolha uma imagem para o item.");
        }
        this.caminhoImagem = caminhoImagem;
    }

    @Override
    public String toString() {
        return titulo;
    }
}
