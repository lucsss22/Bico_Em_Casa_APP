// Autor: Carlos Alberto
package br.pucpr.trabalho.domains;

import br.pucpr.trabalho.arquivos.Identificavel;

import java.time.LocalDate;

public class Contratacao implements Identificavel {
    private static final long serialVersionUID = 1L;
    public static final int TAMANHO_MINIMO_ENDERECO = 5;
    public static final int TAMANHO_MAXIMO_ENDERECO = 150;

    private int id;
    private int idServico;
    private int idCliente;
    private LocalDate dataServico;
    private String endereco;
    private String status;

    public Contratacao(int idServico, int idCliente, LocalDate dataServico, String endereco) throws ValidacaoException {
        setIdServico(idServico);
        setIdCliente(idCliente);
        setDataServico(dataServico);
        setEndereco(endereco);
        setStatus("Agendada");
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getIdServico() {
        return idServico;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public LocalDate getDataServico() {
        return dataServico;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getStatus() {
        return status;
    }

    public void setIdServico(int idServico) throws ValidacaoException {
        if (idServico <= 0)
            throw new ValidacaoException("Selecione o serviço contratado.");
        this.idServico = idServico;
    }

    public void setIdCliente(int idCliente) throws ValidacaoException {
        if (idCliente <= 0)
            throw new ValidacaoException("Selecione o cliente da contratação.");
        this.idCliente = idCliente;
    }

    public void setDataServico(LocalDate dataServico) throws ValidacaoException {
        if (dataServico == null)
            throw new ValidacaoException("Informe a data do serviço.");
        if (dataServico.isBefore(LocalDate.now()))
            throw new ValidacaoException("A data do serviço não pode ser anterior a hoje.");
        this.dataServico = dataServico;
    }

    public void setEndereco(String endereco) throws ValidacaoException {
        if (endereco == null || endereco.isBlank())
            throw new ValidacaoException("Informe o endereço do serviço.");
        endereco = endereco.trim();
        if (endereco.length() < TAMANHO_MINIMO_ENDERECO)
            throw new ValidacaoException("O endereço deve ter pelo menos " + TAMANHO_MINIMO_ENDERECO + " caracteres.");
        if (endereco.length() > TAMANHO_MAXIMO_ENDERECO)
            throw new ValidacaoException("O endereço pode ter no máximo " + TAMANHO_MAXIMO_ENDERECO + " caracteres.");
        this.endereco = endereco;
    }

    public void setStatus(String status) throws ValidacaoException {
        if (status == null || !(status.equals("Agendada") || status.equals("Concluída") || status.equals("Cancelada")))
            throw new ValidacaoException("O status deve ser Agendada, Concluída ou Cancelada.");
        this.status = status;
    }

    @Override
    public String toString() {
        return "Contratação " + id;
    }
}
