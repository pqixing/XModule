package test;

import com.pqixing.Tools;
import com.pqixing.git.GitUtils;

import com.pqixing.git.PercentProgress;
import com.pqixing.interfaces.ICredential;
import com.pqixing.interfaces.ILog;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
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

public class GitTest {
    @Test
    public void testClone() throws GitAPIException, IOException {
        Git git = Git.open(new File("/home/pqixing/Desktop/CodeTest/CodeManager"));
        Status call = git.status().call();

        System.out.println(call.isClean());
    }

    private void init() {
        Tools.init(new ILog() {
            @Override
            public void printError(int exitCode, @Nullable String l) {

            }

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

        List<Ref> call = open.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

        for (Ref c : call) {
            System.out.println(c.getName());
        }
        checkOut("yqq_2.2",open);
        checkOut("yqq_2.2",open);


//        open.log().setMaxCount(3).call().forEach(new Consumer<RevCommit>() {
//            @Override
//            public void accept(RevCommit revCommit) {
//                System.out.println(revCommit.getFullMessage());
//            }
//        });
//
//        List<Ref> refs = open.branchList().call();
//        Ref ref = refs.get(0);
//
//        Repository repository = open.getRepository();
//        String branch = repository.getBranch();
//        Object master = repository.getRemoteName("origin");
//        System.out.println(master);
//        open.close();
    }

    public void checkOut(String branchName, Git git) throws GitAPIException {
        List<Ref> local = git.branchList().call();
        String end = "/" + branchName;
        for (Ref c : local) {
            if (c.getName().endsWith(end)) {
                git.checkout().setName(branchName).call();
                System.out.println("切换本地分支");
                return;
            }
        }
        List<Ref> remote = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
        for (Ref c : remote) {
            if (c.getName().endsWith(end)) {
                git.checkout().setName(branchName).setCreateBranch(true).setStartPoint(c.getName()).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).call();
                System.out.println("切换远程分支");
                return;
            }
        }
    }
}
