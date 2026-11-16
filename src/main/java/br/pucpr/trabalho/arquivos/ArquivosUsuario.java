package br.pucpr.trabalho.arquivos;

import java.io.*;
import java.util.ArrayList;

import br.pucpr.trabalho.domains.Usuario;        

public class ArquivosUsuario {
    private  static  final String CAMINHO_ARQUIVO = "usuarios.dat"; 

    public  static  boolean salvarLista(ArrayList<Usuario> usuarios){
        try{
            File arq = new File(CAMINHO_ARQUIVO);
            if (!arq.exists()) {
                arq.createNewFile();
            }
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arq));
            oos.writeObject(usuarios);
            oos.close();
            return  true; 
        }catch(FileNotFoundException e ){
            System.err.println("Erro ao salvar lista de usuario" + e.getMessage());
        }catch(IOException e){
            System.err.println("Erro ao salvar lista de usuario" + e.getMessage());
        }
        return  false;
    }
    
    public static ArrayList<Usuario> lerLista(){
        ArrayList<Usuario> lista = new ArrayList<>();
        try{
            File arq = new File(CAMINHO_ARQUIVO);
            if (arq.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CAMINHO_ARQUIVO));
                lista = (ArrayList<Usuario>) ois.readObject();
                ois.close();
            }
        }catch(IOException | ClassNotFoundException e ){
            System.err.println("Erro ao ler lista de usuários: " + e.getMessage());
        }
        return lista;
    }

    public static  int proximoId(ArrayList<Usuario> usuarios){
        int maior = 0 ; 
        for (Usuario u : usuarios) {
            if (u.getId() > maior) {
                maior = u.getId();
            }
            
        }
        return  maior + 1 ; 
    }


    public static int indiceDoId(ArrayList<Usuario> usuarios , int id ){
        for(int i = 0 ; i <usuarios.size() ; i++){
            if (usuarios.get(i).getId()== id) {
                return  i;
            }
        }
        return -1 ; 
    }

    public static  boolean emailExiste(ArrayList<Usuario> usuarios , String email , int idIgnorado){
        for (Usuario u : usuarios) {
            if (u.getId() != idIgnorado && u.getEmail().equalsIgnoreCase(email.trim())) {
                return  true;
                
            }

        }
        return false; 
    }







}
