package test;

import com.pqixing.tools.Logger;
import com.pqixing.tools.Shell;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ShellTest {

    @Test
    public void testShell() {
        Shell.INSTANCE.setLogger(l -> System.out.println(l));
//        Object result = Shell.runSync("git clone https://github.com/pqixing/modularization.git  --progress module",new File("/Users/pqixing/Desktop"),null);
//        Object result = Shell.runSync("adb install -r -t -p /Users/Dev/Code/MyApplication/app/build/outputs/apk/debug/app-debug.apk",null,null);
//        String name = System.currentTimeMillis()+".apk";
//         Shell.runSync("adb push -p /Users/Dev/Code/MyApplication/app/build/outputs/apk/debug/app-debug.apk  /data/local/tmp/"+name,null,null);
//         Shell.runSync("adb shell pm  install -r -t /data/local/tmp/"+name,null,null);
//         Shell.runSync("adb shell rm /data/local/tmp/"+name,null,null);
        Shell.runSync("adb  devices & echo test1 & echo test2");
        System.out.println("end");
    }
}