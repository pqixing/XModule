package com.pqixing.moduleapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class LaunchActivity extends Activity implements AdapterView.OnItemClickListener {
    List<Class> launchClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ListView lvLaunch = (ListView) findViewById(R.id.lv_launch);
        lvLaunch.setOnItemClickListener(this);
        launchClass = Module.installActivity();
        lvLaunch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, launchClass));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(this, launchClass.get(position)));
    }
}
