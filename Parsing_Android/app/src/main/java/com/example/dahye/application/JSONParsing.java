package com.example.dahye.application;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.dahye.application.MainActivity;
import com.example.dahye.application.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class JSONParsing extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;

    //ListView 출력관련 함수(이름 출력하기)
    ArrayList<String> nameList;
    ArrayAdapter<String> adapter;
    ListView listView;

    //상세보기를 위해서 id를 저장할 List(id를 클릭했을 경우)
    ArrayList<String> idList;

    //데이터를 다운로드 받는 동안 보여질 대화상자
    ProgressDialog progressDialog;

    //listView의 데이터를 다시 출력하고 대화상자를 닫는 핸들러 생성
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message message){
            adapter.notifyDataSetChanged();
            progressDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    //데이터를 다운로드 받아서 파싱한 후 핸들러를 호출하는 스레드 생성
    //화면에 보여지는 거니까 onResume사용
    class ThreadEx extends Thread{
        @Override
        public void run(){
            StringBuilder sb=new StringBuilder();
            try{
                //다운로드 받는 코드
                URL url=new URL("http://172.30.1.55:8080/android/listperson");
                //Connection 연결하기
                HttpURLConnection con=(HttpURLConnection)url.openConnection();
                //옵션 설정
                con.setConnectTimeout(20000);
                con.setUseCaches(false);

                //문자열을 다운로드 받기 위한 스트림 만들기
                BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()));

                //문자열을 다운로드 받아서 sb에 추가하기
                while(true){
                    String line=br.readLine();
                    if(line==null){
                        break;
                    }
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
                JSONArray ar=new JSONArray(sb.toString());

                //여러번 실행하기 위해 클리어해주기
                nameList.clear();
                idList.clear();

                //배열 순회
                for(int i=0; i<ar.length(); i++){
                    //Array에서 가져올 때는 JSONObject
                    JSONObject object=ar.getJSONObject(i);
                    //객체에서 personname의 값을 가져와서 nameList에 추가
                    //int도 String으로 가져올 수 있으니까 getString
                    nameList.add(object.getString("personname"));
                    //id도 가져오기
                    idList.add(object.getString("personid"));
                }
                //핸들러 호출 - 다시 출력(스레드는 다시 출력못하니까 핸들러로 다시 출력)
                handler.sendEmptyMessage(0);
            }catch(Exception e){
                Log.e("파싱 에러", e.getMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jsonparsing);


        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new ThreadEx().start();
            }
        });


        nameList =new ArrayList<>();
        idList =new ArrayList<>();

        adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nameList);

        listView=(ListView)findViewById(R.id.personlist);

        listView.setAdapter(adapter);

        //리스트 뷰에서 항목을 클릭했을 때 수행할 내용
        listView.setOnItemClickListener(
                new ListView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(JSONParsing.this, JSONDetailActivity.class);
                        intent.putExtra("personid", idList.get(position));
                        startActivity(intent);
                    }
                });


        Button back=(Button)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(JSONParsing.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    //onResume 메소드를 재정의 해서 스레드 생성 및 시작(순서대로하기 위해 onCreate 뒤에 작성)
    //엑티비티가 실행 될 때 호출되는 메소드
    @Override
    protected void onResume(){
        super.onResume();
        progressDialog=ProgressDialog.show(JSONParsing.this, "", "다운로드 중...");
        new ThreadEx().start();
    }
}