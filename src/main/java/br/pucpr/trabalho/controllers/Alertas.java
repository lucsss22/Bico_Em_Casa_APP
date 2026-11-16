// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Atalhos para mostrar caixas de mensagem ao usuário.
 */
public final class
Alertas {

    private Alertas() {
    }

    public static void erro(String mensagem) {
        mostrar(Alert.AlertType.ERROR, "Erro", mensagem);
    }

    public static void info(String mensagem) {
        mostrar(Alert.AlertType.INFORMATION, "Informação", mensagem);
    }

    /** Pergunta Sim/Não. Devolve true se o usuário clicou em OK. */
    public static boolean confirmar(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmação");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        return alerta.showAndWait().filter(botao -> botao == ButtonType.OK).isPresent();
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
