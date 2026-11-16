// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.domains.ValidacaoException;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Converte o texto digitado nos campos da tela para os tipos do modelo
 * (data, inteiro, valor em dinheiro), tratando as exceções de conversão.
 */
public final class Conversor {

    /** "uuuu" + STRICT faz o Java recusar datas como 31/02/2026. */
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /** Formato de moeda brasileiro: R$ 1.234,56 */
    private static final NumberFormat FORMATO_MOEDA = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    private Conversor() {
    }

    /** Converte 150.5 em "R$ 150,50" para exibir na tabela. */
    public static String formatarMoeda(BigDecimal valor) {
        return valor == null ? "" : FORMATO_MOEDA.format(valor);
    }

    /** Converte "52998224725" em "529.982.247-25". */
    public static String formatarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf == null ? "" : cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    /** Converte 150.50 em "150,50" para exibir no campo de texto do formulário. */
    public static String formatarDecimal(BigDecimal valor) {
        return valor == null ? "" : valor.toPlainString().replace(".", ",");
    }

    /** Converte "DD/MM/AAAA" em LocalDate. */
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

    /** Converte LocalDate em "DD/MM/AAAA" para exibir na tela. */
    public static String formatarData(LocalDate data) {
        return data == null ? "" : data.format(FORMATO_DATA);
    }

    /** Converte o texto em número inteiro. */
    public static int paraInteiro(String texto, String nomeCampo) throws ValidacaoException {
        if (texto == null || texto.isBlank()) {
            throw new ValidacaoException("Informe o campo " + nomeCampo + ".");
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new ValidacaoException("O campo " + nomeCampo + " deve ser um número inteiro.");
        }
    }

    /** Para campos opcionais: texto vazio vira zero; senão converte como paraDecimal. */
    public static BigDecimal paraDecimalOpcional(String texto, String nomeCampo) throws ValidacaoException {
        if (texto == null || texto.isBlank()) {
            return BigDecimal.ZERO;
        }
        return paraDecimal(texto, nomeCampo);
    }

    /** Converte valores como "150,50" ou "150.50" em BigDecimal. */
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
}
