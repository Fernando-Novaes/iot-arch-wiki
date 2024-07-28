package br.ufrj.cos.components.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class QRCodeComponent {

    public Image generateQRCode(String text, int width, int height) {

        return new Image(generateBarcodeImage(text, width, height), "Barcode");
    }

    private StreamResource generateBarcodeImage(String text, int width, int height) {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix;

        try {
            bitMatrix = barcodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        try {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new StreamResource("barcode.png", () -> new ByteArrayInputStream(pngOutputStream.toByteArray()));
    }

}
