package com.zuczug.zuczugetl;

import javolution.util.FastMap;

import java.util.Map;

/**
 * Created by zuczug on 14-11-25.
 */
public class ZuczugEtlStaticField {

    public static Map<String,String> sizeField = FastMap.newInstance();

    static {

        sizeField.put("0","0");
        sizeField.put("2","2");
        sizeField.put("4","4");
        sizeField.put("6","6");
        sizeField.put("8","8");

        sizeField.put("25","0");
        sizeField.put("26","2");
        sizeField.put("27","4");
        sizeField.put("28","6");
        sizeField.put("29","8");

        sizeField.put("F","2");

    }

}
