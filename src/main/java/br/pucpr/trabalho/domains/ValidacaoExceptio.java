package br.pucpr.trabalho.domains;

public class ValidacaoExceptio extends Exception {
    private  static  final long serialVersionUID = 1L ; 

    public ValidacaoExceptio(String message){
        super(message);
    }

    public ValidacaoExceptio(String message ,Throwable cause){
        super(message , cause);
    }
}
