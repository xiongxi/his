package com.lq;

import com.lq.util.CommonUtil;

import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by xiongxi on 2018/1/6.
 */
public class Constant {

    static Map<Integer, String> hospitalMap = new HashMap<Integer, String>();
    static Map<Integer, String> moudelMap = new HashMap<Integer, String>();
    static String updateRecordDir;
    static String updatePackageDir;
    static String individuationProcessDir;

    static{
        Properties prop = new Properties();
        try{
            //读取属性文件cx.properties

            InputStreamReader in = new InputStreamReader(Constant.class.getClassLoader().getResourceAsStream("cx.properties"), "UTF-8");

            prop.load(in);

            hospitalMap = CommonUtil.stringToMap(prop.getProperty("hos_name"));
            moudelMap = CommonUtil.stringToMap(prop.getProperty("moudel_name"));
            updateRecordDir = prop.getProperty("update_record_dir");
            updatePackageDir = prop.getProperty("update_package_dir");
            individuationProcessDir = prop.getProperty("individuation_process_dir");
            in.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }



    }
}
