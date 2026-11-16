// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.domains;

/**
 * Lançada quando um dado informado pelo usuário é inválido
 * (campo vazio, número inválido, data fora do formato etc.).
 */
public class ValidacaoException extends Exception {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
