package br.pucpr.trabalho.domains;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

// Monta o código Pix "copia e cola" no padrão do Banco Central (o mesmo texto vira o QR Code)
public class Pix {

    // Chave de exemplo: troque pela chave Pix real para receber de verdade
    public static final String CHAVE = "pagamentos@bicoemcasa.com.br";
    public static final String NOME_RECEBEDOR = "BICO EM CASA";
    public static final String CIDADE_RECEBEDOR = "CURITIBA";

    public static String gerarTxid() {
        String letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String txid = "BICO";
        for (int i = 0; i < 16; i++) {
            txid += letras.charAt(random.nextInt(letras.length()));
        }
        return txid;
    }

    public static String gerarCopiaECola(BigDecimal valor, String txid) {
        String conta = campo("00", "br.gov.bcb.pix") + campo("01", CHAVE);
        String valorTexto = valor.setScale(2, RoundingMode.HALF_UP).toPlainString();

        String codigo = campo("00", "01")
                + campo("26", conta)
                + campo("52", "0000")
                + campo("53", "986")
                + campo("54", valorTexto)
                + campo("58", "BR")
                + campo("59", NOME_RECEBEDOR)
                + campo("60", CIDADE_RECEBEDOR)
                + campo("62", campo("05", txid))
                + "6304";

        return codigo + calcularCrc16(codigo);
    }

    // Cada campo é: ID (2 dígitos) + tamanho (2 dígitos) + valor
    private static String campo(String id, String valor) {
        return id + String.format("%02d", valor.length()) + valor;
    }

    // Código verificador exigido pelo padrão do Pix
    static String calcularCrc16(String texto) {
        int crc = 0xFFFF;
        for (byte b : texto.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc = crc << 1;
                }
                crc = crc & 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }
}
