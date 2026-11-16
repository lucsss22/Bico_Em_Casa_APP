// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.domains.StatusPagamento;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Pequenos ajudantes de aparência usados pelas telas.
 */
public final class Estilo {

    private Estilo() {
    }

    /** Título de tela em negrito. */
    public static Label titulo(String texto) {
        Label titulo = new Label(texto);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        return titulo;
    }

    /** Pinta o texto com a cor do status do pagamento. */
    public static void corDoStatus(Labeled componente, StatusPagamento status) {
        if (status == null) {
            componente.setStyle("");
            return;
        }
        String cor = switch (status) {
            case PENDENTE -> "darkorange";
            case AGUARDANDO_CONFIRMACAO -> "blue";
            case PAGO -> "green";
            case ESTORNADO -> "red";
        };
        componente.setStyle("-fx-text-fill: " + cor + "; -fx-font-weight: bold;");
    }
}
