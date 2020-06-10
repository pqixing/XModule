package com.pqixing.intellij.utils;

import java.util.HashMap;


public class MyModuleGraph extends HashMap<String,String[]> {

    @Override
    public String[] put(String key, String[] value) {
        return super.put(key, null);
    }
}
