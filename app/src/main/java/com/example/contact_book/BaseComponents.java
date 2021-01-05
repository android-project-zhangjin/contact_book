package com.example.contact_book;

import android.content.ContentValues;
import android.database.Cursor;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class BaseComponents {
    protected ImageView avatarImage;
    protected EditText nameInput_EditText;
    protected EditText nicknameInput_EditText;
    protected EditText phoneInput_EditText;
    protected EditText companyInput_EditText;
    protected EditText emailInput_EditText;
    protected EditText emailRemarkInput_EditText;
    protected EditText addressInput_EditText;
    protected EditText noteInput_EditText;
    protected Button starButton;
    protected Button relationshipButton;
    protected Button phoneTypeButton;

    /**
     * 从数据库中读取数据到控件
     * @param cursor 游标
     */
    public void setComponentsText(Cursor cursor){
        nameInput_EditText.setText(cursor.getString(cursor.getColumnIndex("name")));
        nicknameInput_EditText.setText(cursor.getString(cursor.getColumnIndex("nickname")));
        phoneInput_EditText.setText(cursor.getString(cursor.getColumnIndex("phone")));
        phoneInput_EditText.setEnabled(false);
        companyInput_EditText.setText(cursor.getString(cursor.getColumnIndex("company")));
        emailInput_EditText.setText(cursor.getString(cursor.getColumnIndex("email")));
        emailRemarkInput_EditText.setText(cursor.getString(cursor.getColumnIndex("remark")));
        addressInput_EditText.setText(cursor.getString(cursor.getColumnIndex("address")));
        noteInput_EditText.setText(cursor.getString(cursor.getColumnIndex("note")));
    }

    /**
     * 将输入数据保存到ContentValues中
     * @param values ContentValues
     */
    public void saveDataToContentValues(ContentValues values){
        values.put("name",nameInput_EditText.getText().toString());
        values.put("nickname",nicknameInput_EditText.getText().toString());
        values.put("phone",phoneInput_EditText.getText().toString());
        values.put("company",companyInput_EditText.getText().toString());
        values.put("email",emailInput_EditText.getText().toString());
        values.put("remark",emailRemarkInput_EditText.getText().toString());
        values.put("address",addressInput_EditText.getText().toString());
        values.put("note",noteInput_EditText.getText().toString());
    }
}
