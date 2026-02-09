package com.androidapp.attendencecheckqrcode.ui.clazz;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.androidapp.attendencecheckqrcode.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateClassActivity extends AppCompatActivity {

    // Views
    private ImageView btnBack;
    private TextView tvStartTime, tvEndTime, tvSemester; // Thêm tvSemester
    private EditText etClassName, etSubjectCode, etClassCode, etRoom, etDescription; // Thêm các EditText
    private Button btnCreate; // Nút tạo lớp

    // Counter Variables
    private TextView tvTotalSessions, tvMaxAbsence;
    private ImageView btnMinusSession, btnPlusSession, btnMinusAbsent, btnPlusAbsent;
    private int totalSessions = 11;
    private int maxAbsence = 3;

    // Day Selection
    private TextView[] dayViews;
    private int[] dayIds = {R.id.tvDay2, R.id.tvDay3, R.id.tvDay4, R.id.tvDay5, R.id.tvDay6, R.id.tvDay7, R.id.tvDay8};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initViews();
        setupListeners();
        setDefaultSemester(); // Tự động set học kỳ khi mở màn hình
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);

        // Ánh xạ EditText nhập liệu
        etClassName = findViewById(R.id.etClassName);
        etSubjectCode = findViewById(R.id.etSubjectCode);
        etClassCode = findViewById(R.id.etClassCode);
        etRoom = findViewById(R.id.etRoom);
        etDescription = findViewById(R.id.etDescription);

        // Ánh xạ TextView chọn thời gian/học kỳ
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        tvSemester = findViewById(R.id.tvSemester);

        // Ánh xạ nút tạo
        btnCreate = findViewById(R.id.btnCreate);

        // Counters
        tvTotalSessions = findViewById(R.id.tvTotalSessions);
        btnMinusSession = findViewById(R.id.btnMinusSession);
        btnPlusSession = findViewById(R.id.btnPlusSession);

        tvMaxAbsence = findViewById(R.id.tvMaxAbsence);
        btnMinusAbsent = findViewById(R.id.btnMinusAbsent);
        btnPlusAbsent = findViewById(R.id.btnPlusAbsent);

        // Khởi tạo mảng các nút chọn ngày
        dayViews = new TextView[7];
        for (int i = 0; i < 7; i++) {
            dayViews[i] = findViewById(dayIds[i]);
        }
    }

    private void setupListeners() {
        // 1. Nút Back
        btnBack.setOnClickListener(v -> finish());

        // 2. Chọn giờ (TimePicker)
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));

        // 3. Chọn Học kỳ (Hiện Dialog) - MỚI
        tvSemester.setOnClickListener(v -> showSemesterPicker());

        // 4. Xử lý Tăng/Giảm Số buổi
        btnMinusSession.setOnClickListener(v -> {
            if (totalSessions > 1) {
                totalSessions--;
                tvTotalSessions.setText(String.valueOf(totalSessions));
            }
        });
        btnPlusSession.setOnClickListener(v -> {
            totalSessions++;
            tvTotalSessions.setText(String.valueOf(totalSessions));
        });

        // 5. Xử lý Tăng/Giảm Số buổi vắng
        btnMinusAbsent.setOnClickListener(v -> {
            if (maxAbsence > 0) {
                maxAbsence--;
                tvMaxAbsence.setText(String.valueOf(maxAbsence));
            }
        });
        btnPlusAbsent.setOnClickListener(v -> {
            maxAbsence++;
            tvMaxAbsence.setText(String.valueOf(maxAbsence));
        });

        // 6. Xử lý Chọn Thứ (Day Selector)
        for (TextView dayView : dayViews) {
            dayView.setOnClickListener(v -> {
                v.setSelected(!v.isSelected());
                ((TextView) v).setTextColor(v.isSelected() ? 0xFFFFFFFF : 0xFF000000);
            });
        }

        // 7. Nút Tạo lớp học (Có kiểm tra dữ liệu) - MỚI
        btnCreate.setOnClickListener(v -> {
            if (validateInputs()) {
                // Nếu dữ liệu hợp lệ thì mới thực hiện
                createClass();
            }
        });
    }

    // --- LOGIC HỌC KỲ (MỚI) ---

    private void setDefaultSemester() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH chạy từ 0-11 nên phải +1
        int year = calendar.get(Calendar.YEAR);

        String currentSemester;
        String academicYear;

        // Logic xác định học kỳ và năm học
        // HK1: Tháng 8 - 12 (Của năm bắt đầu niên khóa)
        // HK2: Tháng 1 - 5 (Của năm sau niên khóa)
        // HK Hè: Tháng 6 - 7 (Của năm sau niên khóa)

        if (month >= 8 && month <= 12) {
            currentSemester = "Học kỳ 1";
            academicYear = year + " - " + (year + 1);
        } else if (month >= 1 && month <= 5) {
            currentSemester = "Học kỳ 2";
            academicYear = (year - 1) + " - " + year;
        } else { // Tháng 6, 7
            currentSemester = "Học kỳ Hè";
            academicYear = (year - 1) + " - " + year;
        }

        tvSemester.setText(currentSemester + ", năm học " + academicYear);
    }

    private void showSemesterPicker() {
        // Tạo danh sách các tùy chọn học kỳ (Hiện tại, Tương lai gần, Quá khứ gần)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        // Tạo list 3 năm học gần nhất để chọn
        List<String> semesters = new ArrayList<>();
        int[] years = {currentYear - 1, currentYear, currentYear + 1};

        for (int y : years) {
            String yearStr = y + " - " + (y + 1);
            semesters.add("Học kỳ 1, năm học " + yearStr);
            semesters.add("Học kỳ 2, năm học " + yearStr);
            semesters.add("Học kỳ Hè, năm học " + yearStr);
        }

        // Chuyển List thành Array cho Dialog
        String[] semesterArray = semesters.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn học kỳ");
        builder.setItems(semesterArray, (dialog, which) -> {
            tvSemester.setText(semesterArray[which]);
        });
        builder.show();
    }

    // --- LOGIC KIỂM TRA (VALIDATION) ---

    private boolean validateInputs() {
        // 1. Kiểm tra Tên lớp học (*)
        if (TextUtils.isEmpty(etClassName.getText().toString().trim())) {
            etClassName.setError("Vui lòng nhập tên lớp học");
            etClassName.requestFocus();
            return false;
        }

        // 2. Kiểm tra Mã môn (*)
        if (TextUtils.isEmpty(etSubjectCode.getText().toString().trim())) {
            etSubjectCode.setError("Vui lòng nhập mã môn");
            etSubjectCode.requestFocus();
            return false;
        }

        // 3. Kiểm tra Mã lớp (*)
        if (TextUtils.isEmpty(etClassCode.getText().toString().trim())) {
            etClassCode.setError("Vui lòng nhập mã lớp");
            etClassCode.requestFocus();
            return false;
        }

        // 4. Kiểm tra xem đã chọn ít nhất 1 ngày học chưa (Tùy chọn thêm)
        boolean isDaySelected = false;
        for (TextView day : dayViews) {
            if (day.isSelected()) {
                isDaySelected = true;
                break;
            }
        }
        if (!isDaySelected) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 ngày học trong tuần", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true; // Tất cả đều hợp lệ
    }

    private void createClass() {
        // TODO: Gọi API hoặc lưu vào Database ở đây
        // Lấy dữ liệu:
        String className = etClassName.getText().toString();
        String subjectCode = etSubjectCode.getText().toString();
        String semester = tvSemester.getText().toString();
        // ...

        Toast.makeText(this, "Tạo lớp " + className + " thành công!", Toast.LENGTH_SHORT).show();
        finish(); // Đóng màn hình
    }

    // --- TIỆN ÍCH KHÁC ---

    private void showTimePicker(TextView targetView) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    String time = String.format(Locale.getDefault(), "%02d : %02d", hourOfDay, minute1);
                    targetView.setText(time);
                }, hour, minute, true); // true = 24h format, false = AM/PM
        timePickerDialog.show();
    }
}