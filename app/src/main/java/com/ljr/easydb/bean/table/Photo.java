package com.ljr.easydb.bean.table;

import com.ljr.easydb.annotation.DbField;
import com.ljr.easydb.annotation.DbTable;

@DbTable("TB_PHOTO")
public class Photo {


    private String time;
    @DbField("SRC")
    private String path;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
