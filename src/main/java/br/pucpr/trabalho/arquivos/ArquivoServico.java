package br.pucpr.trabalho.arquivos;

import br.pucpr.trabalho.domains.Servico;

import java.io.*;
import java.util.ArrayList;

public class ArquivoServico {
    private static final String CAMINHO_ARQUIVO = "servicos.dat";

    public static boolean salvarLista(ArrayList<Servico> lista) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CAMINHO_ARQUIVO))) {
            oos.writeObject(lista);
            return true;
        } catch (IOException e) {
            System.err.println("Erro ao salvar lista: " + e.getMessage());
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Servico> lerLista() {
        ArrayList<Servico> lista = new ArrayList<>();
        File arq = new File(CAMINHO_ARQUIVO);
        if (arq.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arq))) {
                lista = (ArrayList<Servico>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro ao ler lista: " + e.getMessage());
            }
        }
        return lista;
    }
}
