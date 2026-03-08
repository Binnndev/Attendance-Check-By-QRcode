package com.androidapp.attendencecheckqrcode.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.androidapp.attendencecheckqrcode.R;
import com.androidapp.attendencecheckqrcode.models.User;
import com.androidapp.attendencecheckqrcode.ui.home.HomeActivity;
import com.androidapp.attendencecheckqrcode.ui.signup.SignUpActivity;
import com.androidapp.attendencecheckqrcode.utils.MockData;

// Import retrofit nếu muốn dùng API sau này
// import retrofit2.Call;
// import retrofit2.Callback;
// import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageView btnShowPass;
    private TextView tvSignUp, tvForgotPassword;
    private CheckBox cbRemember;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnShowPass = findViewById(R.id.btnShowPass);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        cbRemember = findViewById(R.id.cbRemember);
    }

    private void setupListeners() {
        // 1. Ẩn/Hiện mật khẩu
        btnShowPass.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Đang hiện -> Chuyển sang Ẩn
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                // btnShowPass.setImageResource(R.drawable.ic_eye_on);
            } else {
                // Đang ẩn -> Chuyển sang Hiện
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                // btnShowPass.setImageResource(R.drawable.ic_eye_off);
            }
            etPassword.setSelection(etPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        // 2. Chuyển sang màn hình Đăng ký
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // 3. Xử lý Đăng nhập
        btnLogin.setOnClickListener(v -> handleLogin());

        // 4. Quên mật khẩu (Demo)
        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
        );
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ Email và Mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- GIAI ĐOẠN 1: DÙNG MOCK DATA (ĐỌC FILE TXT) ---
        // Thêm tham số 'this' để MockData có thể truy cập file hệ thống
        User user = MockData.checkLogin(this, email, password);

        if (user != null) {
            processLoginSuccess(user);
        } else {
            Toast.makeText(this, "Sai Email hoặc Mật khẩu!", Toast.LENGTH_SHORT).show();
        }

        // --- GIAI ĐOẠN 2: GỌI API (Tương lai - Giữ nguyên làm mẫu) ---
        /*
        ApiService api = ApiClient.getClient().create(ApiService.class);
        // Tạo object gửi đi
        LoginRequest request = new LoginRequest(email, password);

        api.login(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processLoginSuccess(response.body());
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối Server!", Toast.LENGTH_SHORT).show();
            }
        });
        */
    }

    // Hàm xử lý chung khi đăng nhập thành công
    private void processLoginSuccess(User user) {
        Toast.makeText(this, "Xin chào: " + user.getFullName(), Toast.LENGTH_SHORT).show();

        // LOGIC MỚI: TẤT CẢ USER ĐỀU VÀO TRANG CHỦ (HomeActivity)
        // Việc phân quyền Giảng viên/Sinh viên sẽ nằm ở từng Lớp học cụ thể
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);

        // Truyền toàn bộ đối tượng User sang màn hình sau để hiển thị tên
        intent.putExtra("currentUser", user);

        startActivity(intent);
        finish(); // Đóng LoginActivity để người dùng không back lại được
    }
}