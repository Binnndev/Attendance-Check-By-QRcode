package com.androidapp.attendencecheckqrcode.api;



import com.androidapp.attendencecheckqrcode.models.AttendanceItem;
import com.androidapp.attendencecheckqrcode.models.AuthResponse;
import com.androidapp.attendencecheckqrcode.models.LoginRequest;
import com.androidapp.attendencecheckqrcode.models.QrData;
import com.androidapp.attendencecheckqrcode.models.RegisterRequest;
import com.androidapp.attendencecheckqrcode.models.Session;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/auth/register")
    Call<AuthResponse> registerUser(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> loginUser(@Body LoginRequest request);

    @GET("api/sessions/{sessionId}")
    Call<Session> getSessionDetails(@Path("sessionId") int sessionId);

    @GET("api/sessions/{sessionId}/attendance")
    Call<List<AttendanceItem>> getSessionAttendance(@Path("sessionId") int sessionId);

    @GET("api/sessions/{sessionId}/qr/current")
    Call<QrData> getCurrentQrCode(@Path("sessionId") int sessionId);

    @POST("api/sessions/{sessionId}/qr/revoke")
    Call<Void> revokeQrCode(@Path("sessionId") int sessionId);
}