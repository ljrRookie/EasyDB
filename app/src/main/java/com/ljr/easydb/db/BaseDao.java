package com.ljr.easydb.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.ljr.easydb.annotation.DbField;
import com.ljr.easydb.annotation.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    //定义一个缓存空间（key 字段名: "USER_ID"  value 成员变量: "private java.lang.Integer com.ljr.easydb.bean.table.User.id"）
    private HashMap<String, Field> mCacheMap;

    /**
     * 初始化
     * @return
     */
    public boolean init(SQLiteDatabase sqLiteDatabase,Class<T> entityClass){
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

    /**
     * 插入数据
     * @param entity 插入实体
     * @return
     */
    @Override
    public long insert(T entity) {
        // user 对象 转换为contentvalues  new User(id = 1,name = "ljr",password = "password");
        Map<String,String> map = getValues(entity);
        ContentValues values = getContentValues(map);
        return mSQLiteDatabase.insert(mTableName,null,values);
    }

    /**
     * 更新数据
     * @param entity 修改实体
     * @param where 搜索实体
     * @return
     */
    @Override
    public long update(T entity, T where) {
        Map map = getValues(entity);
        ContentValues values = getContentValues(map);
        Map whereMap = getValues(where);
        Condition condition = new Condition(whereMap);
        return mSQLiteDatabase.update( mTableName,values,condition.whereCause,condition.whereArgs);
    }

    /**
     * 删除数据
     * @param where
     * @return
     */
    @Override
    public long delete(T where) {
        Map map = getValues(where);
        Condition condition = new Condition(map);
        return mSQLiteDatabase.delete(mTableName,condition.whereCause,condition.whereArgs);
    }

    @Override
    public List<T> query(T where) {
        return query(where,null,null,null);
    }

    @Override
    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit) {
        Map map = getValues(where);
        // select * from tableName  limit 0,10;
        String limitString = null;
        //String seclections = "where 1=1 and id=? and name=?";
        //String selectionArgs = String[]{,""};
        //select * from tableName where id=? and name=?
        if(startIndex != null && limit != null){
            limitString = startIndex+" , "+limit;
        }
        Condition condition = new Condition(map);
        Cursor cursor = mSQLiteDatabase.query(mTableName,null,condition.whereCause,condition.whereArgs,null,null,
                orderBy,limitString);
        // 定义一个解析游标的方法
        List<T> result = getResult(cursor,where);
        return result;
    }
    private List<T> getResult(Cursor cursor, T obj) {
        ArrayList list = new ArrayList();
        Object item = null;// User user = null;
        while (cursor.moveToNext()){
            try {
                item = obj.getClass().newInstance(); // user = new User(); user.setId(cursor.getId);
                Iterator iterator = mCacheMap.entrySet().iterator();// 成员变量
                while (iterator.hasNext()){
                    Map.Entry entry = (Map.Entry)iterator.next();
                    // 获取列名
                    String columnName = (String)entry.getKey();
                    // 以列名拿到列名在游标中的位置
                    int columnIndex = cursor.getColumnIndex(columnName);
                    // 获取成员变量的类型
                    Field field = (Field) entry.getValue();
                    Class type = field.getType();
                    // cursor.getString(columnIndex);
                    if(columnIndex != -1){
                        if(type == String.class){
                            // User user = new User
                            // user.setId(1);  id.set(user,1);
                            field.set(item,cursor.getString(columnIndex));
                        }else if(type== Double.class){
                            field.set(item,cursor.getDouble(columnIndex));
                        }else if(type== Integer.class){
                            field.set(item,cursor.getInt(columnIndex));
                        }else if(type== Long.class){
                            field.set(item,cursor.getLong(columnIndex));
                        }else if(type==byte[].class){
                            field.set(item,cursor.getBlob(columnIndex));
                        }
                    }
                }
                list.add(item);
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return list;
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




    private class Condition{
        private String whereCause;
        private String[] whereArgs;

          public Condition(Map<String,String> whereMap){
            ArrayList list = new ArrayList();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("1=1");
            // 获取所有的字段名
            Set keys = whereMap.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()){
                String key = (String)iterator.next();
                String value = whereMap.get(key);
                if(value != null){
                    stringBuilder.append(" and ").append(key).append(" =?");
                    list.add(value);
                }
            }
            this.whereCause = stringBuilder.toString();
            this.whereArgs = (String[])list.toArray(new String[list.size()]);
        }
    }

}
