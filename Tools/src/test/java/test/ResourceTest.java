package test;

import org.junit.Test;

import java.io.*;

public class ResourceTest {

    @Test
    public void readText() throws IOException {
        InputStream stream = getClass().getResourceAsStream("/test.txt");

        System.out.println(new BufferedReader(new InputStreamReader(stream)).readLine());
    }
}
