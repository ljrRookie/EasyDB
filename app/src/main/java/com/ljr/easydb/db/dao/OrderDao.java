package com.ljr.easydb.db.dao;

import com.ljr.easydb.db.BaseDao;

import java.util.List;

/**
 *  除了基类基础数据库操作外，可以继承BaseDao,添加自定义数据库操作方法
 * @param <T>
 */
public class OrderDao<T> extends BaseDao<T> {
    public List<T> query(String sql){
        return null;
    }
}
