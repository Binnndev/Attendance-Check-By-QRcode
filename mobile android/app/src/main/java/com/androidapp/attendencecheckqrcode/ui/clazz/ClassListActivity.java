package com.androidapp.attendencecheckqrcode.ui.clazz;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidapp.attendencecheckqrcode.R;
import com.androidapp.attendencecheckqrcode.adapters.ClassAdapter;
import com.androidapp.attendencecheckqrcode.models.Classroom;
import com.androidapp.attendencecheckqrcode.models.User;
import com.androidapp.attendencecheckqrcode.utils.MockData;

import java.util.ArrayList;
import java.util.List;

public class ClassListActivity extends AppCompatActivity {

    private RecyclerView rcvClassList;
    private ClassAdapter classAdapter;
    private ImageView btnBack;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        if (getIntent().hasExtra("currentUser")) {
            currentUser = (User) getIntent().getSerializableExtra("currentUser");
        }

        initViews();
        setupRecyclerView();

        btnBack.setOnClickListener(v -> finish());

        loadData();
    }

    private void initViews() {
        rcvClassList = findViewById(R.id.rcvClassList);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        classAdapter = new ClassAdapter(new ArrayList<>());
        rcvClassList.setLayoutManager(new LinearLayoutManager(this));
        rcvClassList.setAdapter(classAdapter);
    }

    private void loadData() {
        if (currentUser == null) return;

        // --- LOGIC MỚI: Đọc từ file classes.txt ---
        // Demo: Lấy tất cả lớp mà ID giảng viên KHÁC ID của mình (Coi như là mình đi học)
        List<Classroom> enrolledClasses = MockData.getEnrolledClasses(this, currentUser.getId());

        classAdapter.setData(enrolledClasses);

        if (enrolledClasses.isEmpty()) {
            Toast.makeText(this, "Bạn chưa tham gia lớp nào.", Toast.LENGTH_SHORT).show();
        }
    }
}