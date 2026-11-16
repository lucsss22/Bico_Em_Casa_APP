package br.pucpr.trabalho.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

// Transforma um texto em imagem de QR Code (biblioteca ZXing)
public class QrCode {

    public static Image gerar(String texto, int tamanho) throws WriterException {
        BitMatrix matriz = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tamanho, tamanho);
        WritableImage imagem = new WritableImage(matriz.getWidth(), matriz.getHeight());
        PixelWriter pincel = imagem.getPixelWriter();
        for (int x = 0; x < matriz.getWidth(); x++) {
            for (int y = 0; y < matriz.getHeight(); y++) {
                if (matriz.get(x, y)) {
                    pincel.setColor(x, y, Color.BLACK);
                } else {
                    pincel.setColor(x, y, Color.WHITE);
                }
            }
        }
        return imagem;
    }
}
