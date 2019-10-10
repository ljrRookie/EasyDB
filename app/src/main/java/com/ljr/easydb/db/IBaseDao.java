package com.ljr.easydb.db;

public interface IBaseDao<T> {
    long insert(T entity);
}
