package com.example.check_in_service.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Renders a QR payload to a PNG image. Used only for the printable segment poster endpoint -
 * the app itself renders QR codes client-side from the raw payload (see QrController).
 */
public final class QrCodeImageGenerator {

    private QrCodeImageGenerator() {
    }

    public static byte[] generatePng(String payload, int size) throws WriterException, IOException {
        BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }
}
