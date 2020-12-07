package com.example.music;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by yibo on 2020/12/4.
 */
public class TabledatabaseHelper extends SQLiteOpenHelper {//辅助抽象类，对数据库进行增删改查

    public static final String sql= "create table login("
            +"id integer primary key autoincrement,"
            +"title String,"
            +"artist String,"
            +"url String)";
    private Context mcontext;//提供了一个应用运行所需要的信息，资源，系统服务，比如Activity之间的切换，服务的启动等
    public TabledatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
        mcontext = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db){//数据库访问类，封装了一系列数据库操作的API，可以对数据进行增删改查操作
        db.execSQL(sql);
        Toast.makeText(mcontext,"Create succeeded",Toast.LENGTH_SHORT).show();//显示信息
    }
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        db.execSQL("drop table if exists ACCOUNT_PASSWORD");//执行insert、delete、update和CREATE TABLE之类有更改行为的SQL语句
        onCreate(db);
    }
}
