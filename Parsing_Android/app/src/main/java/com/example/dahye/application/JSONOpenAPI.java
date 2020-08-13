package com.example.dahye.application;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.dahye.application.MainActivity;
import com.example.dahye.application.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class JSONOpenAPI extends AppCompatActivity {
    String json;
    ListView listView;
    ArrayList<String> list;
    ArrayAdapter<String> adapter;

    Handler handler=new Handler(){
        public void handleMessage(Message message){
            adapter.notifyDataSetChanged();
        }
    };

    class ThreadEx extends Thread{
        //다운로드 받은 문자열을 저장할 변수
        String json="";
        public void run(){
            try{
                //다운로드 받을 주소 생성
                //검색어로 입력받아서
                EditText bookname=(EditText)findViewById(R.id.bookname);
                //"java"를 검색했을 때지만 한글로 "자바"를 입력했을 때 다른결과가 나오지 않도록 인코딩
                String sam= URLEncoder.encode(bookname.getText().toString());
                //파라미터는 순서가 없음
                URL url=new URL("https://dapi.kakao.com/v3/search/book?target=title&query=" + sam);
                //URL 연결 객체 생성
                HttpURLConnection con=(HttpURLConnection)url.openConnection();
                //Kakao API의 인증 설정
                con.setRequestProperty("Authorization", "KakaoAK 311288dc5e49578a3cb237223baf684a");
                con.setConnectTimeout(20000);
                //data가 계속 변하니까 false
                con.setUseCaches(false);

                BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb=new StringBuilder();

                while(true){
                    String line = br.readLine();
                    if(line == null)break;
                    sb.append(line + "\n");
                }
                json = sb.toString();
                //다운로드 받은 문자열 확인
                Log.e("json", json);

                br.close();
                con.disconnect();
            }catch(Exception e){
                Log.e("다운로드 실패", e.getMessage());
            }

            //json 파싱
            try{
                JSONObject book=new JSONObject(json);
                JSONArray meta=book.getJSONArray("documents");
                Log.e("documents", meta.toString());
                //배열 데이터를 순회
                //오라클검색하고 클리어해주면 다른 검색어를 다시 입력가능
                list.clear();
                for(int i=0; i<meta.length(); i++){
                    JSONObject books=meta.getJSONObject(i);
                    //List에 제목과 세일 가격을 가져와서 추가
                    list.add(books.getString("title")+ "\n" + books.getString("sale_price"));
                }
                //핸들러를 불러서 리스트 뷰룰 다시 출력하도록 하기
                handler.sendEmptyMessage(0);
            }catch(Exception e){
                Log.e("파싱 에러", e.getMessage());
            }

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jsonopen_api);

        listView=(ListView)findViewById(R.id.listView);
        list=new ArrayList<>();
        adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        Button json=(Button)findViewById(R.id.json);
        json.setOnClickListener(new View.OnClickListener(){
            @Override
                public void onClick(View v) {
                    ThreadEx th= new ThreadEx();
                    th.start();

            }
        });

        Button back=(Button)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(JSONOpenAPI.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
