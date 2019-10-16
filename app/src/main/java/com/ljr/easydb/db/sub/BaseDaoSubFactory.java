package com.ljr.easydb.db.sub;

import android.database.sqlite.SQLiteDatabase;

import com.ljr.easydb.db.BaseDao;
import com.ljr.easydb.db.BaseDaoFactory;

public class BaseDaoSubFactory extends BaseDaoFactory {
    private static final BaseDaoSubFactory mInstance = new BaseDaoSubFactory();

    public static BaseDaoSubFactory getInstance() {
    return mInstance;
    }

    private SQLiteDatabase mSubSQLiteDatabase;

    @Override
    public <T extends BaseDao<M>, M> T getBaseDao(Class<T> daoClass, Class<M> entityClass) {
        BaseDao baseDao = null;
        if(map.get(PrivateDataBaseEnums.database.getValue()) != null){
            return (T) map.get(PrivateDataBaseEnums.database.getValue());
        }
        mSubSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(PrivateDataBaseEnums.database.getValue(),null);
        try {
            baseDao = daoClass.newInstance();
            baseDao.init(mSubSQLiteDatabase,entityClass);

            map.put(PrivateDataBaseEnums.database.getValue(),baseDao);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return (T)baseDao;

    }
}
