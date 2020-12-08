package com.example.contact_book;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class StringPicker extends WheelView<String>{
    private List<String> mStringList=new ArrayList<>();
    private String mSelectedString;
    private int mSelectedStringIndex;
    private StringPicker.OnStringSelectedListener mOnStringSelectedListener;

    public StringPicker(Context context) {
        this(context, null);
    }

    public StringPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StringPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Log.d("My","I");
        mStringList.add(" ");
        setDataList(mStringList);
        Log.d("My","II");

        initAttrs(context, attrs);
        setItemMaximumWidthText("0000");
        setSelectedStringIndex(mSelectedStringIndex, false);
        setOnWheelChangeListener(new OnWheelChangeListener<String>() {
            @Override
            public void onWheelSelected(String item, int position) {
                mSelectedString=item;
                mSelectedStringIndex=position;
                if(mOnStringSelectedListener!=null){
                    Log.d("My","onWheelSelected_1");
                    mOnStringSelectedListener.onStringSelected(item);
                    Log.d("My","onWheelSelected_2");
                }
            }
        });

        Log.d("My","StringPicker");
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        mSelectedStringIndex=mStringList.size()/2;
        mSelectedString=mStringList.get(mStringList.size()/2);

        Log.d("My","initAttrs");
    }

    public int getSelectStringIndex(){
        return mSelectedStringIndex;
    }

    public void setSelectedStringIndex(int selectedStringIndex) {
        setSelectedStringIndex(selectedStringIndex, true);
        Log.d("My","setSelectedString");
    }

    public void setSelectedStringIndex(int selectedStringIndex, boolean smoothScroll) {
        setCurrentPosition(selectedStringIndex, smoothScroll);
        Log.d("My","setSelectedString");
    }

    public String getSelectedString() {
        return mSelectedString;
    }

    public void setOnStringSelectedListener(StringPicker.OnStringSelectedListener onStringSelectedListener) {
        mOnStringSelectedListener = onStringSelectedListener;
        Log.d("My","setOnStringSelectedListener");
    }

    public interface OnStringSelectedListener {
        void onStringSelected(String String);
    }

}