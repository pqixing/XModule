package test;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Test;

import java.io.File;
import java.util.function.Consumer;

public class GitTest {

    @Test
    public void testLog() throws Exception {
        Git git = Git.open(new File("/opt/Code/dachen/YHQ/MedicalProject"));
        Iterable<RevCommit> call = git.log().setMaxCount(1).call();

        int[] i = new int[1];
        RevWalk revWalk = new RevWalk( git.getRepository() );
        call.forEach(new Consumer<RevCommit>() {
            @Override
            public void accept(RevCommit revCommit) {
                revCommit.getId();
            }
        });
        System.out.println("end "+i[0]);

    }
}
