package com.example.administrator.fiction;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.net.Uri;

import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private List<String[]> userList = new ArrayList<String[]>();
    private MyAdapter mAdapter;
    private SQLiteDatabase db;
    private Handler handlerUI = new Handler() {
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case MSG_TO_UI_SUCCESS:
                        System.out.println("线程来了数据");
                        mAdapter.notifyDataSetChanged();
                        for (int i = 0; i < userList.size(); i++) {
                            UpdateFiction(userList.get(i));
                        }
                        break;
                    case MSG_TO_UI_FAIL:
                        new AlertDialog.Builder(MainActivity.this).setTitle("Waring")
                                .setMessage("TestTestTest~")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();

                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private static final int MSG_FROM_UI_SUCCESS = 2;
    private static final int MSG_TO_UI_SUCCESS = 1;
    private static final int MSG_TO_UI_FAIL = 0;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Data部分/////////////////////////////////


        //Database
        db = this.openOrCreateDatabase("fiction.db",SQLiteDatabase.CREATE_IF_NECESSARY,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Fiction (name VARCHAR,chapter VARCHAR,web VARCHAR)");
        userList = getData();
    ///////////////////////////////////////////

        //控件部分////////////////////////////////
        //toolbar```````````````````````````````````````````````````````
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb);
        setSupportActionBar(toolbar);
        //menu
        Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String msg = "";
                switch (item.getItemId()) {
                    case R.id.action_add:
                        Add_Dialog();
                        break;
                    case R.id.action_surf:

                        break;
                    case R.id.action_refresh:
                        new Thread(new MyThread()).start();
                        //getData();
                        break;
                }
                return true;
            }
        };
        toolbar.setOnMenuItemClickListener(onMenuItemClick);

        //Adapter relative``````````````````````````````````````````````

        mAdapter = new MyAdapter(this);
        ListView listview = (ListView) findViewById(R.id.list);
        listview.setAdapter(mAdapter);
        try {
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Uri uri = Uri.parse((String) ((TextView) view.findViewById(R.id.FictionWeb)).getText());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);

                }
            });
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT);
        }
        //``````````````````````````````````````````````````````````````````````

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.administrator.fiction/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.administrator.fiction/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


    static class ViewHolder {
        TextView FictioName;
        TextView NewChapter;
        TextView FictionWeb;
        Button del;
    }


    private void InsertFiction(String[] item) {
        ContentValues cv = new ContentValues();
        cv.put("name",item[0]);
        if (item[1] != "")
            cv.put("chapter",item[1]);
        cv.put("web",item[2]);
        db.insert("Fiction",null,cv);
    }

    private void RemoveFiction(String[] item) {
        db.delete("Fiction","name=?",new String[]{item[0]});
    }

    private void UpdateFiction(String[] item) {
        ContentValues cv = new ContentValues();//实例化ContentValues
        cv.put("chapter",item[1]);//添加要更改的字段及内容
        String whereClause = "name=?";//修改条件
        //修改条件的参数
        db.update("Fiction",cv,whereClause,new String[]{item[0]});//执行修改
    }


    //适配器自定义
    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflator;

        public MyAdapter(Context context) {
            this.mInflator = LayoutInflater.from(context);

        }
        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        @Override
        public Object getItem(int pos) {
            return userList.get(pos);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflator.inflate(R.layout.add_fiction,null);
                holder.FictioName = (TextView) convertView.findViewById(R.id.FictionName);
                holder.NewChapter = (TextView) convertView.findViewById(R.id.NewChapter);
                holder.FictionWeb = (TextView) convertView.findViewById(R.id.FictionWeb);
                holder.del = (Button)convertView.findViewById(R.id.del);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.FictioName.setText(userList.get(position)[0]);
            holder.NewChapter.setText(userList.get(position)[1]);
            holder.FictionWeb.setText(userList.get(position)[2]);

            holder.del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RemoveFiction(userList.get(position));
                    userList.remove(position);
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }

    //menu填充
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    //获取数据，HTTP部分~~~


    private List<String[]> getData()  {
        try {
            Cursor cv = db.rawQuery("SELECT * FROM Fiction", null);
            List<String[]> list = new ArrayList<String[]>();
            while (cv.moveToNext()) {
                String str[] = new String[3];
                str[0] = cv.getString(cv.getColumnIndex("name"));
                str[1] = cv.getString(cv.getColumnIndex("chapter"));
                str[2] = cv.getString(cv.getColumnIndex("web"));
                list.add(str);
            }
            cv.close();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
/*
        String str[] = {"全职法师","","http://www.qidian.com/Book/3489766.aspx"};
        list.add(str);
        String str1[] = {"飞天","","http://www.qidian.com/Book/2227457.aspx"};
        list.add(str1);
        String str2[] = {"天醒之路","","http://www.qidian.com/Book/3434836.aspx"};
        list.add(str2);
*/
        return null;
    }

    public class MyThread implements Runnable {
            @Override
            public void run() {
                try {
                    System.out.println("读取前```");
                    for (int j = 0; j < userList.size(); j++) {
                        Document document = Jsoup.connect(userList.get(j)[2]).get();
                        Elements div = document.select("#readV");
                        Document divcontent = Jsoup.parse(div.toString());
                        Element strong = divcontent.select("strong").first();
                        userList.get(j)[1] = strong.text();
                    }
                    System.out.println("读取完```");
                    handlerUI.obtainMessage(MSG_TO_UI_SUCCESS).sendToTarget();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    handlerUI.obtainMessage(MSG_TO_UI_FAIL).sendToTarget();
                }
            }

    }

    //XML解析
  /*  private String readXML(InputStream is) {

        XmlPullParser xmlPullParser = Xml.newPullParser();
        String data = "";
        try {
            xmlPullParser.setInput(is,"UTF-8");
            int eventType = xmlPullParser.getEventType();
            while (eventType != xmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    //文章开始，可以进行初始化
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //开始标签
                    case XmlPullParser.START_TAG:
                        String name = xmlPullParser.getName();
                        break;
                    //结束标签</xxx>
                    case XmlPullParser.END_TAG:

                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "";

    }*/



    //添加小说的对话框
    private void Add_Dialog() {
        LayoutInflater li = LayoutInflater.from(this);
        final View vv = li.inflate(R.layout.add_dialog, null);

        //get a builder and set the view
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Prompt");
        builder.setView(vv);
        builder.setPositiveButton("确定",new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int size = userList.size();
                for (int j = 0; j < size; j++) {
                    if ( userList.get(j)[0].toString().contentEquals(((TextView)vv.findViewById(R.id.fn)).getText()) ) {
                        Toast.makeText(MainActivity.this
                                , "小说重名，请重新添加", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                String[] newItem = new String[3];
                newItem[0] = ((TextView)vv.findViewById(R.id.fn)).getText().toString();
                newItem[1] = " ";
                newItem[2] = ((TextView)vv.findViewById(R.id.fw)).getText().toString();
                userList.add(newItem);
                InsertFiction(newItem);
                Toast.makeText(MainActivity.this
                        , "添加成功", Toast.LENGTH_SHORT).show();
                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();
    }
}
