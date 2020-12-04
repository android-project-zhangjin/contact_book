 package com.example.contact_book;


import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

 public class contactList extends Fragment {
    private static ArrayList<miniCard> dataList=new ArrayList<>();
    private static final List<String> relationshipList=new ArrayList<>();
    private static final List<String> phoneTypeList=new ArrayList<>();

    private View view;
    private RecyclerView recyclerView;
    private miniCardAdapter mAdapter;

    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private SQLiteDatabase db;

    public contactList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_contact_list,container,false);

        mySQLiteOpenHelper=new MySQLiteOpenHelper(getContext());
        db=mySQLiteOpenHelper.getWritableDatabase();

        new InitRelationshipList().initRelationshipList(relationshipList);
        new InitPhoneTypeList().initPhoneTypeList(phoneTypeList);


        return view;
    }

     @Override
     public void onResume() {
         super.onResume();
         //在onActivityResult()执行完毕后，执行onResume()，这是一个可以优化性能的地方
         initDataList(dataList);
         initRecyclerView();

     }

     @Override
     public void onActivityCreated(@Nullable Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);

         //悬浮按钮监听事件
         if(getActivity()!=null){
             //添加联系人
             FloatingActionButton add_contact_btn=(FloatingActionButton)getActivity().findViewById(R.id.add_contact);
             add_contact_btn.setOnClickListener(new View.OnClickListener(){
                 @Override
                 public void onClick(View v) {
                     //在启动新增联系人Activity时，要求获得结果，RESULT_OK表示确定，RESULT_CANCEL表示取消
                     Intent intent=new Intent(getActivity(),contact_msg_edit.class);
                     //传入参数代表“添加”
                     intent.putExtra("option","New");
                     startActivityForResult(intent,1);
                 }
             });

             FloatingActionButton flash_btn=(FloatingActionButton)getActivity().findViewById(R.id.phoneCall);
             flash_btn.setOnClickListener(new View.OnClickListener(){
                 @Override
                 public void onClick(View view) {
                     Intent intent = new Intent(Intent.ACTION_DIAL);
                     Uri data = Uri.parse("tel:" + "");
                     intent.setData(data);
                     startActivity(intent);
                 }
             });

         }


     }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK){
            String phone=data.getStringExtra("phone");
            if(phone==null){
                Log.d("My","NULL_PHONE");
            } else {

                //游标——查询
                Cursor cursor=db.query("contact_list_database",null,"phone="+phone,null,null,null,"name"+" collate NOCASE DESC");
                if(cursor.getCount()==0){
                    Log.d("My","CURSOR_NULL");
                } else {
                    cursor.moveToFirst();
                    ArrayList<miniCard> newDataList = new ArrayList<>(dataList);

                    //读取数据
                    readFromDatabase(cursor,newDataList);

                    cursor.close();
                    flash(newDataList);
                }
            }

        } else if(requestCode==2&&resultCode==RESULT_OK){

        }
    }

     @Override
     public void onDestroyView() {
         super.onDestroyView();
         //关闭数据库
         mySQLiteOpenHelper.close();
         db.close();
     }

     private void flash(ArrayList<miniCard> newDataList){
        if(newDataList==null)
            return;
        DiffUtil.DiffResult diffResult= DiffUtil.calculateDiff(new MyDiffCallBack(dataList,newDataList),true);
        diffResult.dispatchUpdatesTo(mAdapter);

        dataList=newDataList;
        mAdapter.setData(dataList);
    }

    //初始化联系人界面
    private void initDataList(ArrayList<miniCard> mDataList) {
        mDataList.clear();

        //游标
        Cursor cursor=db.query("contact_list_database",null,null,null,null,null,"name collate NOCASE DESC");
        cursor.moveToFirst();
        if(cursor.getCount()==0)
            return;
        do{
            readFromDatabase(cursor,mDataList);
        }while(cursor.moveToNext());

        cursor.close();
    }

     /**
      * 将数据从数据库中读入，添加到List中
      * @param cursor 游标
      * @param mDataList 数据集
      */
    private void readFromDatabase(Cursor cursor, ArrayList<miniCard> mDataList){
        String name=cursor.getString(cursor.getColumnIndex("name"));
        String phone=cursor.getString(cursor.getColumnIndex("phone"));
        String relationship=relationshipList.get(cursor.getInt(cursor.getColumnIndex("relationship")));

        //在SQLite中获得的BLOB是字节数组，需要转化为Bitmap用于在ImageView上显示
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
        miniCard card=new miniCard(name,phone,relationship,avatarBitmap);
        card.phoneType=phoneTypeList.get(cursor.getInt(cursor.getColumnIndex("phoneType")));
        card.company=cursor.getString(cursor.getColumnIndex("company"));
        card.email=cursor.getString(cursor.getColumnIndex("email"));
        card.remark=cursor.getString(cursor.getColumnIndex("remark"));
        card.address=cursor.getString(cursor.getColumnIndex("address"));
        card.note=cursor.getString(cursor.getColumnIndex("note"));
        card.star=cursor.getInt(cursor.getColumnIndex("star"));
        card.nickname=cursor.getString(cursor.getColumnIndex("nickname"));
        mDataList.add(card);

    }

    private void initRecyclerView(){
        //布局管理器
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        //RecyclerView分割线
        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(view.getContext(),DividerItemDecoration.VERTICAL);

        //实例化RecyclerView，声明所在布局
        this.recyclerView=(RecyclerView)view.findViewById(R.id.contact_list);

        //实例化名片的Adapter
        this.mAdapter=new miniCardAdapter(dataList,getContext());


        mAdapter.setOnClickListener(new miniCardAdapter.ViewHolder.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int id, final int position) {
                if (id == R.id.star_btn_miniView) {
                    Button star_btn=(Button)view.findViewById(id);
                    ContentValues values=new ContentValues();
                    switch (dataList.get(position).star){
                        case 0:
                            star_btn.setBackground(ResourcesCompat.getDrawable(getResources(),R.mipmap.star_yellow,null));
                            dataList.get(position).star=1;
                            values.put("star",1);
                            db.update("contact_list_database",values,"phone=?",new String[]{dataList.get(position).phone});
                            break;
                        case 1:
                            star_btn.setBackground(ResourcesCompat.getDrawable(getResources(),R.mipmap.star,null));
                            dataList.get(position).star=0;
                            values.put("star",0);
                            db.update("contact_list_database",values,"phone=?",new String[]{dataList.get(position).phone});
                            break;
                    }

                } else  if(id==R.id.miniCardView) {
                    //如果在点击时，有操作按钮，将按钮隐藏；否则展开或收起详情页
                    if(view.findViewById(R.id.editButton).getVisibility()==View.VISIBLE){
                        view.findViewById(R.id.editButton).setVisibility(View.GONE);
                        view.findViewById(R.id.deleteButton).setVisibility(View.GONE);
                        view.findViewById(R.id.callButton).setVisibility(View.GONE);
                        view.findViewById(R.id.tickleButton).setVisibility(View.GONE);
                        view.findViewById(R.id.name_TextView).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.phone_TextView).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.relationship_TextView).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.phoneTypeText).setVisibility(View.VISIBLE);
                    } else {
                        if(view.findViewById(R.id.miniCardViewExpand).getVisibility() == View.GONE){
                            view.findViewById(R.id.miniCardViewExpand).setVisibility(View.VISIBLE);
                        } else if(view.findViewById(R.id.miniCardViewExpand).getVisibility() == View.VISIBLE) {
                            view.findViewById(R.id.miniCardViewExpand).setVisibility(View.GONE);
                        }
                    }
                } else if(id==R.id.editButton){
                    Intent intent=new Intent(getActivity(),contact_msg_edit.class);
                    intent.putExtra("option","Edit");
                    intent.putExtra("phone",dataList.get(position).phone);
                    startActivityForResult(intent,2);
                } else if(id==R.id.tickleButton){
                    Intent intent=new Intent(getActivity(),CareText.class);
                    intent.putExtra("phone",dataList.get(position).phone);
                    startActivity(intent);
                } else if(id==R.id.deleteButton){
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    AlertDialog alert=builder.setIcon(R.mipmap.setting)
                            .setTitle("提示信息")
                            .setMessage("确定删除？")
                            .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if(db.delete("contact_list_database","phone=?",new String[]{dataList.get(position).phone})!=-1){
                                        ArrayList<miniCard> newDataList=new ArrayList<>(dataList);
                                        newDataList.remove(position);
                                        flash(newDataList);
                                    }
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).create();
                    alert.show();
                } else if(id==R.id.callButton){
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    Uri data = Uri.parse("tel:" + dataList.get(position).phone);
                    intent.setData(data);
                    startActivity(intent);

                }
            }
        });

        mAdapter.setOnLongClickListener(new miniCardAdapter.ViewHolder.MyItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int id, int position) {
                if(id==R.id.miniCardView){
                    view.findViewById(R.id.name_TextView).setVisibility(View.GONE);
                    view.findViewById(R.id.phone_TextView).setVisibility(View.GONE);
                    view.findViewById(R.id.relationship_TextView).setVisibility(View.GONE);
                    view.findViewById(R.id.phoneTypeText).setVisibility(View.GONE);
                    view.findViewById(R.id.editButton).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.deleteButton).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.callButton).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.tickleButton).setVisibility(View.VISIBLE);
                }
            }
        });

        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    /*
    private void initDB(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("contact_book",0);
        boolean isFirst=sharedPreferences.getBoolean("first",true);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        if(isFirst){
            editor.putBoolean("first",false);
            //联系人的Uri
            Uri uri = ContactsContract.Contacts.CONTENT_URI;
            //指定获取_id和display_name两列数据，display_name即为姓名
            String[] projection = new String[] {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            //根据Uri查询相应的ContentProvider，cursor为获取到的数据集
            Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ContentValues values=new ContentValues();
                    Long id = cursor.getLong(0);
                    //获取姓名
                    String name = cursor.getString(1);
                    //指定获取NUMBER这一数据
                    String[] phoneProjection = new String[] {
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    };
                    values.put("name",name);

                    //根据联系人的ID获取此人的电话号码
                    Cursor phonesCusor = getContext().getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            phoneProjection,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                            null,
                            null);

                    //获取电话号码
                    if (phonesCusor != null && phonesCusor.moveToFirst()) {
                        String num = phonesCusor.getString(0);
                        values.put("phone",num);
                    }
                    db.insert("contact_list_database",null,values);
                } while (cursor.moveToNext());
            }

        }
        editor.commit();
    }
     */
}
