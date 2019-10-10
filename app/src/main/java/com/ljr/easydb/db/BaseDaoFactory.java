package com.ljr.easydb.db;

import android.database.sqlite.SQLiteDatabase;

public class BaseDaoFactory {
    private static final BaseDaoFactory mInstance= new BaseDaoFactory();
    public static BaseDaoFactory getInstance(){
        return mInstance;
    }

    private SQLiteDatabase sqLiteDatabase;
    private String sqlitePath;
    private BaseDaoFactory(){
        sqlitePath = "data/data/com.ljr.easydb/easy.db";
        sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(sqlitePath,null);
    }
    // 生产basedao对象
    public <T> BaseDao<T> getBaseDao(Class<T> entityClass){
        BaseDao baseDao = null;
        try {
            baseDao = BaseDao.class.newInstance();
            baseDao.init(sqLiteDatabase,entityClass);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return baseDao;
    }
}
