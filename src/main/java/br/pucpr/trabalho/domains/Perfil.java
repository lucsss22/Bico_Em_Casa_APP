package br.pucpr.trabalho.domains;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Classe de domínio Perfil.
 * Atributos: id, idUsuario (a quem o perfil pertence), telefone, cidade e dataNascimento.
 * Guarda o id do usuário (e não o objeto Usuario) para não ficar uma cópia
 * desatualizada do usuário dentro do arquivo de perfis.
 * A data é guardada como texto no formato DD/MM/AAAA.
 */
public class Perfil implements Serializable {
    // definido um serialVersionUID
    private static final long serialVersionUID = 1L;

    private int id;
    private int idUsuario;
    private String telefone;
    private String cidade;
    private String dataNascimento;

    public Perfil(int id, int idUsuario, String telefone, String cidade, String dataNascimento)
            throws ValidacaoException {
        if (idUsuario <= 0) {
            throw new ValidacaoException("O id do usuário deve ser maior que zero.");
        }
        this.id = id;
        this.idUsuario = idUsuario;
        setTelefone(telefone);
        setCidade(cidade);
        setDataNascimento(dataNascimento);
    }

    // Getters e Setters (Encapsulamento)
    public int getId() {
        return id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getTelefone() {
        return telefone;
    }

    /** O telefone deve ter somente números, 10 ou 11 dígitos (com DDD). */
    public void setTelefone(String telefone) throws ValidacaoException {
        if (telefone == null || telefone.isBlank()) {
            throw new ValidacaoException("O telefone é obrigatório.");
        }
        telefone = telefone.trim();
        if (telefone.length() < 10 || telefone.length() > 11) {
            throw new ValidacaoException("O telefone deve ter 10 ou 11 dígitos (com DDD).");
        }
        for (int i = 0; i < telefone.length(); i++) {
            if (!Character.isDigit(telefone.charAt(i))) {
                throw new ValidacaoException("O telefone deve ter somente números.");
            }
        }
        this.telefone = telefone;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) throws ValidacaoException {
        if (cidade == null || cidade.isBlank()) {
            throw new ValidacaoException("A cidade é obrigatória.");
        }
        this.cidade = cidade.trim();
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    /** Recebe a data no formato DD/MM/AAAA e verifica se ela existe de verdade. */
    public void setDataNascimento(String data) throws ValidacaoException {
        if (data == null || data.isBlank()) {
            throw new ValidacaoException("A data de nascimento é obrigatória.");
        }
        String[] partes = data.trim().split("/");
        if (partes.length != 3) {
            throw new ValidacaoException("Data inválida. Use o formato DD/MM/AAAA.");
        }

        int dia;
        int mes;
        int ano;
        try {
            dia = Integer.parseInt(partes[0]);
            mes = Integer.parseInt(partes[1]);
            ano = Integer.parseInt(partes[2]);
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Data inválida. Use somente números no formato DD/MM/AAAA.");
        }

        if (ano < 1900 || ano > LocalDate.now().getYear()) {
            throw new ValidacaoException("Ano inválido.");
        }
        if (mes < 1 || mes > 12) {
            throw new ValidacaoException("Mês inválido.");
        }

        int[] diasNoMes = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        boolean bissexto = (ano % 4 == 0 && ano % 100 != 0) || ano % 400 == 0;
        int maximo = diasNoMes[mes - 1];
        if (mes == 2 && bissexto) {
            maximo = 29;
        }
        if (dia < 1 || dia > maximo) {
            throw new ValidacaoException("Dia inválido para esse mês.");
        }

        // guarda sempre no formato DD/MM/AAAA, com zeros à esquerda
        this.dataNascimento = String.format("%02d/%02d/%04d", dia, mes, ano);
    }

    /**
     * Método toString para facilitar a visualização dos objetos
     * @return string contendo os dados do perfil
     */
    @Override
    public String toString() {
        return "Perfil: " + id + " - Usuario: " + idUsuario + " - " + cidade + " - " + dataNascimento;
    }
}
