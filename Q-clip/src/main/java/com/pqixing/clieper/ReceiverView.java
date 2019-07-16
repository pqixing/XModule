package com.pqixing.clieper;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.view.View;
import android.widget.Toast;

public class ReceiverView extends View {
    public static ReceiverView onAttachedView;
    ClipHelperReceiver receiver;
    private Runnable cmd;
    private int[] colors = new int[]{Color.parseColor("#880000FF"), Color.GRAY};
    private int color = 0;

    public ReceiverView(Context context) {
        super(context);
        receiver = new ClipHelperReceiver();
        setBackgroundColor(colors[color]);
        cmd = new Runnable() {
            @Override
            public void run() {
                if (isAttachedToWindow()) {
                    setBackgroundColor(colors[color = (color + 1) % 2]);
                    postDelayed(cmd, 1000);
                }
            }
        };
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        onAttachedView = this;
        getContext().registerReceiver(receiver, new IntentFilter("com.peqixing.clip.helper"));
        Toast.makeText(getContext(), "启动PC助手", Toast.LENGTH_SHORT).show();
        postDelayed(cmd, 1000);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(receiver);
        onAttachedView = null;
        Toast.makeText(getContext(), "退出PC助手", Toast.LENGTH_SHORT).show();
    }
}
