package com.fengmap.indoorPosition.utils;

import com.fengmap.indoorPosition.entity.UserEntity;

/**
 * Created by ACER on 2018/1/16.
 */

public class UserInfo {
    private static UserEntity userEntity;
    private UserInfo(){};

    public static UserEntity getUserEntity(){
        if (userEntity == null){
            userEntity = new UserEntity();
        }
        return userEntity;
    }

}
