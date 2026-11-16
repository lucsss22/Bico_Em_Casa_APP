package br.pucpr.trabalho.controllers;

import br.pucpr.trabalho.domains.StatusPagamento;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Estilo {

    public static Label titulo(String texto) {
        Label titulo = new Label(texto);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        return titulo;
    }

    public static void corDoStatus(Label label, StatusPagamento status) {
        String cor = "black";
        if (status == StatusPagamento.PENDENTE) {
            cor = "darkorange";
        } else if (status == StatusPagamento.AGUARDANDO_CONFIRMACAO) {
            cor = "blue";
        } else if (status == StatusPagamento.PAGO) {
            cor = "green";
        } else if (status == StatusPagamento.ESTORNADO) {
            cor = "red";
        }
        label.setStyle("-fx-text-fill: " + cor + "; -fx-font-weight: bold;");
    }
}
