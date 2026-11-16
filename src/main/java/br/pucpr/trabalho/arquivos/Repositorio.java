package br.pucpr.trabalho.arquivos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

// Salva e lê uma lista de objetos em arquivo (serialização)
public class Repositorio<T extends Identificavel> {

    private File arquivo;

    public Repositorio(String nomeArquivo) {
        this.arquivo = new File(nomeArquivo);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<T> listar() throws PersistenciaException {
        if (!arquivo.exists()) {
            return new ArrayList<>();
        }
        try {
            ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(arquivo));
            ArrayList<T> lista = (ArrayList<T>) entrada.readObject();
            entrada.close();
            return lista;
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException("Não foi possível ler o arquivo " + arquivo.getName());
        }
    }

    public T buscarPorId(int id) throws PersistenciaException {
        for (T item : listar()) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new PersistenciaException("Registro " + id + " não encontrado.");
    }

    public void inserir(T item) throws PersistenciaException {
        ArrayList<T> lista = listar();
        int maiorId = 0;
        for (T existente : lista) {
            if (existente.getId() > maiorId) {
                maiorId = existente.getId();
            }
        }
        item.setId(maiorId + 1);
        lista.add(item);
        gravar(lista);
    }

    public void atualizar(T item) throws PersistenciaException {
        ArrayList<T> lista = listar();
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId() == item.getId()) {
                lista.set(i, item);
                gravar(lista);
                return;
            }
        }
        throw new PersistenciaException("Registro " + item.getId() + " não encontrado.");
    }

    public void excluir(int id) throws PersistenciaException {
        ArrayList<T> lista = listar();
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId() == id) {
                lista.remove(i);
                gravar(lista);
                return;
            }
        }
        throw new PersistenciaException("Registro " + id + " não encontrado.");
    }

    private void gravar(ArrayList<T> lista) throws PersistenciaException {
        try {
            ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(arquivo));
            saida.writeObject(lista);
            saida.close();
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar o arquivo " + arquivo.getName());
        }
    }
}
