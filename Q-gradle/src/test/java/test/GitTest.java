package test;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.junit.Test;

import java.io.File;

public class GitTest {

    @Test
    public void testLog() throws Exception {
        Git git = Git.open(new File("/home/pqixing/Desktop/CodeTest/CodeManager"));
        Status call = git.status().call();

        System.out.println(call.isClean());
    }


    private void init() {
    }
}
