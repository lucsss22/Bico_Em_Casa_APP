// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.arquivos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda uma lista de objetos em arquivo usando serialização.
 * É genérico (T): a mesma classe serve para Comentario, Pagamento etc.
 *
 * Cada operação lê a lista do arquivo, altera e grava de volta.
 */
public class Repositorio<T extends Identificavel> {

    private final File arquivo;

    public Repositorio(String nomeArquivo) {
        this.arquivo = new File(nomeArquivo);   // salva na pasta do projeto, igual aos outros .dat do grupo
    }

    /** Lê todos os objetos do arquivo. Se o arquivo ainda não existe, devolve lista vazia. */
    @SuppressWarnings("unchecked")
    public List<T> listar() throws PersistenciaException {
        if (!arquivo.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (List<T>) entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException("Não foi possível ler o arquivo " + arquivo.getName(), e);
        }
    }

    /** Procura um objeto pelo id. */
    public T buscarPorId(int id) throws PersistenciaException {
        for (T item : listar()) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new PersistenciaException("Registro " + id + " não encontrado.");
    }

    /** Gera o próximo id (maior id + 1), adiciona o objeto e grava. */
    public void inserir(T item) throws PersistenciaException {
        List<T> itens = listar();
        int maiorId = 0;
        for (T existente : itens) {
            if (existente.getId() > maiorId) {
                maiorId = existente.getId();
            }
        }
        item.setId(maiorId + 1);
        itens.add(item);
        gravar(itens);
    }

    /** Substitui o objeto que tem o mesmo id. */
    public void atualizar(T item) throws PersistenciaException {
        List<T> itens = listar();
        for (int i = 0; i < itens.size(); i++) {
            if (itens.get(i).getId() == item.getId()) {
                itens.set(i, item);
                gravar(itens);
                return;
            }
        }
        throw new PersistenciaException("Registro " + item.getId() + " não encontrado.");
    }

    /** Remove o objeto com o id informado. */
    public void excluir(int id) throws PersistenciaException {
        List<T> itens = listar();
        boolean removeu = itens.removeIf(item -> item.getId() == id);
        if (!removeu) {
            throw new PersistenciaException("Registro " + id + " não encontrado.");
        }
        gravar(itens);
    }

    /** Serializa a lista inteira no arquivo. */
    private void gravar(List<T> itens) throws PersistenciaException {
        try (ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            saida.writeObject(new ArrayList<>(itens));
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar o arquivo " + arquivo.getName(), e);
        }
    }
}
