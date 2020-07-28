package com.pqixing.modularization.utils;

import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;

public class GitSSHFactory {
    public static SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {

        @Override
        protected void configure(OpenSshConfig.Host hc, Session session) {
        }
    };

    public static TransportConfigCallback transportConfigCallback = transport -> {
        if (transport instanceof SshTransport) {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        }
    };

}