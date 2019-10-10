package com.ljr.easydb.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.ljr.easydb.annotation.DbField;
import com.ljr.easydb.annotation.DbTable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BaseDao<T> implements IBaseDao<T> {

    //持有数据库操作的引用
    private SQLiteDatabase mSQLiteDatabase;
    //表名
    private String mTableName;
    //操作数据库所对应的java类型
    private Class<T> mEntityClass;
    //标识，用来标识是否已经做过初始化
    private boolean mIsInit = false;
    //定义一个缓存空间（key 字段名  value 成员变量）
    private HashMap<String, Field> mCacheMap;

    /**
     * 初始化
     * @return
     */
    protected boolean init(SQLiteDatabase sqLiteDatabase,Class<T> entityClass){
        this.mSQLiteDatabase = sqLiteDatabase;
        this.mEntityClass = entityClass;
        if(!mIsInit){
            //根据传入的Class进行数据表的创建 本例子中对应的是User对象；
            DbTable dbTable = entityClass.getAnnotation(DbTable.class);
            if(dbTable !=null && !"".equals(dbTable.value())){
                mTableName = dbTable.value();
            }else{
                mTableName = entityClass.getName();
            }
            if(!sqLiteDatabase.isOpen()){
                return false;
            }
            String createTableSql = getCreateTableSql();
            sqLiteDatabase.execSQL(createTableSql);
            mCacheMap = new HashMap<>();
            initCacheMap();
            mIsInit = true;
        }
        return mIsInit;
    }

    private void initCacheMap() {
        // 取得所有的列名
        String sql = "select * from "+mTableName+" limit 1,0";
        Cursor cursor = mSQLiteDatabase.rawQuery(sql,null);
        String[] columnNames = cursor.getColumnNames();
        //获取所有的成员变量
        Field[] colunmnFields = mEntityClass.getDeclaredFields();
        //将字段访问权限打开
        for (Field field : colunmnFields) {
            field.setAccessible(true);
        }
        for (String columnName : columnNames) {
            Field columnField = null;
            for ( Field field : colunmnFields ) {
                String fieldName = null;
                if(field.getAnnotation(DbField.class)!=null){
                    fieldName = field.getAnnotation(DbField.class).value();
                }else{
                    fieldName = field.getName();
                }
                if(columnName.equals(fieldName)){
                    columnField = field;
                    break;
                }
            }
            if(columnField!=null){
                mCacheMap.put(columnName,columnField);
            }
        }
    }

    /**
     * 创建数据库表语句
     * @return
     */
    private String getCreateTableSql() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("create table if not exists ");
        stringBuilder.append(mTableName).append("(");
        //反射得到所有的成员变量
        Field[] fields = mEntityClass.getDeclaredFields();
        for (Field field : fields) {
            Class type = field.getType();
            //得到注解的数据库表的列名
            DbField dbField = field.getAnnotation(DbField.class);
            //优先使用注解的表列名，没有则使用变量名为表列名
            if(dbField != null && !"".equals(dbField.value())){
                if(type == String.class){
                    stringBuilder.append(dbField.value()).append(" TEXT,");
                }else if(type== Integer.class){
                    stringBuilder.append(dbField.value()).append(" INTEGER,");
                }else if(type== Long.class){
                    stringBuilder.append(dbField.value()).append(" BIGINT,");
                }else if(type== Double.class){
                    stringBuilder.append(dbField.value()).append(" DOUBLE,");
                }else if(type==byte[].class){
                    stringBuilder.append(dbField.value()).append(" BLOB,");
                }
            }else{
                if(type== String.class){
                    stringBuilder.append(field.getName()).append(" TEXT,");
                }else if(type== Integer.class){
                    stringBuilder.append(field.getName()).append(" INTEGER,");
                }else if(type== Long.class){
                    stringBuilder.append(field.getName()).append(" BIGINT,");
                }else if(type== Double.class){
                    stringBuilder.append(field.getName()).append(" DOUBLE,");
                }else if(type==byte[].class){
                    stringBuilder.append(field.getName()).append(" BLOB,");
                }
            }
        }
        if(stringBuilder.charAt(stringBuilder.length()-1)==','){
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    @Override
    public long insert(T entity) {
        // user 对象 转换为contentvalues  new User(id = 1,name = "ljr",password = "password");
        Map<String,String> map = getValues(entity);
        ContentValues values = getContentValues(map);
        return mSQLiteDatabase.insert(mTableName,null,values);
    }

    private ContentValues getContentValues(Map<String, String> map) {
        ContentValues contentValues = new ContentValues();
        Set keys = map.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            String value = map.get(key);
            if(value != null){
                contentValues.put(key,value);
            }
        }
        return contentValues;
    }

    private Map<String, String> getValues(T entity) {
        HashMap<String,String> map = new HashMap<>();
        // 得到所有的成员变量，user的成员变量
        for (Field field : mCacheMap.values()) {
            field.setAccessible(true);
            // 获取成员变量的值
            try {
                Object object = field.get(entity);
                if (object == null) {
                    continue;
                }
                String value = object.toString();
                // 获取列名
                String key = null;
                DbField dbField = field.getAnnotation(DbField.class);
                if (dbField != null && !"".equals(dbField.value())) {
                    key = dbField.value();
                } else {
                    key = field.getName();
                }
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    map.put(key, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}
