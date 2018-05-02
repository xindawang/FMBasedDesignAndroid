package com.fengmap.indoorPosition.entity;

import java.util.HashMap;

/**
 * Created by ACER on 2018/4/22.
 */

public class ApNameEntity {
    public static HashMap<String, String> changeName = new HashMap<>();
    public static void setMap(){
        changeName.put("abc2", "ap1");
        changeName.put("abc3", "ap2");
        changeName.put("abc4", "ap3");
        changeName.put("abc6", "ap4");
        changeName.put("abc7", "ap5");
        changeName.put("abc8", "ap6");
        changeName.put("TP-LINK_0236", "ap7");
        changeName.put("TP-LINK_3E5D", "ap8");
        changeName.put("TP-LINK_115D", "ap9");
        changeName.put("TP-LINK_1646", "ap10");
        changeName.put("Xiaomi_3525_CADA", "ap11");
        changeName.put("Xiaomi_31CB_CE34", "ap12");
        changeName.put("MERCURY_CFF9", "ap13");
        changeName.put("MERCURY_BD09", "ap14");
        changeName.put("MERCURY_B932", "ap15");
    }

    public static HashMap<String, String> getMap(){
        if (changeName.size()==0) setMap();
        return changeName;
    }
}
