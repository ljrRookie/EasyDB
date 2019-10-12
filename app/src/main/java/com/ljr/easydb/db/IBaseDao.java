package com.ljr.easydb.db;

import java.util.List;

public interface IBaseDao<T> {
    long insert(T entity);

    long update(T entity, T where);

    long delete(T where);

    List<T> query(T where);

    List<T> query(T where, String orderBy, Integer startIndex, Integer limit);
}
