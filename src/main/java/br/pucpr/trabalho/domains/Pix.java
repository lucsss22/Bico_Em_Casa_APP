// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.domains;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;

/**
 * Monta o código Pix "copia e cola" no padrão BR Code do Banco Central (EMV QRCPS).
 * O mesmo texto é usado para gerar a imagem do QR Code.
 *
 * O código é uma sequência de campos no formato ID + TAMANHO + VALOR, por exemplo:
 *   "5303986" = campo 53 (moeda), tamanho 03, valor "986" (Real).
 * O último campo (63) é um CRC16, uma "soma de verificação" que o app do banco
 * usa para conferir se o código não foi digitado/copiado errado.
 */
public final class Pix {

    /** Dados de quem recebe o pagamento. */
    // CHAVE DE EXEMPLO (repositório público). Para receber de verdade, troque pela chave Pix real.
    public static final String CHAVE = "pagamentos@bicoemcasa.com.br";
    public static final String NOME_RECEBEDOR = "BICO EM CASA";   // máx. 25 caracteres, sem acento
    public static final String CIDADE_RECEBEDOR = "CURITIBA";     // máx. 15 caracteres, sem acento

    private static final String CARACTERES_TXID = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Pix() {
    }

    /** Gera um identificador da transação (txid): letras e números, até 25 caracteres. */
    public static String gerarTxid() {
        StringBuilder txid = new StringBuilder("BICO");
        for (int i = 0; i < 16; i++) {
            txid.append(CARACTERES_TXID.charAt(ALEATORIO.nextInt(CARACTERES_TXID.length())));
        }
        return txid.toString();
    }

    /** Monta o código "copia e cola" para o valor e o txid informados. */
    public static String gerarCopiaECola(BigDecimal valor, String txid) {
        String contaRecebedor = campo("00", "br.gov.bcb.pix") + campo("01", CHAVE);
        String dadosAdicionais = campo("05", txid);

        String codigo = campo("00", "01")                        // versão do padrão
                + campo("26", contaRecebedor)                     // chave Pix
                + campo("52", "0000")                             // categoria do comerciante (não informada)
                + campo("53", "986")                              // moeda: 986 = Real
                + campo("54", valor.setScale(2, RoundingMode.HALF_UP).toPlainString()) // valor
                + campo("58", "BR")                               // país
                + campo("59", NOME_RECEBEDOR)                     // nome de quem recebe
                + campo("60", CIDADE_RECEBEDOR)                   // cidade de quem recebe
                + campo("62", dadosAdicionais)                    // txid
                + "6304";                                         // campo do CRC (tamanho 04)

        return codigo + calcularCrc16(codigo);
    }

    /** Formata um campo: ID (2 dígitos) + tamanho (2 dígitos) + valor. */
    private static String campo(String id, String valor) {
        return id + String.format("%02d", valor.length()) + valor;
    }

    /**
     * CRC16-CCITT (polinômio 0x1021, valor inicial 0xFFFF), exigido pelo padrão do Pix.
     * Devolve 4 caracteres hexadecimais em maiúsculo.
     */
    static String calcularCrc16(String texto) {
        int crc = 0xFFFF;
        for (byte b : texto.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }
}
