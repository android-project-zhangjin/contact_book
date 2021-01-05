package com.example.contact_book;

import android.app.Application;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

public class EditTextFocusProcessor {
    //启用函数
    public void StartProcess(Context context,View currentFocus,MotionEvent motionEvent){
        if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
            if(IsShouldHideKeyboard(currentFocus,motionEvent)){
                HideKeyboard(context,currentFocus);
            }
        }
    }

    //根据EditText控件在屏幕的区域判断是否该失去焦点
    public boolean IsShouldHideKeyboard(View view, MotionEvent motionEvent){
        if(view instanceof EditText){
            //控件坐标
            int[] coord={0,0};
            view.getLocationInWindow(coord);
            int left=coord[0];
            int top=coord[1];
            int right=left+view.getWidth();
            int buttom=top+view.getHeight();
            int evX=(int)motionEvent.getRawX();
            int evY=(int)motionEvent.getRawY();
            return !((left<=evX && evX<=right) && (top<=evY && evY<=buttom));
        }
        else return false;
    }

    public void HideKeyboard(Context context,View view){
        //InputMethodManager管理输入法与应用程序的交互
        InputMethodManager inputMethodManager=((InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE));
        if(inputMethodManager!=null){
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }
        view.clearFocus();
    }
}