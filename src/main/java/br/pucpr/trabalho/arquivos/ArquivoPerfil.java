package br.pucpr.trabalho.arquivos;

import br.pucpr.trabalho.domains.Perfil;

import java.io.*;
import java.util.ArrayList;

/**
 * Classe responsável por gravar e ler a lista de perfis no arquivo perfis.dat
 * (persistência por serialização).
 */
public class ArquivoPerfil {

    // Constante com o nome do arquivo para persistir os objetos
    private static final String CAMINHO_ARQUIVO = "perfis.dat";

    /**
     * Escreve a lista de perfis no arquivo.
     * @return true se gravou, false se deu erro
     */
    public static boolean salvarLista(ArrayList<Perfil> perfis) {
        try {
            File arq = new File(CAMINHO_ARQUIVO);
            if (!arq.exists()) {
                arq.createNewFile();
            }
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arq));
            oos.writeObject(perfis);
            oos.close();
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao salvar lista de perfis: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erro ao salvar lista de perfis: " + e.getMessage());
        }
        return false;
    }

    /**
     * Lê a lista de perfis do arquivo.
     * Se o arquivo ainda não existe (primeira execução), devolve uma lista vazia.
     */
    public static ArrayList<Perfil> lerLista() {
        ArrayList<Perfil> lista = new ArrayList<>();
        try {
            File arq = new File(CAMINHO_ARQUIVO);
            if (arq.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CAMINHO_ARQUIVO));
                lista = (ArrayList<Perfil>) ois.readObject();
                ois.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao ler lista de perfis: " + e.getMessage());
        }
        return lista;
    }

    /** Calcula o próximo id: maior id da lista + 1. */
    public static int proximoId(ArrayList<Perfil> perfis) {
        int maior = 0;
        for (Perfil p : perfis) {
            if (p.getId() > maior) {
                maior = p.getId();
            }
        }
        return maior + 1;
    }

    /** Devolve a posição do perfil na lista, ou -1 se não encontrar. */
    public static int indiceDoId(ArrayList<Perfil> perfis, int id) {
        for (int i = 0; i < perfis.size(); i++) {
            if (perfis.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    /** Verifica se o usuário já possui um perfil. */
    public static boolean usuarioTemPerfil(ArrayList<Perfil> perfis, int idUsuario) {
        for (Perfil p : perfis) {
            if (p.getIdUsuario() == idUsuario) {
                return true;
            }
        }
        return false;
    }
}
