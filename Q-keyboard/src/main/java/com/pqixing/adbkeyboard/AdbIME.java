package com.pqixing.adbkeyboard;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.adbkeyboard.R;

import java.util.ArrayList;
import java.util.List;

public class AdbIME extends InputMethodService {

    private BroadcastReceiver mReceiver = null;
    private SharedPreferences sp;
    private int historyCount = 20;
    private ListView historyView;

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
        historyView = mInputView.findViewById(R.id.lv_history);
        initHistory(historyView, getHistoryView());

        return mInputView;
    }

    private List<String> getHistoryView() {
        String key = "historyView";
        ArrayList<String> historys = new ArrayList<>(historyCount);
        for (int i = 0; i < historyCount; i++) {
            String h = sp.getString(key + i, null);
            if (h == null) break;
            historys.add(h);
        }
        return historys;
    }

    private void addHistory(String h) {
        List<String> historys = getHistoryView();
        historys.remove(h);
        historys.add(0, h);
        while (historys.size() > historyCount) historys.remove(historys.size() - 1);

        SharedPreferences.Editor edit = sp.edit();
        String key = "historyView";
        for (int i = 0; i < historys.size(); i++) {
            edit.putString(key + i, historys.get(i));
        }
        edit.apply();
        initHistory(historyView,historys);
    }

    private void initHistory(ListView listView, List<String> historys) {
        if(listView==null) return;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        listView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, historys) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setSingleLine();
                }
                return view;
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getApplicationContext().getSharedPreferences("AdbIme", 0);
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
        addHistory(input);
        return true;
    }
}
