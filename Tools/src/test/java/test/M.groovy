package test

import com.alibaba.fastjson.JSON;

public class M {

    private String name = "text11111111"
    @Override
    public String toString() {
        return JSON.toJSONString(this)
    }
}