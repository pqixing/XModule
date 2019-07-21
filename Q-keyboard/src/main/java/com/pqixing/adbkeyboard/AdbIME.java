package com.pqixing.adbkeyboard;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.InputConnection;
import com.android.adbkeyboard.R;

public class AdbIME extends InputMethodService {

    private BroadcastReceiver mReceiver = null;

    @Override
    public View onCreateInputView() {
        View mInputView = getLayoutInflater().inflate(R.layout.ime_view, null);
        mInputView.findViewById(R.id.btn_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdbReceiver.setClipText(getApplicationContext(), getInputText());
            }
        });
        mInputView.findViewById(R.id.btn_paste).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setInputText(AdbReceiver.getClipText(getApplicationContext()));
            }
        });
        mInputView.findViewById(R.id.btn_ime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setInputText("");
            }
        });

        return mInputView;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(AdbReceiver.ACTION_GET_VERSION);
        filter.addAction(AdbReceiver.ACTION_GET_TEXT);
        filter.addAction(AdbReceiver.ACTION_GET_TEXT_EDIT);
        filter.addAction(AdbReceiver.ACTION_SET_TEXT);
        filter.addAction(AdbReceiver.ACTION_SET_TEXT_EDIT);
        mReceiver = new AdbReceiver(this);
        registerReceiver(mReceiver, filter);
    }

    public void onDestroy() {
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    String getInputText() {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) return null;
        inputConnection.performContextMenuAction(android.R.id.selectAll);
        CharSequence sData = inputConnection.getSelectedText(0);
        String result = sData == null ? "" : sData.toString();
        setInputText(result);
        return result;
    }

    boolean setInputText(String input) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) return false;
        if (input == null) input = "";
        inputConnection.beginBatchEdit();
        inputConnection.performContextMenuAction(android.R.id.selectAll);
        inputConnection.commitText(input, 0);
        inputConnection.endBatchEdit();
        return false;
    }
}
