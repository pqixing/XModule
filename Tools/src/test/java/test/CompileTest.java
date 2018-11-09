package test;

import com.alibaba.fastjson.JSON;

import groovy.lang.GroovyClassLoader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class CompileTest {

    @Test
    public void testCompile() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, InstantiationException {
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//        int result = compiler.run(null, null, null, "/opt/Code/github/modularization/Tools/src/test/iterface/test/BeanTest.iterface");
//        System.out.println(result==0?"成功":"失败");
//
//
//        URL[] urls = new URL[] {new URL("file:/"+"/opt/Code/github/modularization/Tools/src/test/iterface/test/")};
//        URLClassLoader loader = new URLClassLoader(urls);
//        Class c = loader.loadClass("BeanTest2");
//        for (Field f :c.getFields()){
//            f.setAccessible(true);
//            System.out.println(f.get(null));
//        }
        Class aClass = new GroovyClassLoader().parseClass(new File("/opt/Code/github/modularization/Tools/src/test/iterface/test/M.groovy"));
//        //通过Object把数组转化为参数
//        GroovyTestBean bean = new GroovyTestBean();
//        bean.setName("tesssssssssssssssssssss");
//        GroovyTestBean test = JSON.parseObject(JSON.toJSONString(bean), GroovyTestBean.class);
        System.out.println(JSON.toJSONString(aClass.newInstance().toString()));
    }
}

