package com.pizzk.refresh;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        findViewById(R.id.bt1).setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, NormalListActivity.class));
        });
        findViewById(R.id.bt2).setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, FoldListActivity.class));
        });
    }
}
