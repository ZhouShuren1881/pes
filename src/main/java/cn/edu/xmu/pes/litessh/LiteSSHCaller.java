package cn.edu.xmu.pes.litessh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.util.Properties;

public class LiteSSHCaller {
    public static boolean runNew(String... args) {
        String bashname = System.getProperty("admin.host.process.bashname");

        var session = getSession();
        if (session == null) return false;

        /* * WARNING: setsid xx & 命令如果在后台有输出，会导致进程卡死(bug of JSch) * */
        /* * WARNING: 所以需要将输出重定向到xxx.log中 * */
        // setsid ./FlyGoose.sh OUTPUT_Directory mallGateway adminGateway > OUTPUT_Directory&
        String cmdTemplate = "setsid %bashname %arg0 %arg1 %arg2 >OUTPUT_Directory.log 2>&1 &";
        try {
            String result = execGetResult(session,
                    cmdTemplate.replace("%bashname", bashname)
                            .replace("%arg0", args[0])
                            .replace("%arg1", args[1])
                            .replace("%arg2", args[2])
            );
            return result != null;
        } finally {
            session.disconnect();
        }
    }

    public static boolean killall() {
        String feature = System.getProperty("admin.host.process.feature");

        var session = getSession();
        if (session == null) return false;

        // ps -ef | grep FlyGoose | grep -v grep |cut -c 9-15|xargs kill -9
        String cmdTemplate = "ps -ef | grep %arg0 | grep -v grep | cut -c 9-15 | xargs kill -9";
        try {
            String result = execGetResult(session, cmdTemplate.replace("%arg0", feature));
            return result != null;
        } finally {
            session.disconnect();
        }
    }

    public static int getRunningPrcessNumber() {
        String feature = System.getProperty("admin.host.process.feature");

        var session = getSession();
        if (session == null) return -1;

        // ps -ef | grep FlyGoose | grep -v grep  | wc -l
        String cmdTemplate = "ps -ef | grep %arg0 | grep -v grep  | wc -l";
        try {
            String numString = execGetResult(session, cmdTemplate.replace("%arg0", feature));
            if (numString == null) return -1;
            if (numString.equals("")) return 0;
            try {
                int num = Integer.parseInt(numString);
                return num;
            } catch (NumberFormatException e) { return -1; }
        } finally {
            session.disconnect();
        }
    }

    static Session getSession() {
        Session session = null;

        String username = System.getProperty("admin.host.username");
        String password = System.getProperty("admin.host.password");
        String ip = System.getProperty("admin.host.ip");
        int port = 22;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, ip, port);
            session.setPassword(password);
            session.setUserInfo(new UserInfoClass());
            var config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setTimeout(3000);
            session.connect();
        } catch (JSchException e) {
            return null;
        }
        return session;
    }

    static String execGetResult(Session session, String cmd) {
        ChannelExec channelExec = null;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(cmd);
            channelExec.setInputStream(null);
            channelExec.setErrStream(null);
            channelExec.connect();

            var stream = channelExec.getInputStream();
            var s = new String(stream.readAllBytes());
            stream.close();
            return s;
        } catch (IOException | JSchException e) {
            return null;
        } finally {
            try {
                channelExec.disconnect();
            } catch (Exception e) {}
        }
    }
}
