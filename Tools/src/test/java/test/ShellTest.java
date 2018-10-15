package test;

import com.pqixing.shell.Shell;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;

public class ShellTest {

    @Test
    public void testShell() throws GitAPIException {
        Shell.INSTANCE.setLogger(l -> System.out.println(l));
        String name = System.currentTimeMillis()+".apk";
         Shell.runSync("adb push -p /Users/Dev/Code/MyApplication/app/build/outputs/apk/debug/app-debug.apk  /data/local/tmp/"+name,null,null);
         Shell.runSync("adb shell pm  install -r -t /data/local/tmp/"+name,null,null);
         Shell.runSync("adb shell rm /data/local/tmp/"+name,null,null);

    }
}