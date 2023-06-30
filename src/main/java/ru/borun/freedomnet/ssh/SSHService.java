package ru.borun.freedomnet.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SSHService implements ISSHService {

    private static SSHService instance;

    public static SSHService getInstance() {
        if (instance == null) {
            instance = new SSHService();
        }
        return instance;
    }

    @Override
    public void checkSSHConnection(String host, int port, String user, String pass) throws Exception {
        var session = new JSch().getSession(user, host, port);
        session.setPassword(pass);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(3000);
        session.connect();
        session.disconnect();
    }
}
