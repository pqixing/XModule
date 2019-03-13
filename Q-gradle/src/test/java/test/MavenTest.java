package test;


import com.alibaba.fastjson.JSON;

import org.junit.Test;

import java.io.File;

import groovy.lang.GroovyClassLoader;

public class MavenTest {

    @Test
    public void testMavenParse() throws Exception {
//        String testHelper = JGroovyHelper.getImpl(IExtHelper.class).getTestHelper();
        Class aClass = new GroovyClassLoader().parseClass(new File("/opt/Code/github/modularization/Gradle/src/main/resources/setting/templet.java"));
        String s = JSON.toJSONString(aClass.newInstance());
        System.out.println("------- ---"+ s);
//        VersionManager.INSTANCE.parseNetVersions("http://192.168.3.7:9527/nexus/content/repositories/androidtest","com/dachen/android");
    }
}
