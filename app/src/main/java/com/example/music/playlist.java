package com.example.music;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
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
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yibo on 2020/12/4.
 */
public class playlist extends AppCompatActivity {
    private int count;//数量
    private TabledatabaseHelper dbHelper;
    private ArrayAdapter adapter;//数据适配器
    private MusicService musicService;
    private ListView listView;
    private ServiceConnection conn = new ServiceConnection() {//绑定到一个服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {//系统调用这个来传送在service的onBind()中返回的IBinder//定义可见一个应用程序组件
            musicService = ((MusicService.MyBinder) service).getService();//Binder是android内独有的跨进程通信方式
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {//Android系统在同service的连接意外丢失时调用这个．比如当service崩溃了或被强杀了．当客户端解除绑定时，这个方法不会被调用
        }
    };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,//读外部存储权限
            Manifest.permission.WRITE_EXTERNAL_STORAGE,};//写外部存储权限
    private List<Music> musics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {//用于Activity之间传输数据用
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);
        getSupportActionBar().hide();//隐藏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏显示

        Intent intent = new Intent(this, MusicService.class);//一般用于启动Activity、启动服务、发送广播等，承担了Android应用程序三大核心组件相互间的通信功能。
        bindService(intent, conn, Context.BIND_AUTO_CREATE);//如果客户端绑定时，第三个参数为Context.BIND_AUTO_CREATE，表示只要绑定存在，就自动建立Serice

        dbHelper = new TabledatabaseHelper(this,"login.db",null,1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();//获取一个用于操作数据库的SQLiteDatabase实例，以只读方式打开数据库
        Cursor cursor = db.query("login",null,null,null,null,null,null);//查询
        musics = new ArrayList<Music>();
        musics.clear();
        count = cursor.getCount();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String url = cursor.getString(cursor.getColumnIndex("url"));
            Music music = new Music();
            music.setTitle(title);
            music.setArtist(artist);
            music.setUrl(url);
            musics.add(music);
            Log.e("huizhong", "music adds succeedly");
        }
        cursor.close();

        Button button = (Button)findViewById(R.id.button2);//初始化暂停键
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.start();
            }
        });

        Button button6 = (Button)findViewById(R.id.button6);//初始化清除所有键
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete("login",null,null);
                Log.e("huizhong","count = "+count);
                for (int i = 1; i <= count; i++) {
                    musics.remove(0);
                }
                adapter.notifyDataSetChanged();//方法强制listview调用getView来刷新每个Item的内容
            }
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {//检查权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);//请求权限
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
        }

        adapter = new MusicAdapter(playlist.this, R.layout.musicitem, musics); //新建想对应的适配器
        // adapter = new ArrayAdapter<String>(playlist.this,android.R.layout.simple_list_item_1,list);     //用字符串适配器试验
        listView = (ListView) findViewById(R.id.listView2);
        listView.setAdapter(adapter);//添加进去

        this.registerForContextMenu(listView);//为视图注册上下文菜单

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Music music = musics.get(position);
                String url = music.getUrl();
                String title = music.getTitle();
                String artist = music.getArtist();

                Intent intent = new Intent("startnew");
                intent.putExtra("url", url);//放入要传递的数据
                intent.putExtra("title", title);
                intent.putExtra("artist", artist);

                final Intent eintent = new Intent(createExplicitFromImplicitIntent(playlist.this, intent));//从隐含意图创建显示
                bindService(eintent, conn, Service.BIND_AUTO_CREATE);//只要绑定存在，就自动建立Serice
                startService(eintent);//通过向 startService() 传递一个 Intent，为该服务提供要保存的数据
            }
        });
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){//ContextMenu菜单就是长按某一个按钮，就会在屏幕的中间弹出ContextMenu
        menu.add(0,1,0,"删除");
    }
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch(item.getItemId()){
            case 1:
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String title = ((TextView)menuInfo.targetView.findViewById(R.id.songname)).getText().toString();

                db.delete("login", "title =?", new String[]{title+""});
                Log.e("huizhong","删除SQL项成功" );
                musics.remove(menuInfo.position);
                adapter.notifyDataSetChanged();//强制刷新
                break;
        }
        return true;
    }
    @Override
    protected void onDestroy() {//破坏链接
        unbindService(conn);
        super.onDestroy();
    }
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);//解析intent过程中返回的信息，实际上就是AndroidManifest.xml 标签的信息
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);//组件名称，创建一个新的意图。将旧的意图用于额外的用途和重复使用
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);//将组建设置为显示
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
}
