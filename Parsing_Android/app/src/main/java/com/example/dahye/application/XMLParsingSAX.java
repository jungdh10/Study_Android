package com.example.dahye.application;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.dahye.application.MainActivity;
import com.example.dahye.application.R;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

//SAX-(작업순서:스레드 ->파서->핸들러)
public class XMLParsingSAX extends AppCompatActivity {
    //기사 제목을 저장할 리스트
    ArrayList<String> titleList;
    ArrayAdapter<String> adapter;
    ListView listView;

    //링크를 저장할 리스트
    ArrayList<String> linkList;

    //대화상자
    ProgressDialog progressDialog;

    //업데이트를 위한 레이아웃
    SwipeRefreshLayout swipeRefreshLayout;



    //UI갱신을 위한 핸들러
    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message message){
            //대화상자 닫아야 하니까
            progressDialog.dismiss();
            adapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    //데이터를 다운받을 스레드
    class ThreadEx extends Thread{
        @Override
        public void run(){
            //다운로드 받은 문자열을 저장할 객체 생성
            StringBuilder sb= new StringBuilder();
            //데이터 다운로드 받기
            try{
                //데이터를 다운로드 받을 주소 생성
                URL url=new URL("http://rss.donga.com/health.xml");
                //연결
                HttpURLConnection con=(HttpURLConnection)url.openConnection();
                //옵션 설정
                con.setConnectTimeout(20000);
                con.setUseCaches(false);

                //데이터 읽기
                BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()));

                while(true){
                    String line=br.readLine();
                    if(line==null)break;
                    sb.append(line+"\n");
                }
                br.close();
                con.disconnect();
                Log.e("다운로드 받은 문자열", sb.toString());

            }catch(Exception e){
                Log.e("다운로드 실패", e.getMessage());
            }


            try{
                //SAX Parser를 이용한 파싱 요청
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                XMLReader reader = parser.getXMLReader();
                //파싱을 수행해 줄 객체 생성
                SaxHandler saxHandler = new SaxHandler();
                //XML 파싱을 위임
                reader.setContentHandler(saxHandler);
                //데이터 전달
                InputStream inputStream = new ByteArrayInputStream(sb.toString().getBytes("utf-8"));
                //파싱 시작
                reader.parse(new InputSource(inputStream));
                //핸들러에게 메시지 전달
                handler.sendEmptyMessage(0);

            }catch (Exception e){
                Log.e("파싱 에러", e.getMessage());
            }

        }
    }

    //XML 파싱을 수행해 줄 클래스
    class SaxHandler extends DefaultHandler{
        String content =null;

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            //Log.e("태그", "문서읽기 시작");
            //파싱시작할 때 비우기 위한 설정
            titleList.clear();
            linkList.clear();
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
            //Log.e("태그", "문서읽기 종료");
            //Log.e("제목", titleList.toString());
            //Log.e("링크", linkList.toString());
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            //Log.e("시작 태그", qName);
            //시작태그가 끝나면 내용을 집어 넣어야 하니까 시작할 때 데이터를 넣을 수 없음
            content=null;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            //Log.e("종료 태그", qName);
            if(qName.equals("title")){
                titleList.add(content);
            }else if(qName.equals("link")){
                linkList.add(content);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            //Log.e("태그 안의 내용", new String(ch));
            //글자수가 남아서 특수문자가 출력되니까 start, length로 몇개 인지 알려줘야 함(배열이니까)
            content=new String(ch, start, length);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xmlparsing_sax);


        //ListView 초기화
        titleList=new ArrayList<>();
        adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titleList);
        listView=(ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);

        //다른 변수 초기화
        linkList=new ArrayList<>();
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        progressDialog=ProgressDialog.show(this, "건강 기사", "다운로드 중...");


        Thread th=new ThreadEx();
        th.start();

        //하단으로 드래그 했을 때 수행할 이벤트 핸들러
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        progressDialog = ProgressDialog.show(XMLParsingSAX.this, "건강 기사", "업데이트 중");
                        Thread th = new ThreadEx();
                        th.start();
                    }
                });


        //리스트 뷰의 항목을 클릭했을 때 호출되는 이벤트 핸들러
        listView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){

                String link = linkList.get(position);
                //크롬 기사 링크로 가기

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(intent);
            }
        });

        Button back=(Button)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(XMLParsingSAX.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}