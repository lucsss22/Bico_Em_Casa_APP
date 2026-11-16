// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.arquivos;

import java.io.Serializable;

/**
 * Contrato para toda classe que pode ser salva em arquivo pelo Repositorio.
 * Exige um id (para localizar o objeto) e ser Serializable (para virar bytes).
 */
public interface Identificavel extends Serializable {

    int getId();

    void setId(int id);
}
