package com.androidapp.attendencecheckqrcode.ui.signup;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.androidapp.attendencecheckqrcode.R;
import com.androidapp.attendencecheckqrcode.api.ApiClient;
import com.androidapp.attendencecheckqrcode.models.AuthResponse;
import com.androidapp.attendencecheckqrcode.models.RegisterRequest;
import com.androidapp.attendencecheckqrcode.ui.login.LoginActivity;

import java.util.Calendar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private ImageView btnBack, btnShowPass;
    private TextView tvLoginLink;
    private Button btnSignUp;
    private LinearLayout btnGoogle;

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
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
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

        btnShowPass.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            etPassword.setSelection(etPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        btnSignUp.setOnClickListener(v -> handleSignUp());
    }

    private void handleSignUp() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Vui lòng điền đủ tất cả các trường!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSignUp.setEnabled(false);
        btnSignUp.setText("Đang xử lý...");

        RegisterRequest request = new RegisterRequest(firstName, lastName, email, phone, dob, password);

        ApiClient.getApiService(this).registerUser(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnSignUp.setEnabled(true);
                btnSignUp.setText("Đăng ký");

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Toast.makeText(SignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(SignUpActivity.this, "Lỗi từ máy chủ!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnSignUp.setEnabled(true);
                btnSignUp.setText("Đăng ký");
                Toast.makeText(SignUpActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            etDob.setText(d + "/" + (m + 1) + "/" + y);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}