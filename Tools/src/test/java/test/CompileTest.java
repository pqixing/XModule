package test;

import groovy.lang.GroovyClassLoader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class CompileTest {

    @Test
    public void testCompile() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//        int result = compiler.run(null, null, null, "/opt/Code/github/modularization/Tools/src/test/java/test/BeanTest.java");
//        System.out.println(result==0?"成功":"失败");
//
//
//        URL[] urls = new URL[] {new URL("file:/"+"/opt/Code/github/modularization/Tools/src/test/java/test/")};
//        URLClassLoader loader = new URLClassLoader(urls);
//        Class c = loader.loadClass("BeanTest2");
//        for (Field f :c.getFields()){
//            f.setAccessible(true);
//            System.out.println(f.get(null));
//        }
        Class aClass = new GroovyClassLoader().parseClass(new File("/opt/Code/github/modularization/Tools/src/test/java/test/BeanTest"));
        //通过Object把数组转化为参数
        System.out.println(aClass.getName());
    }
}
