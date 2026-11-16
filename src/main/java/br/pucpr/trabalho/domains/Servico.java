package br.pucpr.trabalho.domains;

import br.pucpr.trabalho.arquivos.Identificavel;

import java.time.LocalDate;

public class Servico implements Identificavel {
    private int id;
    private String titulo;
    private String nomeUsuario;
    private double precoServico;
    private LocalDate criadoEm;
    private LocalDate atualizadoEm;

    public Servico(int id, String titulo, String nomeUsuario, double precoServico) {
        this.id = id;
        this.nomeUsuario = nomeUsuario;
        this.titulo = titulo;
        this.precoServico = precoServico;
        criadoEm = LocalDate.now();
        atualizadoEm = LocalDate.now();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    // getters

    public String getTitulo() {
        return titulo;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public double getPrecoServico() {
        return precoServico;
    }

    public LocalDate getCriadoEm() {
        return criadoEm;
    }

    public LocalDate getAtualizadoEm() {
        return atualizadoEm;
    }

    // setters

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public void setPrecoServico(double precoServico) {
        this.precoServico = precoServico;
    }

    public void setCriadoEm(LocalDate criadoEm) {
        this.criadoEm = criadoEm;
    }

    public void setAtualizadoEm(LocalDate atualizadoEm) {
            this.atualizadoEm = atualizadoEm;
    }
}
