package com.deveire.dev.deveiregeofindergolf;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class StatsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        TextView statsText = (TextView) findViewById(R.id.statsText);

        statsText.setText("" + getIntent().getStringExtra("data"));

    }
}
