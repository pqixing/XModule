//package com.pqixing.clieper;
//
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Intent;
//import android.os.IBinder;
//import android.widget.Toast;
//
//public class CliperService extends Service {
//    private BroadcastReceiver r;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this,"onStart",Toast.LENGTH_SHORT).show();
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//}
