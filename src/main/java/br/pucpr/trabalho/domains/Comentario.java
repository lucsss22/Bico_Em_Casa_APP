// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.domains;

import br.pucpr.trabalho.arquivos.Identificavel;

import java.time.LocalDate;

/**
 * Comentário que o cliente escreve no momento da contratação,
 * explicando o que deseja que o profissional faça.
 */
public class Comentario implements Identificavel {

    private static final long serialVersionUID = 1L;
    public static final int TAMANHO_MAXIMO_TEXTO = 1000;

    private int id;
    private int contratacaoId;       // vem da Contratação, não aparece para o usuário
    private String nomeServico;      // vem do Serviço contratado, só é exibido
    private String autor;            // nome ou apelido de como o cliente prefere ser chamado
    private String texto;
    private LocalDate dataDesejada;
    private Periodo periodo;
    private boolean urgente;
    private LocalDate dataCriacao;

    /**
     * Cria um comentário novo. Os setters validam cada campo.
     * contratacaoId e nomeServico são preenchidos pelo sistema (a partir da contratação),
     * os demais pelo usuário.
     */
    public Comentario(int contratacaoId, String nomeServico, String autor, String texto,
                      LocalDate dataDesejada, Periodo periodo, boolean urgente) throws ValidacaoException {
        this.dataCriacao = LocalDate.now();
        setContratacaoId(contratacaoId);
        setNomeServico(nomeServico);
        setAutor(autor);
        setTexto(texto);
        setDataDesejada(dataDesejada);
        setPeriodo(periodo);
        setUrgente(urgente);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getContratacaoId() {
        return contratacaoId;
    }

    public void setContratacaoId(int contratacaoId) throws ValidacaoException {
        if (contratacaoId <= 0) {
            throw new ValidacaoException("O ID da contratação deve ser maior que zero.");
        }
        this.contratacaoId = contratacaoId;
    }

    public String getNomeServico() {
        return nomeServico;
    }

    public void setNomeServico(String nomeServico) throws ValidacaoException {
        if (nomeServico == null || nomeServico.isBlank()) {
            throw new ValidacaoException("O comentário precisa estar ligado a um serviço.");
        }
        this.nomeServico = nomeServico.trim();
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) throws ValidacaoException {
        if (autor == null || autor.isBlank()) {
            throw new ValidacaoException("Informe como você prefere ser chamado.");
        }
        this.autor = autor.trim();
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) throws ValidacaoException {
        if (texto == null || texto.isBlank()) {
            throw new ValidacaoException("O comentário não pode ficar vazio.");
        }
        if (texto.length() > TAMANHO_MAXIMO_TEXTO) {
            throw new ValidacaoException("O comentário pode ter no máximo "
                    + TAMANHO_MAXIMO_TEXTO + " caracteres.");
        }
        this.texto = texto.trim();
    }

    public LocalDate getDataDesejada() {
        return dataDesejada;
    }

    public void setDataDesejada(LocalDate dataDesejada) throws ValidacaoException {
        if (dataDesejada == null) {
            throw new ValidacaoException("Informe a data desejada para o serviço.");
        }
        if (dataDesejada.isBefore(dataCriacao)) {
            throw new ValidacaoException("A data desejada não pode ser anterior à data do comentário.");
        }
        this.dataDesejada = dataDesejada;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public void setPeriodo(Periodo periodo) throws ValidacaoException {
        if (periodo == null) {
            throw new ValidacaoException("Selecione o período.");
        }
        this.periodo = periodo;
    }

    /** Indica se o cliente precisa do serviço com urgência. */
    public boolean isUrgente() {
        return urgente;
    }

    public void setUrgente(boolean urgente) {
        this.urgente = urgente;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }
}
