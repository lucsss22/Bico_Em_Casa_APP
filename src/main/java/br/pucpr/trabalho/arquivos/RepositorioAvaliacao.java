package br.pucpr.trabalho.arquivos;

import br.pucpr.trabalho.domains.Avaliacao;
import br.pucpr.trabalho.domains.ValidacaoException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RepositorioAvaliacao {

    private final Path arquivo = Path.of("dados", "avaliacoes.properties");

    public List<Avaliacao> listar() throws IOException {
        if (Files.notExists(arquivo)) {
            return List.of();
        }

        Properties propriedades = new Properties();
        try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            try {
                propriedades.load(leitor);
            } catch (IllegalArgumentException excecao) {
                throw new IOException("O arquivo de avaliações está malformado.", excecao);
            }
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(propriedadeObrigatoria(propriedades, "quantidade"));
        } catch (NumberFormatException excecao) {
            throw new IOException("O arquivo de avaliações não contém uma quantidade válida.",
                    excecao);
        }
        if (quantidade < 0) {
            throw new IOException("A quantidade de avaliações não pode ser negativa.");
        }

        List<Avaliacao> avaliacoes = new ArrayList<>();
        for (int indice = 0; indice < quantidade; indice++) {
            String prefixo = "avaliacao." + indice + ".";
            try {
                Avaliacao avaliacao = new Avaliacao(
                        propriedadeObrigatoria(propriedades, prefixo + "profissionalAvaliado"),
                        Integer.parseInt(propriedadeObrigatoria(propriedades, prefixo + "nota")),
                        propriedadeObrigatoria(propriedades, prefixo + "comentario"));
                avaliacao.setId(Integer.parseInt(propriedadeObrigatoria(
                        propriedades, prefixo + "id")));
                avaliacoes.add(avaliacao);
            } catch (ValidacaoException | IllegalArgumentException excecao) {
                throw new IOException(
                        "Os dados da avaliação na posição " + (indice + 1) + " são inválidos.",
                        excecao);
            }
        }

        return List.copyOf(avaliacoes);
    }

    public void salvar(List<Avaliacao> avaliacoes) throws IOException {
        Properties propriedades = new Properties();
        propriedades.setProperty("quantidade", Integer.toString(avaliacoes.size()));
        for (int indice = 0; indice < avaliacoes.size(); indice++) {
            Avaliacao avaliacao = avaliacoes.get(indice);
            String prefixo = "avaliacao." + indice + ".";
            propriedades.setProperty(prefixo + "id", Integer.toString(avaliacao.getId()));
            propriedades.setProperty(
                    prefixo + "profissionalAvaliado", avaliacao.getProfissionalAvaliado());
            propriedades.setProperty(prefixo + "nota", Integer.toString(avaliacao.getNota()));
            propriedades.setProperty(prefixo + "comentario", avaliacao.getComentario());
        }

        Files.createDirectories(arquivo.getParent());
        try (Writer escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
            propriedades.store(escritor, "Avaliações");
        }
    }

    private String propriedadeObrigatoria(Properties propriedades, String nome) throws IOException {
        String valor = propriedades.getProperty(nome);
        if (valor == null) {
            throw new IOException("Falta a propriedade obrigatória: " + nome);
        }
        return valor;
    }
}
