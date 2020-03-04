package test;

import com.pqixing.modularization.utils.GitSSHFactory;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.junit.Test;
import org.omg.SendingContext.RunTime;

import java.io.File;

public class GitTest {

    @Test
    public void testLog() throws Exception {
        //ssh://git@github.com:user/repo.git

//        Runtime.getRuntime().exec("git clone git@gitlab.gz.cvte.cn:robot_application/robot_android/config.git /Users/pqx/Documents/Code/robot/config");
//        Git open = Git.open(new File("/Users/pqx/Documents/Code/robot/config"));

//        open.pull().setTransportConfigCallback(GitSSHFactory.transportConfigCallback).call();
//
        Git master = Git.cloneRepository()
                .setURI("git@gitlab.gz.cvte.cn:robot_application/robot_android/config.git")
                .setTransportConfigCallback(GitSSHFactory.transportConfigCallback)
                .setDirectory(new File("/Users/pqx/Documents/Code/robot/config"))
                .setBranch("master").call();

        System.out.println("----------");
    }


    private void init() {
    }
}
