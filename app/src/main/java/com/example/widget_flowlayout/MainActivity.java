package com.example.widget_flowlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Flowlayout flowlayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flowlayout= (Flowlayout) findViewById(R.id.flowlayout);
        flowlayout.setOnItemClickListener(new Flowlayout.OnItemClickListener() {
            @Override
            public void onItemClick(String text) {
                Toast.makeText(MainActivity.this,text,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
