package br.pucpr.trabalho.arquivos;

import br.pucpr.trabalho.domains.Denuncia;
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

public class RepositorioDenuncias {

    private final Path arquivo = Path.of("dados", "denuncias.properties");

    public List<Denuncia> listar() throws IOException {
        if (Files.notExists(arquivo)) {
            return List.of();
        }

        Properties propriedades = new Properties();
        try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            try {
                propriedades.load(leitor);
            } catch (IllegalArgumentException excecao) {
                throw new IOException("O arquivo de denúncias está malformado.", excecao);
            }
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(propriedadeObrigatoria(propriedades, "quantidade"));
        } catch (NumberFormatException excecao) {
            throw new IOException("O arquivo de denúncias não contém uma quantidade válida.",
                    excecao);
        }
        if (quantidade < 0) {
            throw new IOException("A quantidade de denúncias no arquivo não pode ser negativa.");
        }

        List<Denuncia> denuncias = new ArrayList<>();
        for (int indice = 0; indice < quantidade; indice++) {
            String prefixo = "denuncia." + indice + ".";
            try {
                Denuncia denuncia = new Denuncia(
                        propriedadeObrigatoria(propriedades, prefixo + "alvo"),
                        propriedadeObrigatoria(propriedades, prefixo + "motivo"),
                        propriedadeObrigatoria(propriedades, prefixo + "descricao"));
                denuncia.setId(Integer.parseInt(propriedadeObrigatoria(
                        propriedades, prefixo + "id")));
                denuncias.add(denuncia);
            } catch (ValidacaoException | IllegalArgumentException excecao) {
                throw new IOException(
                        "Os dados da denúncia na posição " + (indice + 1) + " são inválidos.",
                        excecao);
            }
        }

        return List.copyOf(denuncias);
    }

    public void salvar(List<Denuncia> denuncias) throws IOException {
        Properties propriedades = new Properties();
        propriedades.setProperty("quantidade", Integer.toString(denuncias.size()));
        for (int indice = 0; indice < denuncias.size(); indice++) {
            Denuncia denuncia = denuncias.get(indice);
            String prefixo = "denuncia." + indice + ".";
            propriedades.setProperty(prefixo + "id", Integer.toString(denuncia.getId()));
            propriedades.setProperty(prefixo + "alvo", denuncia.getAlvo());
            propriedades.setProperty(prefixo + "motivo", denuncia.getMotivo());
            propriedades.setProperty(prefixo + "descricao", denuncia.getDescricao());
        }

        Files.createDirectories(arquivo.getParent());
        try (Writer escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
            propriedades.store(escritor, "Denúncias");
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
