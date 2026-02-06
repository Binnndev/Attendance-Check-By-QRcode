package com.androidapp.attendencecheckqrcode.ui.login;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.androidapp.attendencecheckqrcode.R;

import android.content.Intent; // <--- Nhớ import Intent

import com.androidapp.attendencecheckqrcode.ui.home.HomeActivity;
import com.androidapp.attendencecheckqrcode.ui.signup.SignUpActivity; // <--- Import màn hình Sign Up

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageView btnShowPass;
    private TextView tvForgotPassword, tvSignUp;
    private CheckBox cbRemember;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ẩn Action Bar nếu cần
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 1. Ánh xạ View
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnShowPass = findViewById(R.id.btnShowPass);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp); //sss
        cbRemember = findViewById(R.id.cbRemember);

        // 2. Xử lý sự kiện hiện/ẩn mật khẩu
        btnShowPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Ẩn mật khẩu
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    // Đổi icon (giả sử bạn có icon eye_off)
                    // btnShowPass.setImageResource(R.drawable.ic_eye_on);
                } else {
                    // Hiện mật khẩu
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    // btnShowPass.setImageResource(R.drawable.ic_eye_off);
                }
                // Di chuyển con trỏ về cuối
                etPassword.setSelection(etPassword.getText().length());
                isPasswordVisible = !isPasswordVisible;
            }
        });

        // 3. Xử lý sự kiện nút Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Xử lý logic đăng nhập ở đây
                    Toast.makeText(LoginActivity.this, "Logging in with: " + email, Toast.LENGTH_SHORT).show();

                    // Trong LoginActivity.java -> hàm loginUser thành công:
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish(); // Đóng Login lại để user không back về được
                }
            }
        });

        // 4. Xử lý sự kiện Sign Up
        tvSignUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(LoginActivity.this, "Go to Sign Up Screen", Toast.LENGTH_SHORT).show();
//            }
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển màn hình
                // Từ: LoginActivity.this (Màn hình hiện tại)
                // Đến: SignUpActivity.class (Màn hình muốn tới)
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }
}