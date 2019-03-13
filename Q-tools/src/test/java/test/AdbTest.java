//package test;
//
//import org.junit.Test;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//
//import se.vidstige.jadb.JadbConnection;
//import se.vidstige.jadb.JadbDevice;
//import se.vidstige.jadb.JadbException;
//import se.vidstige.jadb.managers.Package;
//import se.vidstige.jadb.managers.PackageManager;
//
//public class AdbTest {
//
//    @Test
//    public void testDevices() throws IOException, JadbException {
//        JadbConnection jadb = new JadbConnection();
//
//        JadbDevice jadbDevice = jadb.getDevices().get(0);
//        System.out.println(jadbDevice.getSerial());
//        System.out.println(jadbDevice.getState().toString());
//        PackageManager pm = new PackageManager(jadbDevice);
//        List<Package> packages = pm.getPackages();
//        File file = new File("/opt/Code/github/modularization/Root/build/apk/Medical-master.apk");
//        String s = "am start -n com.dachen.medicalcircle/com.dachen.dgroupdoctor.ui.SplashActivity";
////        pm.installWithOptions(new File("/opt/Code/github/modularization/Root/build/apk/Medical-master.apk"), Arrays.asList(PackageManager.REINSTALL_KEEPING_DATA, PackageManager.ALLOW_TEST_APK));
//       jadbDevice.executeShell(s);
//    }
//}
