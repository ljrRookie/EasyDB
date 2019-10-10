package com.ljr.easydb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ljr.easydb.bean.table.User;
import com.ljr.easydb.db.BaseDao;
import com.ljr.easydb.db.BaseDaoFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseDao<User> baseDao = BaseDaoFactory.getInstance().getBaseDao(User.class);
                long ljr = baseDao.insert(new User(12, "ljr", "123456"));
            }
        });
    }
}
