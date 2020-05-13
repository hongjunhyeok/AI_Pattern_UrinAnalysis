package com.example.urineanalysis;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class chartmenu extends AppCompatActivity {

    Button btn_chart_to_gloucose ;
    Button btn_chart_to_protein;
    Button btn_chart_to_rbc;
    Button btn_chart_to_ph;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chartmenu2);

    btn_chart_to_gloucose=findViewById(R.id.btn_chart_to_gloucose);
    btn_chart_to_protein=findViewById(R.id.btn_chart_to_protein);
    btn_chart_to_rbc=findViewById(R.id.btn_chart_to_rbc);
    btn_chart_to_ph=findViewById(R.id.btn_chart_to_PH);

    btn_chart_to_gloucose.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(getApplicationContext(),menu_gloucose.class);
            startActivity(i);
        }
    });

    btn_chart_to_protein.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(getApplicationContext(),menu_protein.class);
            startActivity(i);
        }
    });

    btn_chart_to_ph.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Intent i = new Intent(getApplicationContext(),menu_ph.class);
            startActivity(i);

        }
        });

    btn_chart_to_rbc.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(getApplicationContext(),menu_rbc.class);
            startActivity(i);
        }
    });
    }
}
