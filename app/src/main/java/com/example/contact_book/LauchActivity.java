package com.example.contact_book;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;


public class LauchActivity extends AppCompatActivity {
    private final List<String> unPermissionList = new ArrayList<>(); //申请未得到授权的权限列表
    private final String[] permissionList = new String[]{         //申请的权限列表
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
    };

    private ImageView logo_1;
    private ImageView logo_2;
    private final Animation.AnimationListener animationListener_logo= new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            rotate_scale_positive(logo_1);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Fade_out(logo_2);
            rotate_scale_reverse(logo_1);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    private final Animation.AnimationListener animationListener_finish=new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Log.d("My","Hello");
            startActivity(new Intent(LauchActivity.this,MainActivity.class));
            finish();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lauch);

        logo_1=findViewById(R.id.logo_1);
        logo_2=findViewById(R.id.logo_2);

        MySQLiteOpenHelper mySQLiteOpenHelper=new MySQLiteOpenHelper(getApplicationContext());
        SQLiteDatabase db=mySQLiteOpenHelper.getWritableDatabase();
        mySQLiteOpenHelper.close();
        db.close();

        checkPermission();  //授予权限

    }

    private void Fade_in(View view){
        AlphaAnimation alphaAnimation=new AlphaAnimation(0.0f,1.0f);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setDuration(2000);
        alphaAnimation.setInterpolator(new AccelerateInterpolator());
        alphaAnimation.setAnimationListener(animationListener_logo);
        view.startAnimation(alphaAnimation);
    }

    private void Fade_out(View view){
        AlphaAnimation alphaAnimation=new AlphaAnimation(1.0f,0.0f);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setDuration(2000);
        alphaAnimation.setInterpolator(new DecelerateInterpolator());
        alphaAnimation.setAnimationListener(animationListener_finish);
        view.startAnimation(alphaAnimation);
    }

    private void rotate_scale_positive(View view){
        Animation animation=AnimationUtils.loadAnimation(LauchActivity.this,R.anim.rotate_scale_positive);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    private void rotate_scale_reverse(View view){
        Animation animation=AnimationUtils.loadAnimation(LauchActivity.this,R.anim.rotate_scale_reverse);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    //权限判断和申请
    private void checkPermission() {
        unPermissionList.clear();//清空申请的没有通过的权限
        //逐个判断是否还有未通过的权限
        for (String s : permissionList) {
            if (ContextCompat.checkSelfPermission(this, s) !=
                    PackageManager.PERMISSION_GRANTED) {
                unPermissionList.add(s);//添加还未授予的权限到unPermissionList中
            }
        }
        //有权限没有通过，需要申请
        String tag = "My";
        if (unPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(this,permissionList, 100);    //TODO 申请权限的时候闪退，应该是这里有问题
            Log.d(tag, "check 有权限未通过");
        } else {
            //权限已经都通过了，可以将程序继续打开了
            Log.d(tag, "check 权限都已经申请通过");
        }

        Fade_in(logo_2);
    }
}
