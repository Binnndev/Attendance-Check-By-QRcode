package com.androidapp.attendencecheckqrcode.ui.signup;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.androidapp.attendencecheckqrcode.R;

import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity {

    // Khai báo các biến View
    private ImageView btnBack;
    private TextView tvLoginLink;
    private EditText etDob;

    // --- 1. Khai báo thêm các biến mới ---
    private EditText etPassword;
    private ImageView btnShowPass;
    private Button btnSignUp;
    private LinearLayout btnGoogle; // Nút Google trong XML là LinearLayout

    // Biến cờ để theo dõi trạng thái ẩn/hiện mật khẩu
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Ẩn Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        // Ánh xạ các view cũ
        btnBack = findViewById(R.id.btnBack);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        etDob = findViewById(R.id.etDob);

        // --- 2. Ánh xạ các view mới ---
        etPassword = findViewById(R.id.etPassword);
        btnShowPass = findViewById(R.id.btnShowPass);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogle = findViewById(R.id.btnGoogle);
    }

    private void setupListeners() {
        // Xử lý nút Back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Xử lý link Login
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Xử lý chọn ngày sinh
        etDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // --- 3. Sự kiện Click nút Đăng Ký (Sign Up) ---
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ở đây bạn có thể thêm logic kiểm tra dữ liệu (validation) trước khi thông báo
                // Ví dụ: if (etEmail.getText().toString().isEmpty()) { ... }

                Toast.makeText(SignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                // Sau khi đăng ký thành công thì có thể đóng màn hình này hoặc chuyển trang
                // finish();
            }
        });

        // --- 4. Sự kiện Click nút Google ---
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignUpActivity.this, "Đang kết nối với Google...", Toast.LENGTH_SHORT).show();
                // Logic đăng nhập Google thực tế sẽ viết ở đây (dùng Firebase Auth, v.v.)
            }
        });

        // --- 5. Sự kiện Ẩn/Hiện Mật khẩu (Click icon mắt) ---
        btnShowPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Đang hiện -> Chuyển sang Ẩn
                    // Đặt lại kiểu hiển thị là Password (dấu chấm tròn)
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

                    // Đổi icon mắt (Nếu bạn có icon mắt gạch chéo thì thay vào đây)
                    // btnShowPass.setImageResource(R.drawable.ic_eye_off);
                    // btnShowPass.setAlpha(0.5f); // Ví dụ làm mờ icon đi để báo hiệu đang ẩn
                } else {
                    // Đang ẩn -> Chuyển sang Hiện
                    // Đặt lại kiểu hiển thị là Text thường
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                    // Đổi icon mắt mở
                    // btnShowPass.setImageResource(R.drawable.ic_eye_on);
                    // btnShowPass.setAlpha(1.0f);
                }

                // Đảo ngược trạng thái
                isPasswordVisible = !isPasswordVisible;

                // Quan trọng: Di chuyển con trỏ nháy về cuối dòng chữ sau khi ẩn/hiện
                // Nếu không có dòng này, con trỏ sẽ nhảy về đầu dòng rất khó chịu
                etPassword.setSelection(etPassword.getText().length());
            }
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                SignUpActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        etDob.setText(selectedDate);
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }
}