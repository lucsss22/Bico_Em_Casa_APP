package br.pucpr.trabalho.arquivos;

import br.pucpr.trabalho.domains.ItemPortfolio;

import java.io.*;
import java.util.ArrayList;

public class ArquivoPortfolio {
    private static final String CAMINHO_ARQUIVO = "portfolio.dat";

    public static boolean salvarLista(ArrayList<ItemPortfolio> lista) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CAMINHO_ARQUIVO))) {
            oos.writeObject(lista);
            return true;
        } catch (IOException e) {
            System.err.println("Erro ao salvar lista: " + e.getMessage());
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<ItemPortfolio> lerLista() {
        ArrayList<ItemPortfolio> lista = new ArrayList<>();
        File arq = new File(CAMINHO_ARQUIVO);
        if (arq.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arq))) {
                lista = (ArrayList<ItemPortfolio>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro ao ler lista: " + e.getMessage());
            }
        }
        return lista;
    }
}
