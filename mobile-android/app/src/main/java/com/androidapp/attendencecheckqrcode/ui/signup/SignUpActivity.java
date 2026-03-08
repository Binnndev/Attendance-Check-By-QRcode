package com.androidapp.attendencecheckqrcode.ui.signup;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
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
import com.androidapp.attendencecheckqrcode.models.User;
import com.androidapp.attendencecheckqrcode.ui.login.LoginActivity;
import com.androidapp.attendencecheckqrcode.utils.MockData;

import java.util.Calendar;

// [FUTURE API] import retrofit2.Call...

public class SignUpActivity extends AppCompatActivity {

    private ImageView btnBack, btnShowPass;
    private TextView tvLoginLink;
    private Button btnSignUp;
    private LinearLayout btnGoogle;

    // Các trường nhập liệu
    private EditText etFirstName, etLastName, etEmail, etDob, etPhone, etPassword;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Ánh xạ View (Đảm bảo file XML đã có các ID này)
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone); // Cần thêm EditText này vào XML
        etDob = findViewById(R.id.etDob);
        etPassword = findViewById(R.id.etPassword);

        btnShowPass = findViewById(R.id.btnShowPass);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogle = findViewById(R.id.btnGoogle);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        tvLoginLink.setOnClickListener(v -> finish());
        etDob.setOnClickListener(v -> showDatePicker());

        // Ẩn/Hiện mật khẩu
        btnShowPass.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            etPassword.setSelection(etPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        // Xử lý Đăng ký
        btnSignUp.setOnClickListener(v -> handleSignUp());
    }

    private void handleSignUp() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 1. Validation (Kiểm tra dữ liệu)
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Vui lòng điền đủ tất cả các trường!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. [MOCK] Lưu vào file .txt
        User newUser = new User(email, password, firstName, lastName, dob, phone);
        MockData.saveUserToTextFile(this, newUser);

        Toast.makeText(this, "Đăng ký thành công! Đã lưu vào máy.", Toast.LENGTH_LONG).show();

        // 3. [FUTURE API] Vị trí gọi API sau này
        /*
        ApiService api = ApiClient.getClient().create(ApiService.class);
        RegisterRequest req = new RegisterRequest(email, password, firstName, lastName...);
        api.register(req).enqueue(new Callback<User>() { ... });
        */

        // Chuyển về trang Login và xóa các màn hình trước đó
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    etDob.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }
}