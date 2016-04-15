package com.example.administrator.fiction;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/15.
 */
public class SearchService extends Service {
    HandleBroadcastReceiver handleBroadcastReceiver;
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        handleBroadcastReceiver = new HandleBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Protocols.MY_ACTION);
        registerReceiver(handleBroadcastReceiver,intentFilter);
        //SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("fiction.db",SQLiteDatabase.CREATE_IF_NECESSARY,null);
        SQliteHelper sQliteHelper = new SQliteHelper(this,"fiction.db",null,1);
        final List<String[]> fictionList = new ArrayList<>();
        final SQLiteDatabase db = sQliteHelper.getWritableDatabase();

        Cursor cv = db.rawQuery("select * from Fiction", null);
        final StringBuilder stringBuilder = new StringBuilder();
        while (cv.moveToNext()) {
            String[] str = new String[3];
            str[0] = cv.getString(cv.getColumnIndex("name"));
            str[1] = cv.getString(cv.getColumnIndex("chapter"));
            str[2] = cv.getString(cv.getColumnIndex("web"));
            fictionList.add(str);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("Test","read : start" );
                    for (int j = 0; j < fictionList.size(); j++) {
                        Document document = Jsoup.connect(fictionList.get(j)[2]).get();
                        Elements div = document.select("#readV");
                        Document divcontent = Jsoup.parse(div.toString());
                        Element strong = divcontent.select("strong").first();
                        if (!strong.text().equals(fictionList.get(j)[1])) {
                            ContentValues cv = new ContentValues();
                            cv.put("chapter", strong.text());
                            stringBuilder.append(strong.text());
                            stringBuilder.append("||");
                            db.update("Fiction",cv,"name = ?",new String[]{fictionList.get(j)[0]});
                        }
                    }
                    Intent intent1 = new Intent();
                    intent.setAction(Protocols.MY_ACTION_NOTIFICATION).putExtra("newlist", stringBuilder.toString());
                    sendBroadcast(intent1);

                } catch (Exception e) {}
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(handleBroadcastReceiver);
        super.onDestroy();
    }
}
