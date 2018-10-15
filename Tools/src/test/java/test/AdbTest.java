package test;

import org.junit.Test;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.IOException;

public class AdbTest {

    @Test public void testDevices() throws IOException, JadbException {
        JadbConnection jadb = new JadbConnection();

        JadbDevice jadbDevice = jadb.getDevices().get(0);
        System.out.println(jadbDevice.getSerial());
        System.out.println( jadbDevice.getState().toString());
    }
}
