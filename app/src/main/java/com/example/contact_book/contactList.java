 package com.example.contact_book;


 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.ContactsContract;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;

 import androidx.annotation.NonNull;
 import androidx.annotation.Nullable;
 import androidx.core.content.res.ResourcesCompat;
 import androidx.fragment.app.Fragment;
 import androidx.recyclerview.widget.DiffUtil;
 import androidx.recyclerview.widget.DividerItemDecoration;
 import androidx.recyclerview.widget.LinearLayoutManager;
 import androidx.recyclerview.widget.RecyclerView;

 import com.getbase.floatingactionbutton.FloatingActionButton;

 import java.text.CollationKey;
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Locale;

 import static android.app.Activity.RESULT_OK;

 public class contactList extends Fragment{
    //程序启动首次创建
    private static boolean isFirst=true;

    //记录当前的位置
    private static int currPosition = 0;

    //静态的列表——避免重复加载
    private static ArrayList<miniCard> dataList=new ArrayList<>();
    private static final List<String> relationshipList=new ArrayList<>();
    private static final List<String> phoneTypeList=new ArrayList<>();

    //RecyclerView布局管理器
    private LinearLayoutManager linearLayoutManager=null;

    private View view;
    private Context mContext;
    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private SQLiteDatabase db;

    //Adapter
    private miniCardAdapter mAdapter;

     /**
      * 在子线程中处理图片更新
      */
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @SuppressLint({"HandlerLeak", "Recycle"})
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (mContext != null) {
                    ArrayList<miniCard> newDataList = new ArrayList<>(dataList);
                    MySQLiteOpenHelper mySQLiteOpenHelper_local = new MySQLiteOpenHelper(mContext);
                    SQLiteDatabase db_local = mySQLiteOpenHelper_local.getReadableDatabase();

                    Cursor cursor;
                    for (int i = msg.getData().getInt("first"); i <= msg.getData().getInt("last") && i < dataList.size(); i++) {
                        cursor = db_local.query("contact_list_database", new String[]{"avatar"}, "phone=?", new String[]{dataList.get(i).phone}, null, null, null);
                        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
                            if (cursor.getBlob(cursor.getColumnIndex("avatar")) != null) {
                                try {
                                    miniCard tmp = dataList.get(i).clone();
                                    tmp.avatar = cursor.getBlob(cursor.getColumnIndex("avatar"));
                                    if (!Collections.replaceAll(newDataList, tmp, tmp))
                                        Log.w("My", "修改后的替换出现错误");
                                } catch (CloneNotSupportedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        cursor.close();
                    }

                    mySQLiteOpenHelper_local.close();
                    db_local.close();
                    flash(newDataList, mAdapter);
                }
                Log.d("My", "Thread");
            }
        }
    };

    public contactList() {
        // Required empty public constructor
    }

     @Override
     public void onCreate(@Nullable Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d("My","contactList onCreate");
         mySQLiteOpenHelper=new MySQLiteOpenHelper(getContext());
         db=mySQLiteOpenHelper.getWritableDatabase();
         db.setLocale(Locale.CHINESE);

         mContext=getContext();
     }

     @Override
     public void onPause() {
         super.onPause();
         Log.d("My","contactList onPause");
         if (linearLayoutManager != null){
             currPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
         }
     }

     @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_contact_list,container,false);

        new InitRelationshipList().initRelationshipList(relationshipList);
        new InitPhoneTypeList().initPhoneTypeList(phoneTypeList);

        //初始化——只在程序第一次安装后的首次启动运行
        initDB();

        Log.d("My","contactList onCreateView");

        return view;
    }

     @Override
     public void onResume() {
         super.onResume();
         initAdapter();
         if(isFirst){
             initDataList(dataList);
             isFirst = false;
         } else {
             mAdapter.setData(dataList);
         }
         //当contactList这个Fragment第一次加载或者从其他Activity或Fragment替换返回后，recyclerView需要重新绑定view，也就是要重新初始化
         initRecyclerView();
     }

     @Override
     public void onActivityCreated(@Nullable Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);

         //悬浮按钮监听事件
         if(getActivity()!=null){
             //添加联系人
             FloatingActionButton add_contact_btn= getActivity().findViewById(R.id.add_contact);
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

             FloatingActionButton flash_btn= getActivity().findViewById(R.id.phoneCall);
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
        assert data != null;
        String phone=data.getStringExtra("phone");
        if(phone==null || phone.equals(""))
            return;

        //游标——查询
        Cursor cursor=db.query("contact_list_database",null,"phone="+phone,null,null,null,"name collate localized asc");
        if(cursor.getCount() == 0){
            Log.d("My","CURSOR_NULL");
        } else {
            Log.d("My","sub Thread");

            cursor.moveToFirst();
            ArrayList<miniCard> newDataList = new ArrayList<>(dataList);

            if (requestCode==1 && resultCode==RESULT_OK) {
                //读取数据
                newDataList.add(readFromDatabase(cursor));
            } else if (requestCode==2 && resultCode==RESULT_OK){
                //由于从contact_msg_edit返回没能得到item的position，加之数据可能改变，需要在数据库中再次查询
                miniCard card = readFromDatabase(cursor);
                card.avatar = cursor.getBlob(cursor.getColumnIndex("avatar"));
                if(!Collections.replaceAll(newDataList,card,card))
                    Log.w("My","修改后的替换出现错误");
            }

            //中文List排序
            Collections.sort(newDataList, new Comparator<miniCard>() {
                final Collator collator = Collator.getInstance(Locale.CHINESE);
                @Override
                public int compare(miniCard t1, miniCard t2) {
                    CollationKey key1 = collator.getCollationKey(t1.name);
                    CollationKey key2 = collator.getCollationKey(t2.name);
                    return key1.compareTo(key2);
                }
            });

            cursor.close();
            flash(newDataList, mAdapter);
        }
    }

     @Override
     public void onDestroyView() {
         super.onDestroyView();
         //关闭数据库
         mySQLiteOpenHelper.close();
         db.close();
     }

     private void flash(ArrayList<miniCard> newDataList, miniCardAdapter adapter){
        if(newDataList==null)
            return;
        DiffUtil.DiffResult diffResult= DiffUtil.calculateDiff(new MyDiffCallBack(dataList,newDataList),true);
        diffResult.dispatchUpdatesTo(adapter);

        dataList=newDataList;
        adapter.setData(dataList);
    }

    //初始化联系人界面
    private void initDataList(ArrayList<miniCard> mDataList) {
        mDataList.clear();

        //游标
        Cursor cursor=db.query("contact_list_database",null,null,null,null,null,"name collate localized asc");
        cursor.moveToFirst();
        if(cursor.getCount()==0)
            return;

        //初始化时加载头十个联系人的头像
        int count = 0;
        do{
            //从程序的本地数据库读取内容
            //一个耗时的地方,主要是图片访问耗时
            mDataList.add(readFromDatabase(cursor));

            if (count++ < 10){
                //在SQLite中获得的BLOB是字节数组，需要转化为Bitmap用于在ImageView上显示
                mDataList.get(mDataList.size()-1).avatar=cursor.getBlob(cursor.getColumnIndex("avatar"));
                /*
                //判断是否有头像，如果没有则使用默认头像
                if(avatarByte!=null){
                    mDataList.get(mDataList.size()-1).avatar = BitmapFactory.decodeByteArray(avatarByte,0,avatarByte.length);
                }
                 */
            }
        }while(cursor.moveToNext());

        cursor.close();
    }

     /**
      * 将数据从数据库中读入，添加到List中
      * @param cursor 游标
      */
    private miniCard readFromDatabase(Cursor cursor){
        miniCard card=new miniCard();
        card.name = cursor.getString(cursor.getColumnIndex("name"));
        card.nickname = cursor.getString(cursor.getColumnIndex("nickname"));
        card.phone = cursor.getString(cursor.getColumnIndex("phone"));
        card.relationship = relationshipList.get(cursor.getInt(cursor.getColumnIndex("relationship")));
        card.avatar = null;
        card.phoneType=phoneTypeList.get(cursor.getInt(cursor.getColumnIndex("phoneType")));
        card.company=cursor.getString(cursor.getColumnIndex("company"));
        card.email=cursor.getString(cursor.getColumnIndex("email"));
        card.remark=cursor.getString(cursor.getColumnIndex("remark"));
        card.address=cursor.getString(cursor.getColumnIndex("address"));
        card.note=cursor.getString(cursor.getColumnIndex("note"));
        card.star=cursor.getInt(cursor.getColumnIndex("star"));

        return card;
    }

    private void initAdapter(){
        //实例化名片的Adapter
        this.mAdapter=new miniCardAdapter(dataList,getContext());


        mAdapter.setOnClickListener(new miniCardAdapter.ViewHolder.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int id, final int position) {
                if (id == R.id.star_btn_miniView) {
                    Button star_btn= view.findViewById(id);
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
                                        flash(newDataList,mAdapter);
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
    }

     /**
      * 初始化recyclerView
      */
    private void initRecyclerView(){
        //布局管理器
        linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        //设置RecyclerView的位置
        linearLayoutManager.scrollToPosition(currPosition);

        //RecyclerView分割线
        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(view.getContext(),DividerItemDecoration.VERTICAL);

        //实例化RecyclerView，声明所在布局
        RecyclerView recyclerView = view.findViewById(R.id.contact_list);

        //RecyclerView滚动监听器——在停止滚动时加载图片
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if(getActivity() != null){
                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putInt("first",linearLayoutManager.findFirstCompletelyVisibleItemPosition());
                        bundle.putInt("last",linearLayoutManager.findLastCompletelyVisibleItemPosition());
                        msg.what = 1;
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                    Log.d("My","OnScrollStateChange");
                }
            }
        });

        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

     /**
      * 初始化数据库，从系统联系人数据库中获取联系人
      * 只在首次安装后第一次运行启动
      */
    private void initDB(){
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("contact_book",0);
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
            Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    //获取姓名
                    String name = cursor.getString(1);
                    //指定获取NUMBER这一数据
                    String[] phoneProjection = new String[] {
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    };


                    //根据联系人的ID获取此人的电话号码
                    @SuppressLint("Recycle") Cursor phonesCursor = requireContext().getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            phoneProjection,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                            null,
                            null);

                    //获取电话号码
                    if (phonesCursor != null && phonesCursor.getCount() != 0) {
                        phonesCursor.moveToFirst();
                        do{
                            String num = phonesCursor.getString(0);
                            ContentValues values=new ContentValues();
                            //赋予默认值
                            values.put("name",name);
                            values.put("phone",num.replace(" ", ""));
                            values.put("relationship",0);
                            values.put("nickname","");
                            values.put("phoneType",0);
                            values.put("company","");
                            values.put("email","");
                            values.put("remark","");
                            values.put("address","");
                            values.put("note","");
                            values.put("star",0);
                            //插入数据项
                            if(db.insert("contact_list_database",null,values)==-1){
                                Log.w("My",num + " 已存在");
                            }
                        } while(phonesCursor.moveToNext());
                    }

                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        editor.apply();
    }
}