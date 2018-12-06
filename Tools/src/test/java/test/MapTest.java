package test;

import org.junit.Test;

import java.util.HashSet;

public class MapTest {

    @Test
    public void  testCollection(){
        StringBuilder sb = new StringBuilder();
        HashSet<String> set = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            String txt = i+"com.dachen.this.test.my.my====================================================="+i;
            sb.append(txt).append("#");
            set.add(txt);
        }
        String sbs = sb.toString();

        String t = "xcom.dachen.this.test.my";
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            sbs.contains(t);
        }
        System.out.println("count1 -> "+(System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            set.contains(t);
        }
        System.out.println("count2 -> "+(System.currentTimeMillis() - start));
    }

    @Test
    public void testTime(){
        long time = System.currentTimeMillis();
        int t2 = (int) (time/1000);
        System.out.println(" -> "+time );
        System.out.println(" -> "+t2 );
    }
}
