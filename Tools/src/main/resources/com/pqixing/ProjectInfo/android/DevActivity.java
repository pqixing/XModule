package com.modularization.dev;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class DevActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this,"Start App Success!!",Toast.LENGTH_SHORT).show();
    }
}
