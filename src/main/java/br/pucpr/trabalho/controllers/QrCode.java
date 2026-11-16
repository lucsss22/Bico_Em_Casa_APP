// Autor: Tayllor Vinicius Martins dos Santos
package br.pucpr.trabalho.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Map;

/**
 * Transforma um texto em imagem de QR Code para exibir no JavaFX.
 * A biblioteca ZXing calcula a matriz de quadradinhos (BitMatrix);
 * aqui pintamos cada ponto de preto ou branco em uma WritableImage.
 */
public final class QrCode {

    private QrCode() {
    }

    public static Image gerar(String texto, int tamanho) throws WriterException {
        BitMatrix matriz = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tamanho, tamanho,
                Map.of(EncodeHintType.MARGIN, 1));

        WritableImage imagem = new WritableImage(matriz.getWidth(), matriz.getHeight());
        PixelWriter pincel = imagem.getPixelWriter();
        for (int x = 0; x < matriz.getWidth(); x++) {
            for (int y = 0; y < matriz.getHeight(); y++) {
                pincel.setColor(x, y, matriz.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return imagem;
    }
}
