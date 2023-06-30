package ru.borun.freedomnet.ssh;

public interface ISSHService {

    void checkSSHConnection(String host, int port, String user, String pass) throws Exception;

}
