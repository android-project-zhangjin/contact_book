package com.example.contact_book;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;


import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


//通话记录所使用的Fragment
public class RecordFragment extends Fragment {
    public final String TAG = "MAIN";                       //log使用的tag
    private Context context;
    private View view;
    private SQLiteDatabase db;
    private RecordAdapter adapter;
    private RecyclerView recyclerView;
    private List<Record> recordList = new ArrayList<>();    //通话记录列表
    private String[] columns = {CallLog.Calls.CACHED_NAME// 通话记录的联系人
            , CallLog.Calls.NUMBER          // 通话记录的电话号码
            , CallLog.Calls.DATE            // 通话记录的日期
            , CallLog.Calls.DURATION        // 通话时长
            , CallLog.Calls.TYPE            // 通话类型
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.record_list, container, false);
        long startTime = System.currentTimeMillis(); // 获取开始时间
        initRecord();       // 初始化通话记录数据
        long endTime = System.currentTimeMillis(); // 获取结束时间
        Log.e("MAIN", "初始化数据时间： " + (endTime - startTime) + "ms");

        //创建View
        recyclerView = (RecyclerView) view.findViewById(R.id.record_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        //设置recyclerView每个子项的分割线
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        setAdapter(recordList);
        return view;
    }

    private void setAdapter(List<Record> recordList1) {
        adapter = new RecordAdapter(recordList1);  //装载数据
        adapter.setItemClickListener(new RecordAdapter.setOnClickListener() {   //设置item点击事件
            @Override
            public void Onclick(String s) {
                String number = s;
                //Log.d(TAG, number);
                Intent intent = new Intent(getActivity(), RecordActivity.class);
                intent.putExtra("number", number);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "item点击事件设置完成");
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
        MySQLiteOpenHelper mySQLiteOpenHelper = new MySQLiteOpenHelper(context);
        db = mySQLiteOpenHelper.getWritableDatabase();      //连接数据库
    }

    //初始化数据，从CallLog.Calls.CONTENT_URI拿
    private void initRecord() {
        Log.d(TAG, "record_list初始化数据");
        //checkContentProvider();
        initDB();
        Cursor cursor = db.rawQuery("select *,max(datetime(date)) from record_list_database group by name", null);
        if (cursor == null)
            Toast.makeText(context, "数据库无通话记录。", Toast.LENGTH_LONG).show();
        else
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String number = cursor.getString(cursor.getColumnIndex("number"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                Cursor cursor1 = db.rawQuery("select * from number_place_database where number = ?", new String[]{number});
                String place = "";
                if (cursor1.getCount() != 0) {
                    while (cursor1.moveToNext()) {
                        place = cursor1.getString(cursor1.getColumnIndex("place"));
                    }
                }
                Record record = new Record(name, number, date, time, type, place);     //初始化一条记录
                recordList.add(record);
            }
    }

    private void initDB() {
        final List<String> number_place = new ArrayList<String>();  //保存已在number_place数据库中的号码
        final List<String> number_place_wait = new ArrayList<String>();  //保存准备获取归属地的号码
        final List<String> number_place_new = new ArrayList<String>();    //保存准备插入number_place数据库的新信息
        List<String> number_date = new ArrayList<String>();         //保存已在通话记录数据库中的号码和日期信息
        List<String> number_date_new = new ArrayList<String>();         //保存准备插入通话记录数据库的新信息
        //从数据库读取数据初始化两个list
        Cursor cursor_place = db.rawQuery("select * from number_place_database", null);
        if (cursor_place.getCount() != 0)
            while (cursor_place.moveToNext()) {
                String num_place = cursor_place.getString(cursor_place.getColumnIndex("number"));
                number_place.add(num_place);
            }
        else
            Toast.makeText(context, "首次查询归属地，请稍等", Toast.LENGTH_SHORT).show();
        Cursor cursor_num_date = db.rawQuery("select * from record_list_database", null);
        if (cursor_num_date.getCount()!=0)
            while (cursor_num_date.moveToNext()) {
                String num_date = cursor_num_date.getString(cursor_num_date.getColumnIndex("number")) +
                        cursor_num_date.getString(cursor_num_date.getColumnIndex("date"));
                number_date.add(num_date);
            }

        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                null, null, "DATE ASC");  //正排序
        //依次读取cursor =====注意!虚拟机没有通话记录，要自己先打几个
        if (cursor == null)
            Toast.makeText(context, "暂无通话记录。", Toast.LENGTH_LONG).show();
        else {
            long startTime = System.currentTimeMillis(); // 获取开始时间
            while (cursor.moveToNext()) {
                //初始化各种数据
                String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));   //姓名
                final String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));      //号码
                long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));          //获取通话日期，时间戳
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateLong));
                int duration_int = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));    //获取通话时长，值为多少秒
                int type_int = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));            //获取通话类型：1.呼入2.呼出3.未接5已挂断
                String time = getTime(duration_int);                                                //获取通话时长
                String type = getType(type_int);                                                    //获取通话类型
                String place = "";
                if (name == null || name.equals("")) //没有姓名的联系人用电话号码代替
                    name = number;
                if (!number_date.contains(number + date)) {   //如果数据是不重复的,单条通话记录放入number_date_new
                    number_date_new.add(name+","+number+","+date+","+time+","+type);
                    if (!number_place.contains(number)||number_place.size()==0) {   //把要更新归属地的号码插入列表number_place_wait
                        number_place.add(number);
                        number_place_wait.add(number);
                    }
                }
            }
            //插入联系人信息
            if (number_date_new.size()>0){
                StringBuilder sql = new StringBuilder("insert into record_list_database (name,number,date,time,type) values ");
                for (String ndn : number_date_new) {
                    try {
                        String content = "( \"" + ndn.split(",")[0] + "\",\"" + ndn.split(",")[1] + "\",\""
                                + ndn.split(",")[2] + "\",\""+ ndn.split(",")[3] + "\",\""+ ndn.split(",")[4] + "\"),";
                        sql.append(content);
                    } catch (Exception e) {}
                }
                String str_sql = sql.toString();
                str_sql = str_sql.substring(0, str_sql.length() - 1) + ";";
                Log.e(TAG, "date要插入的语句:" + str_sql);
                db.execSQL(str_sql);
            }
            //获取归属地
            if (number_place_wait.size() > 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (String npw : number_place_wait)
                            number_place_new.add(npw + "," + getPlace(npw));
                        //把新的归属地数据插入num_place
                        if (number_place_new.size() > 0) {
                            StringBuilder sql = new StringBuilder("insert into number_place_database values ");
                            for (String npn : number_place_new) {
                                Log.d(TAG, npn);
                                try {
                                    String content = "( " + npn.split(",")[0] + ",\"" + npn.split(",")[1] + "\"),";
                                    sql.append(content);
                                } catch (Exception e) {

                                }
                            }
                            String str_sql = sql.toString();
                            str_sql = str_sql.substring(0, str_sql.length() - 1) + ";";
                            Log.e(TAG, "place要插入的语句:" + str_sql);
                            db.execSQL(str_sql);
                        }
                    }
                }).start();
            }
            long endTime = System.currentTimeMillis(); // 获取结束时间
            Log.e("MAIN", "循环一遍通话记录的时间： " + (endTime - startTime) + "ms");
        }
    }

    public void refresh() {
        recordList.clear();
        initRecord();
        setAdapter(recordList);
    }

    private String getType(int type_int) {
        String type;
        switch (type_int) {     //给type赋值对应的类型
            case 1:
                type = "呼入";
                break;
            case 2:
                type = "呼出";
                break;
            case 3:
                type = "未接";
                break;
            case 4:
                type = "语音邮件";
                break;
            case 5:
                type = "已挂断";
                break;
            case 6:
                type = "挂断列表";
                break;
            case 7:
                type = "应答";
                break;
            default:
                type = "未知";
                break;
        }
        return type;
    }

    private String getTime(int duration_int) {
        String time = "";
        if (duration_int < 60) {
            time = duration_int + "s";
        } else {
            int m = duration_int / 60;
            int s = (duration_int - (60 * m));
            time = m + "m" + s + "s";
        }
        return time;
    }

    private void insertDB(String name, String number, String date, String time, String type) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("number", number);
        values.put("date", date);
        values.put("time", time);
        values.put("type", type);
        db.insert("record_list_database", null, values);
    }

    private String getPlace(final String number) {
        String address = "https://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=" + number;
        final String[] place = new String[2];
        final int[] flag = new int[1];
        HttpUtil.sendHttpRequest(address, new CareText.HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                String content = response.split("[=]")[1];
                place[0] = "未知";
                try {
                    JSONObject jsonObject = new JSONObject(content);
                    place[0] = jsonObject.optString("province", null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onFinish中获取的返回值" + content);
                place[1] = number; //保证子线程执行完后才继续主线程
                flag[0] = 1;
                //Log.d(TAG, place[0]);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
        try {
            Thread.sleep(200);  //要休眠200毫秒，不然这儿太快出问题
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (flag[0] != 1) {
            //循环等待
        }
        Log.d(TAG, "变成循环等待,取得的地址：" + place[0] + "\t");
        return place[0];
    }

    //getPlace的网络请求封装
    static class HttpUtil {
        public static void sendHttpRequest(final String address, final CareText.HttpCallbackListener listener) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    BufferedReader reader = null;
                    try {
                        URL url = new URL(address);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        InputStream in = connection.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(in, "GBK"));   //判断是否用GBK解析
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        if (listener != null) {
                            listener.onFinish(response.toString());
                        }
                    } catch (Exception e) {
                        if (listener != null) {
                            listener.onError(e);
                        }
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }
            }).start();
        }

        public interface HttpCallbackListener {
            void onFinish(String response);

            void onError(Exception e);
        }
    }

    private void checkContentProvider() {
        //为contentProvider测试一下是否有数据
        Cursor c = db.query("record_list_database", null, null, null, null, null, null);
        if (c == null)
            Toast.makeText(getContext(), "暂无通话记录。", Toast.LENGTH_LONG).show();
        else
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndex("number"));
                Toast.makeText(getContext(), name, Toast.LENGTH_LONG).show();
                break;
            }
    }

    private void checkContentResolver() {
        Uri uri = Uri.parse("content://com.example.contact_book.provider/record_list_database");
        Cursor c = context.getContentResolver().query(uri, null, null, null, null);
        if (c == null)
            Toast.makeText(context, "暂无通话记录。", Toast.LENGTH_LONG).show();
        else
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndex("name"));
                Toast.makeText(context, "读取的第一个名字:" + name, Toast.LENGTH_LONG).show();
                break;
            }
    }

}