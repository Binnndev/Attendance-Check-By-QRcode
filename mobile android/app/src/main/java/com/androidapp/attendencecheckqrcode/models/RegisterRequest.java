package com.androidapp.attendencecheckqrcode.models;

public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dob;
    private String password;

    public RegisterRequest(String firstName, String lastName, String email, String phone, String dob, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dob = dob;
        this.password = password;
    }
}
