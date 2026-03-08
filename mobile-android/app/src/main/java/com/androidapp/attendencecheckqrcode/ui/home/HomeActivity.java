//package com.androidapp.attendencecheckqrcode.ui.home;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.text.Html;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.cardview.widget.CardView;
//
//import com.androidapp.attendencecheckqrcode.R;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//
//import com.androidapp.attendencecheckqrcode.ui.attendance.AttendanceActivity;
//import com.androidapp.attendencecheckqrcode.ui.clazz.CreateClassActivity;
//import com.androidapp.attendencecheckqrcode.ui.settings.SettingsActivity;
//import com.androidapp.attendencecheckqrcode.ui.stats.StatsActivity;
//import com.androidapp.attendencecheckqrcode.ui.qr.QRScanActivity;
//import com.androidapp.attendencecheckqrcode.ui.teaching.TeachingListActivity;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Locale;
//
//public class HomeActivity extends AppCompatActivity {
//
//    private FloatingActionButton fabQR;
//    private BottomNavigationView bottomNavigationView;
//
//    private CardView btnJoin, btnCreate, btnClass, btnTeaching;
//    private CardView itemClass1, itemClass2;
//
//    private TextView tvSummary;
//    private TextView tvDate;
//    private TextView tvGreeting;
//    private ImageView btnNotification;
//
//    // Handler để cập nhật thời gian liên tục
//    private Handler handler = new Handler();
//    private Runnable timeUpdater;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home);
//
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().hide();
//        }
//
//        initViews();
//        setupUI();
//        setupListeners();
//
//        // Bắt đầu cập nhật thời gian liên tục
//        startUpdatingTime();
//    }
//
//    private void initViews() {
//        fabQR = findViewById(R.id.fabQR);
//        bottomNavigationView = findViewById(R.id.bottomNavigationView);
//
//        btnJoin = findViewById(R.id.btnJoin);
//        btnCreate = findViewById(R.id.btnCreate);
//        btnClass = findViewById(R.id.btnClass);
//        btnTeaching = findViewById(R.id.btnTeaching);
//
//        itemClass1 = findViewById(R.id.itemClass1);
//        itemClass2 = findViewById(R.id.itemClass2);
//
//        tvSummary = findViewById(R.id.tvSummary);
//        btnNotification = findViewById(R.id.btnNotification);
//        tvDate = findViewById(R.id.tvDate);
//        tvGreeting = findViewById(R.id.tvGreeting);
//    }
//
//    private void setupUI() {
//        String text = "Bạn có <font color='#FFEB3B'><b>2 lớp học</b></font> hôm nay";
//        tvSummary.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
//
//        bottomNavigationView.setBackground(null);
//        if (bottomNavigationView.getMenu().size() >= 3) {
//            bottomNavigationView.getMenu().getItem(2).setEnabled(false);
//        }
//    }
//
//    private void updateCurrentDate() {
//        Date currentDate = new Date();
//        // Định dạng: "Thứ Hai 15/01 - 12:34:56"
//        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM - HH:mm:ss", new Locale("vi", "VN"));
//        String dateString = sdf.format(currentDate);
//
//        String finalString = "📅 HÔM NAY, " + dateString.toUpperCase();
//        tvDate.setText(finalString);
//    }
//
//    private void setupGreeting() {
//        Calendar calendar = Calendar.getInstance();
//        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        String greetingText;
//
//        if (hour >= 6 && hour < 12) {
//            greetingText = "Chào buổi sáng 🌤️";
//        } else if (hour >= 12 && hour < 18) {
//            greetingText = "Chào buổi chiều ☀️";
//        } else {
//            greetingText = "Chào buổi tối 🌙";
//        }
//
//        if (tvGreeting != null) {
//            tvGreeting.setText(greetingText);
//        }
//    }
//
//    private void startUpdatingTime() {
//        timeUpdater = new Runnable() {
//            @Override
//            public void run() {
//                updateCurrentDate();
//                setupGreeting();
//                handler.postDelayed(this, 1000); // cập nhật mỗi giây
//            }
//        };
//        handler.post(timeUpdater);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        handler.removeCallbacks(timeUpdater); // tránh leak bộ nhớ
//    }
//
//    private void setupListeners() {
//        fabQR.setOnClickListener(v -> {
//            Intent intent = new Intent(HomeActivity.this, QRScanActivity.class);
//            startActivity(intent);
//        });
//
//        btnJoin.setOnClickListener(v ->
//                Toast.makeText(HomeActivity.this, "Chức năng: Tham gia lớp", Toast.LENGTH_SHORT).show()
//        );
//
//        btnCreate.setOnClickListener(v -> {
//            Intent intent = new Intent(HomeActivity.this, CreateClassActivity.class);
//            startActivity(intent);
//        });
//
//        btnClass.setOnClickListener(v -> {
//            Intent intent = new Intent(HomeActivity.this, com.androidapp.attendencecheckqrcode.ui.clazz.ClassListActivity.class);
//            startActivity(intent);
//        });
//
//        btnTeaching.setOnClickListener(v -> {
//            Intent intent = new Intent(HomeActivity.this, TeachingListActivity.class);
//            startActivity(intent);
//        });
//
//        itemClass1.setOnClickListener(v ->
//                Toast.makeText(HomeActivity.this, "Chi tiết lớp: Lập trình Android", Toast.LENGTH_SHORT).show()
//        );
//
//        itemClass2.setOnClickListener(v ->
//                Toast.makeText(HomeActivity.this, "Chi tiết lớp: Trí tuệ nhân tạo", Toast.LENGTH_SHORT).show()
//        );
//
//        btnNotification.setOnClickListener(v ->
//                Toast.makeText(HomeActivity.this, "Bạn không có thông báo mới", Toast.LENGTH_SHORT).show()
//        );
//
//        bottomNavigationView.setOnItemSelectedListener(item -> {
//            int id = item.getItemId();
//
//            if (id == R.id.nav_home) {
//                Toast.makeText(this, "Đang ở Trang chủ", Toast.LENGTH_SHORT).show();
//                return true;
//
//            } else if (id == R.id.nav_stat) {
//                Intent intent = new Intent(HomeActivity.this, StatsActivity.class);
//                startActivity(intent);
//                return true;
//
//            } else if (id == R.id.nav_setting) {
//                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
//                startActivity(intent);
//                return true;
//
//            } else if (id == R.id.nav_profile) {
//                Toast.makeText(this, "Mở hồ sơ cá nhân", Toast.LENGTH_SHORT).show();
//                return true;
//            }
//
//            return false;
//        });
//    }
//}
package com.androidapp.attendencecheckqrcode.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.androidapp.attendencecheckqrcode.R;
import com.androidapp.attendencecheckqrcode.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// Import các màn hình con
import com.androidapp.attendencecheckqrcode.ui.clazz.CreateClassActivity;
import com.androidapp.attendencecheckqrcode.ui.clazz.ClassListActivity;
import com.androidapp.attendencecheckqrcode.ui.settings.SettingsActivity;
import com.androidapp.attendencecheckqrcode.ui.stats.StatsActivity;
import com.androidapp.attendencecheckqrcode.ui.qr.QRScanActivity;
import com.androidapp.attendencecheckqrcode.ui.teaching.TeachingListActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private FloatingActionButton fabQR;
    private BottomNavigationView bottomNavigationView;

    private CardView btnJoin, btnCreate, btnClass, btnTeaching;
    private CardView itemClass1, itemClass2;

    private TextView tvSummary;
    private TextView tvDate;
    private TextView tvGreeting;
    private TextView tvName; // <--- THÊM BIẾN NÀY ĐỂ HIỂN THỊ TÊN
    private ImageView btnNotification;

    private Handler handler = new Handler();
    private Runnable timeUpdater;

    // Biến lưu thông tin User hiện tại
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        getUserDataFromIntent(); // <--- LẤY DỮ LIỆU USER
        setupUI();
        setupListeners();

        startUpdatingTime();
    }

    private void initViews() {
        fabQR = findViewById(R.id.fabQR);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        btnJoin = findViewById(R.id.btnJoin);
        btnCreate = findViewById(R.id.btnCreate);
        btnClass = findViewById(R.id.btnClass);
        btnTeaching = findViewById(R.id.btnTeaching);

        itemClass1 = findViewById(R.id.itemClass1);
        itemClass2 = findViewById(R.id.itemClass2);

        tvSummary = findViewById(R.id.tvSummary);
        btnNotification = findViewById(R.id.btnNotification);
        tvDate = findViewById(R.id.tvDate);
        tvGreeting = findViewById(R.id.tvGreeting);

        tvName = findViewById(R.id.tvName);
    }

    // Hàm nhận dữ liệu User từ màn hình Login gửi sang
    private void getUserDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("currentUser")) {
            currentUser = (User) intent.getSerializableExtra("currentUser");

            // Hiển thị tên lên giao diện
            if (currentUser != null && tvName != null) {
                tvName.setText(currentUser.getFullName());
            }
        } else {
            // Nếu không có dữ liệu (chạy thẳng Home), đặt tên mặc định
            if (tvName != null) tvName.setText("Khách");
        }
    }

    private void setupUI() {
        String text = "Bạn có <font color='#FFEB3B'><b>2 lớp học</b></font> hôm nay";
        tvSummary.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));

        bottomNavigationView.setBackground(null);
        if (bottomNavigationView.getMenu().size() >= 3) {
            bottomNavigationView.getMenu().getItem(2).setEnabled(false);
        }
    }

    private void updateCurrentDate() {
        Date currentDate = new Date();
        // Định dạng: "Thứ Hai 15/01 - 12:34"
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM - HH:mm", new Locale("vi", "VN"));
        String dateString = sdf.format(currentDate);
        String finalString = "📅 HÔM NAY, " + dateString.toUpperCase();

        if (tvDate != null) tvDate.setText(finalString);
    }

    private void setupGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greetingText;

        if (hour >= 6 && hour < 12) {
            greetingText = "Chào buổi sáng 🌤️";
        } else if (hour >= 12 && hour < 18) {
            greetingText = "Chào buổi chiều ☀️";
        } else {
            greetingText = "Chào buổi tối 🌙";
        }

        if (tvGreeting != null) {
            tvGreeting.setText(greetingText);
        }
    }

    private void startUpdatingTime() {
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                updateCurrentDate();
                setupGreeting();
                handler.postDelayed(this, 60000); // Cập nhật mỗi 1 phút
            }
        };
        handler.post(timeUpdater);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && timeUpdater != null) {
            handler.removeCallbacks(timeUpdater);
        }
    }

    private void setupListeners() {
        fabQR.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, QRScanActivity.class);
            // QUAN TRỌNG: Truyền User sang
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        });

        btnJoin.setOnClickListener(v ->
                Toast.makeText(HomeActivity.this, "Chức năng: Tham gia lớp", Toast.LENGTH_SHORT).show()
        );

        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateClassActivity.class);
            // --- QUAN TRỌNG: Phải truyền User hiện tại sang ---
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        });

        // Đi tới danh sách lớp học (tư cách sinh viên)
        btnClass.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ClassListActivity.class);
            // --- THÊM DÒNG NÀY ---
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        });

        // Đi tới danh sách lớp dạy (tư cách giảng viên - người tạo lớp)
        btnTeaching.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TeachingListActivity.class);
            // --- THÊM DÒNG NÀY ---
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        });

        // ... các listener khác giữ nguyên
        btnNotification.setOnClickListener(v ->
                Toast.makeText(HomeActivity.this, "Bạn không có thông báo mới", Toast.LENGTH_SHORT).show()
        );

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_stat) {
                startActivity(new Intent(HomeActivity.this, StatsActivity.class));
                return true;
            }
            if (id == R.id.nav_setting) {
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                return true;
            }
            // ...
            return false;
        });
    }
}