package br.pucpr.trabalho.domains;

import br.pucpr.trabalho.arquivos.Identificavel;

import java.time.LocalDate;
import java.util.Locale;

public class Servico implements Identificavel {
    private static final long serialVersionUID = 1L;
    public static final int TAMANHO_MINIMO_TITULO = 3;
    public static final int TAMANHO_MAXIMO_TITULO = 100;
    public static final double PRECO_MAXIMO = 100_000.00;

    private int id;
    private String titulo;
    private int idUsuario;
    private double precoServico;
    private LocalDate criadoEm;
    private LocalDate atualizadoEm;

    public Servico(String titulo, int idUsuario, double precoServico) throws ValidacaoException {
        this.criadoEm = LocalDate.now();
        setTitulo(titulo);
        setIdUsuario(idUsuario);
        setPrecoServico(precoServico);
        this.atualizadoEm = this.criadoEm;
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

    public int getIdUsuario() {
        return idUsuario;
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
    public void setTitulo(String titulo) throws ValidacaoException {
        if (titulo == null || titulo.isBlank())
            throw new ValidacaoException("Informe o título do serviço.");
        if (titulo.length() < TAMANHO_MINIMO_TITULO)
            throw new ValidacaoException("O título deve ter pelo menos " + TAMANHO_MINIMO_TITULO + " caracteres.");
        if (titulo.length() > TAMANHO_MAXIMO_TITULO)
            throw new ValidacaoException("O título pode ter no máximo " + TAMANHO_MAXIMO_TITULO + " caracteres.");
        this.titulo = titulo;
        marcarAtualizado();
    }

    public void setIdUsuario(int idUsuario) throws ValidacaoException {
        if (idUsuario <= 0)
            throw new ValidacaoException("Selecione o usuário responsável pelo serviço.");
        this.idUsuario = idUsuario;
        marcarAtualizado();
    }

    public void setPrecoServico(double precoServico) throws ValidacaoException {
        if (precoServico <= 0)
            throw new ValidacaoException("O preço do serviço deve ser maior que zero.");
        if (precoServico > PRECO_MAXIMO)
            throw new ValidacaoException("O preço do serviço não pode passar de R$ "
                    + String.format(Locale.of("pt", "BR"), "%,.2f", PRECO_MAXIMO) + ".");
        this.precoServico = precoServico;
        marcarAtualizado();
    }

    private void marcarAtualizado() {
        this.atualizadoEm = LocalDate.now();
    }

    @Override
    public  String toString(){
        return "Serviço: " + id + " - " + titulo;
    }
}
