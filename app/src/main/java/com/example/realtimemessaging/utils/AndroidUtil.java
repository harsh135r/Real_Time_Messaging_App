package com.example.realtimemessaging.utils;

import static android.content.Intent.getIntent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.realtimemessaging.model.UserModel;

public class AndroidUtil {
    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void passUserModelAsIntent(Intent intent, UserModel model) {
        intent.putExtra("username",model.getUsername());
        intent.putExtra("phone",model.getPhone());
        intent.putExtra("user id",model.getUserId());

    }
    public static UserModel getUserModelfromIntent(Intent intent){
        UserModel usermodel=new UserModel();
        usermodel.setUsername(intent.getStringExtra("username"));
        usermodel.setPhone(intent.getStringExtra("phone"));
        usermodel.setUserId(intent.getStringExtra("user id"));
        return usermodel;
    }
    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView){
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);

    }
}
