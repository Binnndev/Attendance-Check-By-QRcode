package com.androidapp.attendencecheckqrcode.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Attendance implements Serializable {
    @SerializedName("class_id")
    private String classId;
    @SerializedName("student_id")
    private int studentId;
    @SerializedName("student_name")
    private String studentName;
    @SerializedName("date")
    private String date; // VD: 12/02/2026
    @SerializedName("time")
    private String time; // VD: 08:30:00

    public Attendance(String classId, int studentId, String studentName, String date, String time) {
        this.classId = classId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.date = date;
        this.time = time;
    }

    // Constructor đọc từ file
    public Attendance(String line) {
        String[] p = line.split("\\|");
        if (p.length >= 5) {
            this.classId = p[0];
            this.studentId = Integer.parseInt(p[1]);
            this.studentName = p[2];
            this.date = p[3];
            this.time = p[4];
        }
    }

    public String toFileString() {
        return classId + "|" + studentId + "|" + studentName + "|" + date + "|" + time;
    }

    // Getters...
    public String getDate() { return date; }
    public String getTime() { return time; }
}
