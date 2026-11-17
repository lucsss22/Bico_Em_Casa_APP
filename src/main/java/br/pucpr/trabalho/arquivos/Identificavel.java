package br.pucpr.trabalho.arquivos;

import java.io.Serializable;

public interface Identificavel extends Serializable {

    int getId();

    void setId(int id);
}
