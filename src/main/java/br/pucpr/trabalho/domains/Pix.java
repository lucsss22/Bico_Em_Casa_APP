package br.pucpr.trabalho.domains;

import java.util.Random;

public class Pix {

    // Chave de exemplo: troque pela chave Pix real para receber de verdade
    public static final String CHAVE = "pagamentos@bicoemcasa.com.br";
    public static final String NOME_RECEBEDOR = "BICO EM CASA";

    // Gera o código da transação guardado no pagamento
    public static String gerarTxid() {
        String letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String txid = "BICO";
        for (int i = 0; i < 16; i++) {
            txid += letras.charAt(random.nextInt(letras.length()));
        }
        return txid;
    }
}
