# RA3 - Cadastro com JavaFX e serialização

Trabalho em grupo da disciplina de POO / Experiência Criativa (PUCPR).
Interface feita **somente em código JavaFX** (sem FXML) e persistência por **serialização** (`.dat`).

## Como rodar

Na pasta que contém o `pom.xml`:

```
mvn clean javafx:run
```

Requisitos: JDK 21 ou superior e Maven 3.9+.

## Estrutura

```
src/main/java/br/pucpr/ra3/
  Programa.java, CadastroApp.java     entrada da aplicação
  domains/                            classes de domínio (regras e validações)
  arquivos/                           gravação e leitura (serialização)
  controllers/                        telas JavaFX
```

## Integrantes e classes de cada um

| Integrante | Classes (domínio, arquivo, controllers) |
|---|---|
| Lucas Cobucci Babula Alves | Usuario, Perfil |
| (preencher) | |
| (preencher) | |
| (preencher) | |
| (preencher) | |

## Regras do grupo

1. **Nunca** commitar direto na `main`. Cada pessoa trabalha na sua branch: `feature/nome-da-pessoa`.
2. Cada pessoa cria **classes com nomes só dela** (ex.: `Produto`, `ArquivoProduto`, `ProdutoController`), para não haver conflito.
3. Arquivos compartilhados: `MainController.java`, `CadastroApp.java` e `pom.xml`. Só **uma pessoa** edita (responsável: Lucas). Os demais pedem o botão novo ou enviam o trecho.
4. Sempre dar `git pull` antes de começar a trabalhar.
5. Mensagens de commit curtas e no padrão: `feat: ...`, `fix: ...`, `docs: ...`.
6. Colocar o **autor** em comentário no topo de cada classe (necessário para o PDF e para a arguição).
7. Registrar todo uso de IA na tabela do RA3 (IA, prompt, data/hora, motivo).
