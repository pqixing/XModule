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
        Shell.INSTANCE.setLogger(new Logger() {
            @Override
            public void log(@NotNull String l) {
                System.out.println(l);
            }
        });
        Object result = Shell.runSync("git clone https://github.com/pqixing/modularization.git  --progress module",new File("/Users/pqixing/Desktop"),null);
//        String result = Shell.runSync("echo test",new File("/Users/pqixing/Desktop"),null);
        System.out.println(result);
    }
}