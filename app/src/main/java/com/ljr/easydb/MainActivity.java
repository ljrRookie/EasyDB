package com.ljr.easydb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ljr.easydb.bean.table.User;
import com.ljr.easydb.db.BaseDao;
import com.ljr.easydb.db.BaseDaoFactory;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "EASY_DB >>>>> ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //插入数据
        findViewById(R.id.btn_insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseDao<User> baseDao = BaseDaoFactory.getInstance().getBaseDao(User.class);
                baseDao.insert(new User(12, "ljr", "123456"));
                baseDao.insert(new User(1, "lzq", "6547521"));
                baseDao.insert(new User(3, "lzq", "634521"));
                baseDao.insert(new User(24, "lzq", "654321"));
                baseDao.insert(new User(65, "lzq", "65457884321"));
                baseDao.insert(new User(2, "lzq", "65445454321"));
                baseDao.insert(new User(7, "lzq", "21212"));
                baseDao.insert(new User(5, "林佳荣", "999999"));
                baseDao.insert(new User(60, "林子淇", "000000"));
            }
        });

        //更新数据
        findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update TB_USER where name='ljr' set password='ljr123'
                BaseDao<User> baseDao = BaseDaoFactory.getInstance().getBaseDao(User.class);
                User user = new User(5, "林佳豪", "555333");
                User where = new User();
                where.setId(5);
                baseDao.update(user, where);
            }
        });
        //删除数据
        findViewById(R.id.btn_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseDao<User> baseDao = BaseDaoFactory.getInstance().getBaseDao(User.class);
                User where = new User();
                where.setName("ljr");
                baseDao.delete(where);


            }
        });

        //查询数据
        findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseDao<User> baseDao = BaseDaoFactory.getInstance().getBaseDao(User.class);
                User where = new User();
                where.setName("lzq");
                List<User> query = baseDao.query(where);
                for (int i = 0; i < query.size(); i++) {
                    Log.e(TAG," 数据库数据 第"+i+"条: "+ query.get(i).toString());
                }
            }
        });
    }
}
