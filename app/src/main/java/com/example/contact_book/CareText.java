package com.example.contact_book;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CareText extends AppCompatActivity {

    EditText phoneinput;
    TextView responsetext;
    String phone="";
    String province="";
    String city="";
    String weatherinfo ="";
    String violet="";
    String message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_text);
        Button sendrequest=(Button)findViewById(R.id.search);
        responsetext=(TextView)findViewById(R.id.response_text);
        phoneinput=(EditText)findViewById(R.id.phone_input) ;
        Intent i=getIntent();
        String p=i.getStringExtra("phone");
        phoneinput.setText(p);
        sendrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone=phoneinput.getText().toString();
                if(phone.length()!=11){
                    Toast.makeText(CareText.this,"输入手机号码有误，请重新输入！",Toast.LENGTH_SHORT).show();
                    phoneinput.setText("");
                }else {
                    srwhuc();
                    if (ContextCompat.checkSelfPermission(CareText.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(CareText.this, new String[]{Manifest.permission.SEND_SMS}, 1);
                    }else{
                        send();
                    }
                    phoneinput.setText("");
                    Toast.makeText(CareText.this,"已发送！",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void send(){
        if("强".equals(violet)){
            message="紫外线过强，注意防晒哦~";
        } else{
            message="紫外线适中，和我一起运动吧！";
        }
        SmsManager sms = SmsManager.getDefault();
        PendingIntent pi = PendingIntent.getBroadcast(CareText.this, 0, new Intent(), 0);
        sms.sendTextMessage(phone, null, message, pi, null);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[]permissions,int[]grantResults){
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    send();
                } else {
                    Toast.makeText(this, "您未授权！", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }
    private void srwhuc(){
        String address1="http://www.webxml.com.cn/WebServices/MobileCodeWS.asmx/getMobileCodeInfo?mobileCode="+phone+"&userID=";
        HttpUtil.sendHttpRequest(address1, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                parseXMLWithPull(response);
                getweather();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void getweather(){
        String address2="http://www.webxml.com.cn/WebServices/WeatherWS.asmx/getWeather?theCityCode="+city+"&theUserID=";
        HttpUtil.sendHttpRequest(address2, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                parseweatherXML(response);
                String displayinfo="手机号："+phone+"\n"+"省份："+province+"\n"+
                        "城市："+city+"\n"+weatherinfo;
                display(displayinfo);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });


    }
    private void parseXMLWithPull(String xmlData){
        String s="";
        try{
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType=xmlPullParser.getEventType();
            while(eventType!=XmlPullParser.END_DOCUMENT){
                String nodename=xmlPullParser.getName();
                switch (eventType){
                    case XmlPullParser.START_TAG:{
                        if("string".equals(nodename)){
                            s=xmlPullParser.nextText();
                        }
                        break;
                    }
                    case XmlPullParser.END_TAG:{

                        break;
                    }
                    default:
                        break;
                }
                eventType=xmlPullParser.next();
            }
            String[] info1=s.split("：");
            String[] info2=info1[1].split(" ");
            province=info2[0];
            city=info2[1];
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void parseweatherXML(String xmlData){
        int count=0;
        String time=null;
        String weather=null;
        String v=null;
        try{
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType=xmlPullParser.getEventType();
            while(eventType!=XmlPullParser.END_DOCUMENT){
                String nodename=xmlPullParser.getName();
                switch (eventType){
                    case XmlPullParser.START_TAG:{
                        if("string".equals(nodename)){
                            count++;
                            if(count==4){
                                time=xmlPullParser.nextText();
                            }
                            if(count==5){
                                weather=xmlPullParser.nextText();
                            }
                            if(count==6){
                                v=xmlPullParser.nextText();
                            }
                        }
                        break;
                    }
                    case XmlPullParser.END_TAG:{
                        break;
                    }
                    default:
                        break;
                }
                eventType=xmlPullParser.next();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        violet=v.split("：")[1].replace("。","");
        weatherinfo="时间:"+time+"\n"+"实时天气:"+weather+"紫外线强度："+violet;
    }

    private void display(final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                responsetext.setText(response);
                phone=null;
                province=null;
                city=null;
                weatherinfo =null;
            }
        });
    }
    static class HttpUtil{

        public static void sendHttpRequest(final String address,final HttpCallbackListener listener){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection=null;
                    BufferedReader reader=null;
                    try{
                        URL url=new URL(address);
                        connection=(HttpURLConnection)url.openConnection();
                        connection.setRequestMethod("GET");
                        InputStream in=connection.getInputStream();
                        reader=new BufferedReader(new InputStreamReader(in));
                        StringBuilder response=new StringBuilder();
                        String line;
                        while((line=reader.readLine())!=null){
                            response.append(line);
                        }
                        if(listener!=null){
                            listener.onFinish(response.toString());
                        }
                    }catch (Exception e){
                        if(listener!=null) {
                            listener.onError(e);
                        }
                    }finally {
                        if(reader!=null){
                            try{
                                reader.close();
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                        if(connection!=null){
                            connection.disconnect();
                        }
                    }
                }
            }).start();

        }
    }
    public interface HttpCallbackListener{
        void onFinish(String response);
        void onError(Exception e);
    }
}
