package com.ljr.easydb.db.sub;

import com.ljr.easydb.bean.table.User;
import com.ljr.easydb.db.BaseDaoFactory;
import com.ljr.easydb.db.dao.UserDao;

import java.io.File;

public enum PrivateDataBaseEnums {
    database("");
    private String value;

    PrivateDataBaseEnums(String value) {
    }
    public String getValue(){
        UserDao userDao = BaseDaoFactory.getInstance().getBaseDao(UserDao.class, User.class);
        if(userDao != null){
            User curUser = userDao.getCurrentUser();
            if(curUser != null){
                File file = new File("data/data/com.ljr.easydb/");
                if(!file.exists()){
                    file.mkdir();
                }
                return file.getAbsolutePath() + "/user_" + curUser.getId() + "_private.db";
            }
        }
        return null;
    }
}
