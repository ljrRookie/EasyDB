package com.ljr.easydb.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BaseDaoFactory {
    private static final BaseDaoFactory mInstance= new BaseDaoFactory();
    public static BaseDaoFactory getInstance(){
        return mInstance;
    }

    private SQLiteDatabase sqLiteDatabase;
    private String sqlitePath;

    //设计要给数据库连接池，new容器，只要new一次，下次就不回再创建了。考虑多线程的问题。
    protected Map<String,BaseDao> map = Collections.synchronizedMap(new HashMap<String, BaseDao>());


    protected BaseDaoFactory(){
        sqlitePath = "data/data/com.ljr.easydb/easy.db";
        sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(sqlitePath,null);
    }
    // 生产basedao对象
    public <T extends BaseDao<M>,M> T getBaseDao(Class<T> daoClass,Class<M> entityClass){
        BaseDao baseDao = null;
        if(map.get(daoClass.getSimpleName()) != null){
            return (T)map.get(daoClass.getSimpleName());
        }
        try {
            baseDao = daoClass.newInstance();
            baseDao.init(sqLiteDatabase,entityClass);
            map.put(daoClass.getSimpleName(),baseDao);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return (T)baseDao;
    }
}
