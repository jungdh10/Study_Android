package com.example.dahye.application;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import android.os.*;
import android.widget.Toast;

import com.example.dahye.application.R;

import org.json.JSONObject;

public class JSONDetailActivity extends AppCompatActivity {
    TextView title, patronus, age;
    ImageView img;

    //다운로드 받는 동안 화면에 출력될 대화상자
    ProgressDialog progressDialog;
    //상위 Activity로 부터 넘겨받을 id
    String personid;
    //itemid를 이용해서 서버에서 받아온 데이터를 저장할 변수
    Map<String, String> map;

    //화면 갱신을 위한 핸들러
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message message){
            if(message.what==0){
                //대화상자 제거
                progressDialog.dismiss();
                //텍스트뷰에 데이터 출력
                title.setText(map.get("personname"));
                patronus.setText(map.get("patronus"));
                age.setText(map.get("age"));
                //파일 이름을 가지고 이미지를 다운로드 받는 스레드 생성 및 시작
                ImgThread th=new ImgThread(map.get("picture"));
                th.start();

            }else if(message.what==1){
                //Message의 obj 를 이미지 뷰에 출력
                Bitmap bitmap = (Bitmap)message.obj;
                if(bitmap == null){
                    Toast.makeText(JSONDetailActivity.this, "이미지 다운로드 실패", Toast.LENGTH_LONG).show();
                }else{
                    img.setImageBitmap(bitmap);
                }

            }
        }
    };

    //상세보기를 위한 스레드
    class ThreadEx extends Thread{
        @Override
        public void run(){
            StringBuilder sb=new StringBuilder();
            try{
                //다운로드 받는 코드(이번엔 getperson)
                URL url=new URL("http://172.30.1.55:8080/android/getperson?personid="+personid);
                //Connection 만들기
                HttpURLConnection con=(HttpURLConnection)url.openConnection();
                //옵션 설정
                con.setConnectTimeout(20000);
                con.setUseCaches(false);

                //문자열을 다운로드 받기 위한 스트림 만들기
                BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()));

                //문자열을 다운로드 받아서 sb에 추가하기
                while(true){
                    String line=br.readLine();
                    if(line==null) break;
                    sb.append(line+"\n");
                }
                //사용한 스트림과 연결 해제
                br.close();
                con.disconnect();
            }catch(Exception e){
                Log.e("다운로드 실패", e.getMessage());
            }

            //파싱하는 코드
            try{
                //대괄호니까 배열- JSONArray(대괄호가 벗져기즌 것)(중괄호는 객체)
                //전체 문자열을 배열로 변경
                JSONObject person=new JSONObject(sb.toString());

                map=new HashMap<>();
                map.put("personname", person.getString("personname"));
                map.put("age", person.getString("age"));
                map.put("patronus", person.getString("patronus"));
                map.put("picture", person.getString("picture"));

                //핸들러 호출 - 다시 출력(스레드는 다시 출력못하니까 핸들러로 다시 출력)
                handler.sendEmptyMessage(0);
            }catch(Exception e){
                Log.e("파싱 에러", e.getMessage());
            }
        }
    }

    //파일이름을 가지고 이미지를 다운로드 받는 스레드
    class ImgThread extends Thread{
        //파일이름을 넘겨받을 거니까 변수와 생성자 만들기
        String filename;

        public ImgThread(String filename){
            this.filename=filename;
        }

        @Override
        public void run(){
            try{
                //spec:이미지파일의 경로
                InputStream is=new URL("http://172.30.1.55:8080/android/img/"+filename).openStream();
                Bitmap bitmap= BitmapFactory.decodeStream(is);
                is.close();
                Message message=new Message();
                message.obj=bitmap;
                message.what=1;
                handler.sendMessage(message);
            }catch(Exception e){
                Log.e("이미지 다운로드 실패", e.getMessage());
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jsondetail);

        title=(TextView)findViewById(R.id.title);
        age=(TextView)findViewById(R.id.age);
        patronus=(TextView)findViewById(R.id.patronus);
        img=(ImageView)findViewById(R.id.img);
        Button back=(Button)findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //현재 엑티비티 제거
                finish();
            }
        });

        //이전 화면에서 데이터 받아오기
        Intent intent=getIntent();
        personid=intent.getStringExtra("personid");

        //스레드 시작
        progressDialog =ProgressDialog.show(this, "", "다운로드 중");
        new ThreadEx().start();

    }

}
