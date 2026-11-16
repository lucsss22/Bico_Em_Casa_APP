package br.pucpr.trabalho.domains;

import java.io.Serializable;

//portfólio do profissional.
 
public class ItemPortfolio implements Serializable {
    private static final long serialVersionUID = 1L;

    private String titulo;
    private String descricao;
    private String caminhoImagem;

    public ItemPortfolio(String titulo, String descricao, String caminhoImagem) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.caminhoImagem = caminhoImagem;
    }

    public String getTitulo() { 
        return titulo; 
    }
    public void setTitulo(String titulo) { 
        this.titulo = titulo; 
    }

    public String getDescricao() { 
        return descricao; 
    }
    public void setDescricao(String descricao) { 
        this.descricao = descricao; 
    }

    public String getCaminhoImagem() { 
        return caminhoImagem; 
    }
    public void setCaminhoImagem(String caminhoImagem) { 
        this.caminhoImagem = caminhoImagem; 
    }

    @Override
    public String toString() {
        return titulo;
    }
}
