package test;

import com.pqixing.Tools;
import com.pqixing.git.GitUtils;

import com.pqixing.git.PercentProgress;
import com.pqixing.interfaces.ICredential;
import com.pqixing.interfaces.ILog;
import com.pqixing.shell.Shell;

import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class GitTest {
    @Test
    public void testClone() throws GitAPIException {
        init();
        long start = System.currentTimeMillis();
        GitUtils.clone("https://github.com/pqixing/modularization.manager", "/home/pqixing/Desktop/test2");
        System.out.println("end count " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        Shell.runSync("manager clone --progress https://github.com/pqixing/modularization.manager", new File("/home/pqixing/Desktop/test"));
        System.out.println("end count " + (System.currentTimeMillis() - start));
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


    @Test
    public void testBranch() throws IOException, GitAPIException {
        init();
        Git open = Git.open(new File("/opt/Code/dachen/YHQ/MedicalProject"));
//        PullResult call = open.pull().setProgressMonitor(new PercentProgress(l -> System.out.println(l))).setCredentialsProvider(new UsernamePasswordCredentialsProvider(GitUtils.credentials.getUserName(),GitUtils.credentials.getPassWord())).call();
//        System.out.println(call.isSuccessful()+call.toString());
        open.checkout().setName("master").setForce(true).addPath().getName();
//        open.checkout().setName("master4444").setCreateBranch(true).call();

        open.log().setMaxCount(3).call().forEach(new Consumer<RevCommit>() {
            @Override
            public void accept(RevCommit revCommit) {
                System.out.println(revCommit.getFullMessage());
            }
        });

        open.branchCreate()
        List<Ref> refs = open.branchList().call();
        Ref ref = refs.get(0);

        Repository repository = open.getRepository();
        String branch = repository.getBranch();
        Object master = repository.getRemoteName("origin");
        System.out.println(master);
        open.close();
    }
}
