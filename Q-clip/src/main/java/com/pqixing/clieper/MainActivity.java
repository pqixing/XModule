package com.pqixing.clieper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(Activity.RESULT_OK);
        if (ReceiverView.onAttachedView == null) showFloatView();
        else {
            getWindowManager().removeView(ReceiverView.onAttachedView);
        }
        finish();
    }

    public void showFloatView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //启动Activity让用户授权
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Toast.makeText(this, "需要悬浮窗来保证广播接收,请开启悬浮窗权限", Toast.LENGTH_LONG).show();
        } else {//弹出一个悬浮窗, 防止应用被杀掉
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(10, 10,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                    , WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    , PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.START | Gravity.TOP;
            getWindowManager().addView(new ReceiverView(getApplicationContext()), params);

        }

    }
}
