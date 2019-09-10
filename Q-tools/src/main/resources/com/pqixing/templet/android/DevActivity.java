package com.modularization.dev;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DevActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Start App Success!!", Toast.LENGTH_SHORT).show();
        ListView listView = new ListView(this);
        setContentView(listView);
        try {
            Log.i("DevActivity", "onCreate: ");
            ActivityInfo[] activityInfos = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).activities;

            if (activityInfos != null) listView.setAdapter(new InnerAdapter(activityInfos, this));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

class InnerAdapter extends BaseAdapter {
    final ActivityInfo[] activityInfos;
    final Activity activity;

    public InnerAdapter(ActivityInfo[] activityInfos, Activity activity) {
        this.activityInfos = activityInfos;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return activityInfos.length;
    }

    @Override
    public Object getItem(int i) {
        return activityInfos[i];
    }

    @Override
    public long getItemId(int i) {
        return getItem(i).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ActivityInfo item = (ActivityInfo) getItem(i);
        TextView tv = new TextView(viewGroup.getContext());
        tv.setTextColor(Color.WHITE);
        tv.setText(item.name);
        tv.setTextSize(15f);
        tv.setPadding(30, 30, 30, 30);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.i("DevActivity", "onClick : " + item.name);
                    activity.startActivity(new Intent(activity, Class.forName(item.name)));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        return tv;
    }
}
