package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.domains.ValidacaoException;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

// Converte o texto dos campos da tela (data, número, dinheiro) e formata para exibir
public class Conversor {

    // STRICT faz recusar datas que não existem, como 31/02/2026
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static LocalDate paraData(String texto, String nomeCampo) throws ValidacaoException {
        if (texto == null || texto.isBlank()) {
            throw new ValidacaoException("Informe o campo " + nomeCampo + ".");
        }
        try {
            return LocalDate.parse(texto.trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("O campo " + nomeCampo + " deve ser uma data válida no formato DD/MM/AAAA.");
        }
    }

    public static String formatarData(LocalDate data) {
        if (data == null) {
            return "";
        }
        return data.format(FORMATO_DATA);
    }

    public static BigDecimal paraDecimal(String texto, String nomeCampo) throws ValidacaoException {
        if (texto == null || texto.isBlank()) {
            throw new ValidacaoException("Informe o campo " + nomeCampo + ".");
        }
        try {
            return new BigDecimal(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("O campo " + nomeCampo + " deve ser um valor numérico (ex.: 150,50).");
        }
    }

    // Para campos opcionais: vazio vale zero
    public static BigDecimal paraDecimalOpcional(String texto, String nomeCampo) throws ValidacaoException {
        if (texto == null || texto.isBlank()) {
            return BigDecimal.ZERO;
        }
        return paraDecimal(texto, nomeCampo);
    }

    public static String formatarMoeda(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(valor);
    }

    public static String formatarDecimal(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return valor.toPlainString().replace(".", ",");
    }

    public static String formatarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}
