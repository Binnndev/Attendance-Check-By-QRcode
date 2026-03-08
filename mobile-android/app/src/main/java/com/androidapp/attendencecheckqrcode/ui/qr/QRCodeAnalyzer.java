package com.androidapp.attendencecheckqrcode.ui.qr;

//package com.androidapp.attendencecheckqrcode.ui.qr;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

public class QRCodeAnalyzer implements ImageAnalysis.Analyzer {

    // Interface để gửi kết quả về cho Activity
    public interface OnQRCodeScannedListener {
        void onScanned(String qrCode);
    }

    private final OnQRCodeScannedListener listener;

    public QRCodeAnalyzer(OnQRCodeScannedListener listener) {
        this.listener = listener;
    }

    @Override
    @SuppressLint("UnsafeOptInUsageError")
    public void analyze(@NonNull ImageProxy imageProxy) {
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            // Chuyển đổi ảnh Camera sang InputImage của ML Kit
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            // Cấu hình chỉ quét QR Code (để tối ưu tốc độ)
            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build();

            BarcodeScanner scanner = BarcodeScanning.getClient(options);

            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        // Nếu tìm thấy mã
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            if (rawValue != null) {
                                listener.onScanned(rawValue);
                            }
                        }
                    })
                    .addOnCompleteListener(task -> {
                        // QUAN TRỌNG: Phải đóng imageProxy để nhận frame tiếp theo
                        imageProxy.close();
                    });
        } else {
            imageProxy.close();
        }
    }
}
