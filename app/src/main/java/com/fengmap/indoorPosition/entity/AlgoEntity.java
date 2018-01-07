package com.fengmap.indoorPosition.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ACER on 2018/1/7.
 */

public class AlgoEntity {
    private String algorithm_name;
    private int algorithm_code;
    private List<String> algorithm_list;

    public AlgoEntity(int algorithm_code){
        this.algorithm_code = algorithm_code;
        initList();
    }

    private void initList(){
        algorithm_list = new ArrayList<>();
        algorithm_list.add("knn");
        algorithm_list.add("bayes");
    }

    public String getName(){
        return algorithm_list.get(algorithm_code);
    }
}
