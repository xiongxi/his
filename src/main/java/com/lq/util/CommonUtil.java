package com.lq.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiongxi on 2018/1/7.
 */
public class CommonUtil {

    public static Map<Integer, String> stringToMap(String str){
        Map<Integer, String> resultMap = new HashMap<>();
        String[] array = str.split(",");
        int count = 0;
        for(String s : array){
            resultMap.put(count++, s);
        }
        return resultMap;
    }
}
