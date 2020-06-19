package com.pqixing.modularization.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;

public class GitSSHFactory {
    public static SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
        private String sshKeyFilePath="/Users/pqx/.ssh/id_rsa";
//
//        @Override
//        protected JSch getJSch(final OpenSshConfig.Host hc, FS fs) throws JSchException {
//            JSch jsch = new JSch();
//            jsch.removeAllIdentity();
//            jsch.addIdentity(new File("/Users/pqx/.ssh/id_rsa").getAbsolutePath());
//            jsch.setKnownHosts(new File("/Users/pqx/.ssh/known_hosts").getAbsolutePath());
//            return jsch;
//        }

        @Override
        protected void configure(OpenSshConfig.Host hc, Session session) {
//            java.util.Properties config = new java.util.Properties();
//            config.put("StrictHostKeyChecking", "no");
//            session.setConfig(config);
//            session.setConfig("StrictHostKeyChecking", "no");
//            session.setPassword( "password" );
        }
//
//        public String getSshKeyFilePath() {
//            return sshKeyFilePath;
//        }
//
//        public void setSshKeyFilePath(String sshKeyFilePath) {
//            this.sshKeyFilePath = sshKeyFilePath;
//        }
    };

    public static TransportConfigCallback transportConfigCallback = transport -> {
        if (transport instanceof SshTransport) {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        }
    };

}