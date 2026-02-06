package com.androidapp.attendencecheckqrcode.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.androidapp.attendencecheckqrcode.R;

// Giả sử bạn sẽ tạo các Activity này sau (hoặc thay tên Activity đã có của bạn vào)
import com.androidapp.attendencecheckqrcode.ui.attendance.AttendanceActivity;
import com.androidapp.attendencecheckqrcode.ui.clazz.CreateClassActivity;
import com.androidapp.attendencecheckqrcode.ui.settings.SettingsActivity;
import com.androidapp.attendencecheckqrcode.ui.stats.StatsActivity;

public class HomeActivity extends AppCompatActivity {

    // Khai báo biến
    private LinearLayout btnQR;
    private CardView btnAttendance, btnCreateClass, btnStats, btnSettings;
    private TextView tvHome, tvExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        // Ánh xạ các view theo ID đã đặt trong XML
        btnQR = findViewById(R.id.btnQR);

        btnAttendance = findViewById(R.id.btnAttendance);
        btnCreateClass = findViewById(R.id.btnCreateClass);
        btnStats = findViewById(R.id.btnStats);
        btnSettings = findViewById(R.id.btnSettings);

        tvHome = findViewById(R.id.tvHome);
        tvExport = findViewById(R.id.tvExport);
    }

    private void setupListeners() {
        // 1. Sự kiện nút QR
        btnQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Mở quét mã QR...", Toast.LENGTH_SHORT).show();

                // Chuyển sang màn hình quét QR
                Intent intent = new Intent(HomeActivity.this, com.androidapp.attendencecheckqrcode.ui.qr.QRScanActivity.class);
                startActivity(intent);
            }
        });

        // 2. Sự kiện các nút chức năng (Grid)

        // Điểm danh
        btnAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang màn hình Điểm Danh
                Intent intent = new Intent(HomeActivity.this, AttendanceActivity.class);
                startActivity(intent);
            }
        });

        // Tạo lớp học
        btnCreateClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, CreateClassActivity.class);
                startActivity(intent);
            }
        });

        // Thống kê
        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, StatsActivity.class);
                startActivity(intent);
            }
        });

        // Cài đặt
//        btnSettings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
//                startActivity(intent);
//            }
//        });
        // Trong HomeActivity.java
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, com.androidapp.attendencecheckqrcode.ui.settings.SettingsActivity.class);
            startActivity(intent);
        });

        // 3. Sự kiện Bottom Navigation

        // Nút Trang chủ -> Load lại trang (Refresh)
        tvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Đang tải lại trang chủ...", Toast.LENGTH_SHORT).show();
                recreate(); // Hàm này sẽ khởi động lại Activity hiện tại
            }
        });

        // Nút Xuất file -> Thông báo tính năng phát triển sau
        tvExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Tính năng Xuất file đang phát triển!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}