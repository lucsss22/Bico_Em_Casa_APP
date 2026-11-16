// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.arquivos;

/**
 * Lançada quando ocorre um problema ao ler ou gravar os dados no arquivo.
 */
public class PersistenciaException extends Exception {

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
