package test;

import com.pqixing.git.PercentProgress;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Test;

import java.io.File;

public class GitTest {
    @Test  public void testClone() throws GitAPIException {
        Git git = Git.cloneRepository().setURI("https://github.com/pqixing/modularization.git").setDirectory(new File("/home/pqixing/Desktop/test/module")).setBranch("3.0")
                .setProgressMonitor(new PercentProgress(l -> System.out.println(l))).setCredentialsProvider(new UsernamePasswordCredentialsProvider( "pengqixing", "pengqixing" )).call();

    }
}
