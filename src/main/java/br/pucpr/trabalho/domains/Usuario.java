package br.pucpr.trabalho.domains;

import java.io.Serializable;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id ; 
    private String nome; 
    private  String email; 
    private  String senha ; 

    public Usuario(int id , String nome , String email , String senha )throws ValidacaoException{
        this.id = id;
        setNome(nome);
        setEmail(email);
        setSenha(senha);
    }

    public int getId(){
        return  id;
    }

    public String getNome(){
        return nome ; 
    }

    public void setNome(String nome) throws ValidacaoException {
        if(nome == null || nome.isBlank()){
            throw new ValidacaoException("O nome é obrigatorio.");
        }
        this.nome = nome.trim();
    }


    public String getEmail(){
        return email ; 
    }

    public void setEmail(String email) throws ValidacaoException {
        if (email == null || email.isBlank()) {
            throw new ValidacaoException("O email é obrigatório.");
        }
        email = email.trim();
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            throw new ValidacaoException("Email inválido.");
        }
        this.email = email;
    }


    public String getSenha(){
        return senha ; 
    }

    public void setSenha(String senha) throws ValidacaoException {
        if(senha == null || senha.length()<4){
            throw new ValidacaoException("A senha deve ter pelo menos 4 caracteres");
        }
        this.senha = senha;
        
    }
    @Override 
    public  String toString(){
         return "Usuario: " + id + " - " + nome + " - " + email;
    }


}
