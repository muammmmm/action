package com.example.music;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;


/**
 * Created by yibo on 2020/12/4.
 */
public class LocalMusicActivity extends AppCompatActivity {//将内存中的MP3拿过来

    private TabledatabaseHelper dbHelper;
    private MusicService musicService;
    private MusicAdapter adapter;
    private Boolean Exist = false;
    ListView listView;
    private ServiceConnection conn = new ServiceConnection() {//绑定到一个服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = ((MusicService.MyBinder) service).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,};
    private List<Music> musics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.localmusic);
        getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        listView = (ListView) findViewById(R.id.listView);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_EXTERNAL_STORAGE);
        }

        Button button = (Button)findViewById(R.id.button);//播放/暂停
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.start();
            }
        });
        Button button3 = (Button)findViewById(R.id.button3);//播放列表
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent3 = new Intent(LocalMusicActivity.this,playlist.class);
                startActivity(intent3);
            }
        });



        dbHelper = new TabledatabaseHelper(this,"login.db",null,1);


        Findmusic findmusic = new Findmusic();
        musics = findmusic.getmusics(LocalMusicActivity.this.getContentResolver());   //找到资源，music型组
        adapter = new MusicAdapter(LocalMusicActivity.this,R.layout.musicitem,musics); //新建想对应的适配器
        listView.setAdapter(adapter);

        listView.setOnItemClickListener (new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Log.e("huizhong", "onitemclick");
                Music music = musics.get(position);
                String url = music.getUrl();
                String title = music.getTitle();
                String artist = music.getArtist();

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                Cursor cursor = db.query("login",null,null,null,null,null,null);
                Log.e("huizhong","当前歌曲的title是："+title );
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToNext();
                    Log.e("huizhong","当前游标title是："+cursor.getString(cursor.getColumnIndex("title")));
                    if(title.equals(cursor.getString(cursor.getColumnIndex("title")))) {
                        Log.e("huizhong","已经存在歌曲，不插入了" );
                        Exist = true;
                        break;
                    }
                }
                Log.e("huizhong","当前歌曲是否存在 "+Exist );
                if(Exist==false) {
                    Log.e("huizhong", "创建键");
                    values.put("title", title);
                    values.put("artist", artist);
                    values.put("url", url);
                    db.insert("login", null, values);
                    values.clear();
                    Log.e("huizhong", "成功插入login表");
                    Exist = false;
                }
                cursor.close();
                Intent intent = new Intent("startnew");
                intent.putExtra("url",url);
                intent.putExtra("title",title);
                intent.putExtra("artist",artist);

                final Intent eintent = new Intent(createExplicitFromImplicitIntent(LocalMusicActivity.this,intent));
                bindService(eintent,conn, Service.BIND_AUTO_CREATE);
                startService(eintent);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // 检索所有与给定意图匹配的服务
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // 确保只找到一个匹配
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // 获取组件信息并创建组件名称
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        //创建一个新的意图。将旧的意图用于额外的用途和重复使用
        Intent explicitIntent = new Intent(implicitIntent);
        // 设置组件为显式
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
}
