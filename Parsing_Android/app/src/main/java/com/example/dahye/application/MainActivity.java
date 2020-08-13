package com.example.dahye.application;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.dahye.application.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button json=(Button)findViewById(R.id.json);
        Button json_openapi=(Button)findViewById(R.id.json_openapi);
        Button xml_DOM=(Button)findViewById(R.id.xml_DOM);
        Button xml_SAX=(Button)findViewById(R.id.xml_SAX);

        json.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, JSONParsing.class);
                startActivity(intent);
                finish();
            }
        });

        json_openapi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, JSONOpenAPI.class);
                startActivity(intent);
                finish();
            }
        });

        xml_DOM.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, XMLParsingDOM.class);
                startActivity(intent);
                finish();
            }
        });

        xml_SAX.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, XMLParsingSAX.class);
                startActivity(intent);
                finish();
            }
        });
    }
}




