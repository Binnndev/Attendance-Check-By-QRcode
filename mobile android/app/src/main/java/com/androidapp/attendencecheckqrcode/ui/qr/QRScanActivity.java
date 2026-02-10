package com.androidapp.attendencecheckqrcode.ui.qr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.androidapp.attendencecheckqrcode.R;
import com.androidapp.attendencecheckqrcode.models.User;
import com.androidapp.attendencecheckqrcode.utils.MockData;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QRScanActivity extends AppCompatActivity {

    private PreviewView viewFinder;
    private ExecutorService cameraExecutor;
    private static final int REQUEST_CODE_CAMERA = 10;

    private boolean isProcessing = false;
    private ImageView btnSwitchCamera;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private View viewLaser;

    // User hiện tại đang quét
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);

        // --- NHẬN USER TỪ INTENT ---
        // (Nếu từ Home hoặc ClassDetail chưa truyền thì cần bổ sung putExtra ở đó)
        if (getIntent().hasExtra("currentUser")) {
            currentUser = (User) getIntent().getSerializableExtra("currentUser");
        } else {
            // Fallback lấy user mặc định nếu null (để tránh crash khi test lẻ)
            java.util.List<User> users = MockData.getAllUsers(this);
            if (!users.isEmpty()) currentUser = users.get(0);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        viewFinder = findViewById(R.id.viewFinder);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        viewLaser = findViewById(R.id.viewLaser);

        setupAnimation();
        setupListeners();
        checkPermissionAndStartCamera();
    }

    private void setupAnimation() {
        final float scale = getResources().getDisplayMetrics().density;
        int distanceInPixels = (int) (140 * scale + 0.5f);

        TranslateAnimation animation = new TranslateAnimation(
                0, 0,
                -distanceInPixels,
                distanceInPixels
        );
        animation.setDuration(2500);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        viewLaser.startAnimation(animation);
    }

    private void setupListeners() {
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        btnSwitchCamera.setOnClickListener(v -> {
            lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK) ?
                    CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
            startCamera();
        });
    }

    private void checkPermissionAndStartCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // --- GẮN ANALYZER ---
                imageAnalysis.setAnalyzer(cameraExecutor, new QRCodeAnalyzer(qrCode -> {
                    // Khi quét được mã, chạy trên UI Thread để xử lý
                    runOnUiThread(() -> handleQRCodeResult(qrCode));
                }));

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("QRScan", "Lỗi mở camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // --- HÀM XỬ LÝ KẾT QUẢ QUÉT ĐƯỢC ---
    private void handleQRCodeResult(String qrCode) {
        if (isProcessing) return;
        isProcessing = true; // Khóa để không quét trùng lặp

        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Không xác định được người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            // Gọi MockData để xử lý (Tham gia lớp + Điểm danh)
            MockData.processQRCode(this, currentUser, qrCode);

            Toast.makeText(this, "Điểm danh thành công!", Toast.LENGTH_LONG).show();

            // Có thể trả kết quả về màn hình trước
            Intent resultIntent = new Intent();
            resultIntent.putExtra("scanned_qr", qrCode);
            setResult(RESULT_OK, resultIntent);

        } catch (Exception e) {
            Toast.makeText(this, "Mã QR không hợp lệ hoặc lỗi xử lý", Toast.LENGTH_SHORT).show();
        }

        // Đóng màn hình quét
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Cần quyền Camera để quét mã", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}

//package com.androidapp.attendencecheckqrcode.ui.qr;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.ImageAnalysis;
//import androidx.camera.core.Preview;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.view.PreviewView;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.androidapp.attendencecheckqrcode.R;
//import com.google.common.util.concurrent.ListenableFuture;
//
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import android.widget.ImageView; // Import thêm ImageView
//
//// ... import ...
//import android.view.animation.Animation;
//import android.view.animation.TranslateAnimation;
//
//
//public class QRScanActivity extends AppCompatActivity {
//
//    private PreviewView viewFinder;
//    private ExecutorService cameraExecutor;
//    private static final int REQUEST_CODE_CAMERA = 10;
//
//    // Cờ để tránh việc quét liên tục 1 mã nhiều lần trong 1 giây
//    private boolean isProcessing = false;
//
//    private ImageView btnSwitchCamera; // Khai báo nút mới
//
//    // Mặc định dùng Camera SAU
//    private int lensFacing = CameraSelector.LENS_FACING_BACK;
//
//    private View viewLaser; // Khai báo thêm viewLaser
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_qrscan);
//
//        // --- KHỞI TẠO EXECUTOR TRƯỚC KHI DÙNG CAMERA ---
//        cameraExecutor = Executors.newSingleThreadExecutor();
//
//        // --- 1. ÁNH XẠ VIEW (QUAN TRỌNG: Phải ánh xạ trước khi dùng) ---
//        viewFinder = findViewById(R.id.viewFinder);
//        btnSwitchCamera = findViewById(R.id.btnSwitchCamera); // Ánh xạ nút switch camara
//        viewLaser = findViewById(R.id.viewLaser);
//
//        // --- 2. CODE TẠO HIỆU ỨNG ANIMATION ---
//        // Lấy mật độ điểm ảnh của màn hình để quy đổi dp sang pixel
//        final float scale = getResources().getDisplayMetrics().density;
//        // Chiều cao khung là 280dp, nên khoảng cách từ tâm đến mép là 140dp.
//        // Công thức đổi dp -> px: dp * scale + 0.5f (để làm tròn)
//        int distanceInPixels = (int) (140 * scale + 0.5f);
//
//        // Tạo hiệu ứng: Di chuyển từ trên cao (-distance) xuống dưới thấp (+distance)
//        TranslateAnimation animation = new TranslateAnimation(
//                0, 0,                 // Không di chuyển theo chiều ngang (X)
//                -distanceInPixels,    // Bắt đầu Y: Dịch lên mép trên
//                distanceInPixels      // Kết thúc Y: Dịch xuống mép dưới
//        );
//
//        animation.setDuration(2500); // Tăng lên 2.5 giây cho mượt hơn chút
//        animation.setRepeatCount(Animation.INFINITE); // Lặp vô tận
//        animation.setRepeatMode(Animation.REVERSE); // Chạy xuống rồi chạy ngược lên
//
//        // Bắt đầu chạy
//        viewLaser.startAnimation(animation);
//
//        // --- 3. CÁC SỰ KIỆN KHÁC ---
//        // Nút đóng màn hình
//        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
//        // Xử lý nut chuyen camara
//        btnSwitchCamera.setOnClickListener(v -> {
//            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
//                lensFacing = CameraSelector.LENS_FACING_FRONT;
//            } else {
//                lensFacing = CameraSelector.LENS_FACING_BACK;
//            }
//            // Khởi động lại camera với hướng mới
//            startCamera();
//        });
//
//        // --- 4. KIỂM TRA QUYỀN VÀ MỞ CAMERA ---
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                == PackageManager.PERMISSION_GRANTED) {
//            startCamera();
//        } else {
//            // Xin quyền nếu chưa có
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
//        }
//    }
//
//    private void startCamera() {
//        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
//                ProcessCameraProvider.getInstance(this);
//
//        cameraProviderFuture.addListener(() -> {
//            try {
//                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//
//                // 1. Preview
//                Preview preview = new Preview.Builder().build();
//                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
//
//                // 2. ImageAnalysis
//                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
//                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                        .build();
//
//                imageAnalysis.setAnalyzer(cameraExecutor, new QRCodeAnalyzer(qrCode -> {
//                    runOnUiThread(() -> {
//                        Toast.makeText(this, "Mã: " + qrCode, Toast.LENGTH_SHORT).show();
//                        // Xử lý xong thì đóng: finish();
//                    });
//                }));
//
//                // 3. (SỬA ĐỔI) Chọn Camera dựa trên biến lensFacing
//                CameraSelector cameraSelector = new CameraSelector.Builder()
//                        .requireLensFacing(lensFacing)
//                        .build();
//
//                // 4. Bind to lifecycle
//                cameraProvider.unbindAll(); // Hủy kết nối cũ trước khi tạo mới
//
//                try {
//                    cameraProvider.bindToLifecycle(
//                            this, cameraSelector, preview, imageAnalysis);
//                } catch (Exception exc) {
//                    Log.e("QRScan", "Không tìm thấy camera (có thể máy ảo không có cam trước)", exc);
//                    Toast.makeText(this, "Thiết bị không có Camera này", Toast.LENGTH_SHORT).show();
//                }
//
//            } catch (ExecutionException | InterruptedException e) {
//                Log.e("QRScan", "Lỗi mở camera", e);
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }
//
//    private void handleQRCodeResult(String qrCode) {
//        if (isProcessing) return;
//        isProcessing = true; // Khóa lại không cho quét tiếp
//
//        // HIỆN TẠI: Chỉ hiện Toast thông báo mã quét được
//        Toast.makeText(this, "Mã: " + qrCode, Toast.LENGTH_LONG).show();
//
//        // TƯƠNG LAI: Gọi API điểm danh ở đây
//        // checkAttendanceApi(qrCode);
//
//        // Đóng màn hình quét sau khi thành công
//        finish();
//    }
//
//    // Xử lý kết quả xin quyền
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CODE_CAMERA) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startCamera();
//            } else {
//                Toast.makeText(this, "Bạn cần cấp quyền Camera để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        cameraExecutor.shutdown();
//    }
//}