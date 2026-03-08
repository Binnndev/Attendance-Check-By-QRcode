package com.androidapp.attendencecheckqrcode.ui.teaching;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.androidapp.attendencecheckqrcode.R;
import com.androidapp.attendencecheckqrcode.adapters.TeachingAdapter;
import com.androidapp.attendencecheckqrcode.models.Classroom;
import com.androidapp.attendencecheckqrcode.models.User;
import com.androidapp.attendencecheckqrcode.utils.MockData;
import java.util.ArrayList;
import java.util.List;

public class TeachingListActivity extends AppCompatActivity {
    private RecyclerView rcvTeachingList;
    private TeachingAdapter teachingAdapter;
    private ImageView btnBack;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teaching_list);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // 1. Nhận User (Nếu không có thì lấy User đầu tiên làm mẫu)
        if (getIntent().hasExtra("currentUser")) {
            currentUser = (User) getIntent().getSerializableExtra("currentUser");
        } else {
            // Fallback: Lấy user đầu tiên trong danh sách nếu null
            List<User> users = MockData.getAllUsers(this);
            if (!users.isEmpty()) currentUser = users.get(0);
        }

        initViews();
        setupRecyclerView();

        btnBack.setOnClickListener(v -> finish());
    }

    // --- 2. QUAN TRỌNG: Tự động tải lại khi màn hình hiện lên ---
    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        rcvTeachingList = findViewById(R.id.rcvTeachingList);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        teachingAdapter = new TeachingAdapter(new ArrayList<>());
        rcvTeachingList.setLayoutManager(new LinearLayoutManager(this));
        rcvTeachingList.setAdapter(teachingAdapter);
    }

    private void loadData() {
        if (currentUser == null) return;

        // 3. Lấy danh sách lớp khớp với ID của User hiện tại
        List<Classroom> myClasses = MockData.getTeachingClasses(this, currentUser.getId());

        // Cập nhật Adapter
        teachingAdapter.setData(myClasses);

        // Debug: Hiện thông báo nếu không có lớp (để biết code có chạy không)
        if (myClasses.isEmpty()) {
            Toast.makeText(this, "Chưa có lớp nào (ID: " + currentUser.getId() + ")", Toast.LENGTH_SHORT).show();
        }
    }
}