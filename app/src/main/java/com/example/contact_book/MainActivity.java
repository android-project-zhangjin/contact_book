package com.example.contact_book;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Button tickle_button = findViewById(R.id.tickle);
        tickle_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,CareText.class);
                startActivity(intent);
            }
        });

        final FragmentManager fm =getSupportFragmentManager();
        final ContactFragment conF=new ContactFragment();
        final RecordFragment record_fragment=new RecordFragment();

        fm.beginTransaction().replace(R.id.fragment,conF).commit();

        //陈宇驰所写:给通话记录按钮添加监听   设置按钮监听-通话记录列表
        Button record_button = this.findViewById(R.id.record);
        record_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fm.beginTransaction().replace(R.id.fragment, record_fragment).commit();
            }
        });

        Button contact_btn= findViewById(R.id.contacts_button);
        contact_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fm.beginTransaction().replace(R.id.fragment, conF).commit();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        new EditTextFocusProcessor().StartProcess(getBaseContext(), getCurrentFocus(), ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
