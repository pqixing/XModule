package test;

import com.pqixing.Tools;
import com.pqixing.interfaces.ICredential;
import com.pqixing.interfaces.ILog;
import com.pqixing.modularization.ProjectInfo.manager.manager;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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


    private void init() {
        Tools.init(new ILog() {
            @Override
            public void println(@Nullable String l) {
                System.out.println(l);
            }
        }, "", new ICredential() {
            @NotNull
            @Override
            public String getUserName() {
                return "pengqixing";
            }

            @NotNull
            @Override
            public String getPassWord() {
                return "pengqixing";
            }
        });
    }
}
