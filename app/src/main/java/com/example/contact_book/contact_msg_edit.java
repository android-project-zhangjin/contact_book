package com.example.contact_book;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class contact_msg_edit extends AppCompatActivity {
    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private SQLiteDatabase db;
    ContentValues values=new ContentValues();
    Bitmap bitmap;

    ImageView avatarImage;
    EditText nameInput_EditText;
    EditText nicknameInput_EditText;
    EditText phoneInput_EditText;
    EditText companyInput_EditText;
    EditText emailInput_EditText;
    EditText emailRemarkInput_EditText;
    EditText addressInput_EditText;
    EditText noteInput_EditText;
    Button starButton;
    Button relationshipButton;
    Button phoneTypeButton;
    int starValue=0;

    List<String> phoneTypeList=new ArrayList<>();
    List<String> relationshipList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contact_msg_input);

        //连接数据库
        mySQLiteOpenHelper=new MySQLiteOpenHelper(this);
        db=mySQLiteOpenHelper.getWritableDatabase();

        final StringPicker relationshipPicker= findViewById(R.id.relationshipPicker);
        final StringPicker phoneTypePicker=findViewById(R.id.phoneTypeInput_StringPicker);

        //关系选择器数值设置
        new InitRelationshipList().initRelationshipList(relationshipList);
        relationshipPicker.setDataList(relationshipList);
        relationshipPicker.setHalfVisibleItemCount(2);
        relationshipPicker.setSelectedStringIndex(0);
        //关系选择器监听器
        relationshipPicker.setOnStringSelectedListener(new StringPicker.OnStringSelectedListener() {
            @Override
            public void onStringSelected(String Relationship) {
                values.put("relationship",relationshipPicker.getSelectStringIndex());
            }
        });

        //号码类型选择器数值设置
        new InitPhoneTypeList().initPhoneTypeList(phoneTypeList);
        phoneTypePicker.setDataList(phoneTypeList);
        phoneTypePicker.setHalfVisibleItemCount(1);
        phoneTypePicker.setSelectedStringIndex(0);
        phoneTypePicker.setOnStringSelectedListener(new StringPicker.OnStringSelectedListener() {
            @Override
            public void onStringSelected(String String) {
                values.put("phoneType",phoneTypePicker.getSelectStringIndex());
            }
        });

        //初始化控件
        initComponent();

        //如果为编辑操作，需要从数据库读数据到界面上
        if(getIntent().getStringExtra("option").equals("Edit")){
            if(getIntent().getStringExtra("phone").equals(""))
                return;
            else initFrom(relationshipPicker,phoneTypePicker);
        }

        //点击——切换星标/普通
        starButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(starValue==0){
                    starValue=1;
                    starButton.setBackground(ResourcesCompat.getDrawable(getResources(),R.mipmap.star_yellow,null));
                } else if(starValue==1) {
                    starValue=0;
                    starButton.setBackground(ResourcesCompat.getDrawable(getResources(),R.mipmap.star,null));
                }
            }
        });

        //长按——切换黑标/普通
        starButton.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view) {
                if(starValue!=2){
                    starValue=2;
                    starButton.setBackground(ResourcesCompat.getDrawable(getResources(),R.mipmap.star_black,null));
                } else {
                    starValue=0;
                    starButton.setBackground(ResourcesCompat.getDrawable(getResources(),R.mipmap.star,null));
                }

                return true;
            }
        });

        relationshipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                relationshipPicker.setVisibility(View.VISIBLE);
            }
        });

        phoneTypeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                phoneTypePicker.setVisibility(View.VISIBLE);
            }
        });

    }

    public void nameExpandButtonClick(View view){
        LinearLayout nameExpand_LinearLayout=(LinearLayout) findViewById(R.id.nameExpand_LinearLayout);
        if(nameExpand_LinearLayout.getVisibility()==View.GONE){
            nameExpand_LinearLayout.setVisibility(View.VISIBLE);
            view.setBackground(ContextCompat.getDrawable(getBaseContext(),R.mipmap.arrow_up));
        } else {
            nameExpand_LinearLayout.setVisibility(View.GONE);
            view.setBackground(ContextCompat.getDrawable(getBaseContext(),R.mipmap.arrow_down));
        }
    }

    public void phoneExpandButtonClick(View view){
        LinearLayout phoneExpand_LinearLayout=(LinearLayout)findViewById(R.id.phoneExpand_LinearLayout);
        if(phoneExpand_LinearLayout.getVisibility()==View.GONE){
            phoneExpand_LinearLayout.setVisibility(View.VISIBLE);
            view.setBackground(ContextCompat.getDrawable(getBaseContext(),R.mipmap.arrow_up));
        } else {
            phoneExpand_LinearLayout.setVisibility(View.GONE);
            view.setBackground(ContextCompat.getDrawable(getBaseContext(),R.mipmap.arrow_down));
        }
    }

    public void emailExpandButtonClick(View view){
        LinearLayout emailExpand_LinearLayout=(LinearLayout)findViewById(R.id.emailExpand_LinearLayout);
        if(emailExpand_LinearLayout.getVisibility()==View.GONE){
            emailExpand_LinearLayout.setVisibility(View.VISIBLE);
            view.setBackground(ContextCompat.getDrawable(getBaseContext(),R.mipmap.arrow_up));
        } else {
            emailExpand_LinearLayout.setVisibility(View.GONE);
            view.setBackground(ContextCompat.getDrawable(getBaseContext(),R.mipmap.arrow_down));
        }
    }

    public void saveButtonClick(View view){
        if(nameInput_EditText.getText().toString().isEmpty() || phoneInput_EditText.getText().toString().isEmpty()){
            Toast.makeText(getBaseContext(),"Name & Phone 不得为空",Toast.LENGTH_SHORT).show();
        } else {
            saveInDatabase();
            primaryKeyReturn();
            finish();
        }
    }

    private void initComponent(){
        avatarImage=findViewById(R.id.avatarImage);
        nameInput_EditText=findViewById(R.id.nameInput_EditText);
        nicknameInput_EditText=findViewById(R.id.nicknameInput_EditText);
        phoneInput_EditText=findViewById(R.id.phoneInput_EditText);
        companyInput_EditText=findViewById(R.id.companyInput_EditText);
        emailInput_EditText=findViewById(R.id.emailInput_EditText);
        emailRemarkInput_EditText=findViewById(R.id.emailRemarkInput_EditText);
        addressInput_EditText=findViewById(R.id.addressInput_EditText);
        noteInput_EditText=findViewById(R.id.noteInput_EditText);
        starButton=findViewById(R.id.star_btn);
        relationshipButton=findViewById(R.id.relationshipButton);
        phoneTypeButton=findViewById(R.id.phoneTypeButton);

    }

    private void initFrom(StringPicker relationshipPicker, StringPicker phoneTypePicker){
        Cursor cursor=db.query("contact_list_database",null,"phone="+getIntent().getStringExtra("phone"),
                null,null,null,null);
        if(cursor.getCount()==0)
            return;

        cursor.moveToFirst();

        byte[] avatarByte=cursor.getBlob(cursor.getColumnIndex("avatar"));
        Bitmap avatarBitmap;
        //判断是否有头像，如果没有则使用默认头像
        if(avatarByte!=null){
            avatarBitmap= BitmapFactory.decodeByteArray(avatarByte,0,avatarByte.length);
        } else {
            avatarBitmap=BitmapFactory.decodeResource(getResources(),R.mipmap.default_avatar);
        }
        if(avatarBitmap==null)
            Log.d("My","NULL");

        nameInput_EditText.setText(cursor.getString(cursor.getColumnIndex("name")));
        phoneInput_EditText.setText(cursor.getString(cursor.getColumnIndex("phone")));
        phoneInput_EditText.setEnabled(false);
        companyInput_EditText.setText(cursor.getString(cursor.getColumnIndex("company")));
        emailInput_EditText.setText(cursor.getString(cursor.getColumnIndex("email")));
        emailRemarkInput_EditText.setText(cursor.getString(cursor.getColumnIndex("remark")));
        addressInput_EditText.setText(cursor.getString(cursor.getColumnIndex("address")));
        noteInput_EditText.setText(cursor.getString(cursor.getColumnIndex("note")));
        switch (cursor.getInt(cursor.getColumnIndex("star"))){
            case 0:
                starButton.setBackground(ResourcesCompat.getDrawable(getResources(),R.mipmap.star,null));
                starValue=0;
                break;
            case 1:
                starButton.setBackground(ResourcesCompat.getDrawable(getResources(),R.mipmap.star_yellow,null));
                starValue=1;
                break;
            case 2:
                starButton.setBackground(ResourcesCompat.getDrawable(getResources(),R.mipmap.star_black,null));
                starValue=2;
                break;
        }

        relationshipButton.setText(relationshipList.get(cursor.getInt(cursor.getColumnIndex("relationship"))));
        phoneTypeButton.setText(phoneTypeList.get(cursor.getInt(cursor.getColumnIndex("phoneType"))));

        cursor.close();
    }

    private void saveInDatabase(){
        values.put("name",nameInput_EditText.getText().toString());
        values.put("nickname",nicknameInput_EditText.getText().toString());
        values.put("phone",phoneInput_EditText.getText().toString());
        values.put("company",companyInput_EditText.getText().toString());
        values.put("email",emailInput_EditText.getText().toString());
        values.put("remark",emailRemarkInput_EditText.getText().toString());
        values.put("address",addressInput_EditText.getText().toString());
        values.put("note",noteInput_EditText.getText().toString());
        values.put("star",starValue);
        if(bitmap!=null){
            values.put("avatar",bitmapToByte());
        }


        if(getIntent().getStringExtra("option").equals("New")){
            if(db.insert("contact_list_database",null,values)==-1){
                Toast.makeText(contact_msg_edit.this,"该号码已存在",Toast.LENGTH_LONG).show();
            }
            else {
                ContentValues valuesToSysContactsDataBase = new ContentValues();
                //向系统联系人表中插入数据
                Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI,valuesToSysContactsDataBase);
                long rawContactId = ContentUris.parseId(rawContactUri);

                //插入姓名数据
                valuesToSysContactsDataBase.clear();
                valuesToSysContactsDataBase.put(ContactsContract.Data.RAW_CONTACT_ID,rawContactId);
                valuesToSysContactsDataBase.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                valuesToSysContactsDataBase.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,values.getAsString("name"));
                getContentResolver().insert(ContactsContract.Data.CONTENT_URI,valuesToSysContactsDataBase);

                //插入电话
                valuesToSysContactsDataBase.clear();
                valuesToSysContactsDataBase.put(ContactsContract.Data.RAW_CONTACT_ID,rawContactId);
                valuesToSysContactsDataBase.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                valuesToSysContactsDataBase.put(ContactsContract.CommonDataKinds.Phone.NUMBER,values.getAsString("phone"));
                getContentResolver().insert(ContactsContract.Data.CONTENT_URI,valuesToSysContactsDataBase);

                Toast.makeText(contact_msg_edit.this," 成功添加新联系人 "+nameInput_EditText.getText().toString(),Toast.LENGTH_SHORT).show();
            }
        } else if(getIntent().getStringExtra("option").equals("Edit")){

            if(db.update("contact_list_database",values,"phone=?",new String[]{phoneInput_EditText.getText().toString()})==-1){
                Toast.makeText(this,"Fail",Toast.LENGTH_SHORT).show();
            }

        }


    }

    /**
     * 将新增项的主码传回上一个Activity
     */
    private void primaryKeyReturn(){
        Intent intent=new Intent();
        intent.putExtra("phone",phoneInput_EditText.getText().toString());
        setResult(RESULT_OK,intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==1){
            if(data!=null){
                Uri uri;
                uri=data.getData();
                uri=convertUri(uri);
                if(uri!=null){
                    crop(uri);
                }
            }
        } else if(requestCode==2){
            if(data!=null){
                Log.d("My","file:///" +getExternalCacheDir().getPath());
                try{
                    bitmap=BitmapFactory.decodeStream(
                            contact_msg_edit.this.getContentResolver().openInputStream(Uri.parse(
                                    "file:///" +getExternalCacheDir().getPath()+"/avatar_cropped.png"))
                    );
                    avatarImage.setImageBitmap(bitmap);
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 将content类型的Uri转化为文件类型的Uri
     * @param uri content类型Uri
     * @return 文件类型Uri
     */
    private Uri convertUri(Uri uri) {
        InputStream inputStream;
        try{

            inputStream=getContentResolver().openInputStream(uri);
            Bitmap bitmap=BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return saveBitmap(bitmap);
        } catch (IOException e){
            e.printStackTrace();

            return null;
        }
    }

    /**
     * 将Bitmap写入存储中的一个文件中，并返回写入文件的Uri
     * @param bitmap 被保存的Bitmap
     * @return 文件的Uri
     */
    private Uri saveBitmap(Bitmap bitmap) {
        /*
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE" };
        int permission= ActivityCompat.checkSelfPermission(new_contact_msg_input.this,
                "android.permission.WRITE_EXTERNAL_STORAGE");
        if(permission!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(new_contact_msg_input.this,
                    PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        }

         */

        File img=new File(getExternalCacheDir(),"avatar_image.png");
        try{
            if(!img.exists())
                if(img.createNewFile()){
                    Log.d("My","createNewFile Success");
                }
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
        try{
            FileOutputStream fileOutputStream=new FileOutputStream(img);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            //需要指定Activity名
            return FileProvider.getUriForFile(contact_msg_edit.this,"com.example.contact_book.fileprovider",img);
            //return Uri.fromFile(img);
        }catch(IOException e){
            e.printStackTrace();

            return null;
        }
    }

    public void gallery(View view){
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,1);
    }

    private void crop(Uri uri){

        Log.d("My",uri.toString());
        Intent intent=new Intent("com.android.camera.action.CROP");
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        //intent.setDataAndType(Uri.parse("/sdcard/Android/data/com.example.contact_book/cache/avatar_image.png"),"image/*");
        intent.setDataAndType(uri,"image/*");
        Log.d("My",uri.toString());
        intent.putExtra("crop","true");


        //裁剪框的比例
        intent.putExtra("aspectX",1);
        intent.putExtra("aspectY",1);
        intent.putExtra("scale",true);
        //裁剪后的图片大小
        intent.putExtra("outputX",250);
        intent.putExtra("outputY",250);

        intent.putExtra("outputFormat","PNG");
        intent.putExtra("return-data",false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(
                new File(getExternalCacheDir(),"avatar_cropped.png")
        ));


        startActivityForResult(intent,2);
    }

    private byte[] bitmapToByte(){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * EditText的焦点处理代码来自
     *         https://blog.csdn.net/weixin_43615488/article/details/103927055
     *         dispatchTouchEvent()、IsShouldHideKeyboard()、HideKeyboard()
     *         由于在blog中作者直接将其写在Activity中
     *             为方便使用
     *                 将方法包装为方法类EditText_focus_processor
     * @param ev 屏幕上的事件
     * @return 焦点是否还在
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        new EditText_focus_processor().StartProcess(getBaseContext(),getCurrentFocus(),ev);
        return super.dispatchTouchEvent(ev);
    }

    //返回键触发事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            AlertDialog.Builder builder=new AlertDialog.Builder(contact_msg_edit.this);
            AlertDialog alert=builder.setIcon(R.mipmap.setting)
                    .setTitle("提示信息")
                    .setMessage("新添加的联系人尚未保存")
                    .setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //取消新增
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }).create();
            alert.show();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭数据库连接
        mySQLiteOpenHelper.close();
        db.close();
    }
}