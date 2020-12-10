package cn.edu.xmu.pes.liteservice;

import cn.edu.xmu.pes.liteservice.models.UserInfoClass;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

@Repository
public class LiteSSHCaller {
    @Value("$(admin.pesroot)")
    static String pesroot;

    @Value("$(admin.host.process.bashpath)")
    static String bashpath;

    @Value("$(admin.host.process.feature)")
    static String feature;

    @Value("$(admin.host.username)")
    static String username;

    @Value("$(admin.host.password)")
    static String password;

    @Value("$(admin.host.ip)")
    static String ip;


    @PostConstruct
    public void postConstruct() { }

    public static boolean runNew(String taskDirPath, String... args) {
        var session = getSession();
        if (session == null) return false;

        /* * WARNING: setsid xx & 命令如果在后台有输出，会导致进程卡死(bug of JSch) * */
        /* * WARNING: 所以需要将输出重定向到xxx.log中 * */
        String cmdTemplate = "mkdir -p %dir; cd %dir; setsid %bashname %arg0 %arg1 >pes.log 2>&1 &";
        try {
            String result = execGetResult(session,
                    cmdTemplate
                            .replace("%bashname", bashpath)
                            .replace("%dir",    taskDirPath)
                            .replace("%arg0",   args[0])
                            .replace("%arg1",   args[1])
            );
            return result != null;
        } finally {
            session.disconnect();
        }
    }

    public static boolean killall() {
        var session = getSession();
        if (session == null) return false;

        // ps -ef | grep FlyGoose | grep -v grep |cut -c 9-15|xargs kill -9
        String cmdTemplate = "ps -ef | grep %arg | grep -v grep | cut -c 9-15 | xargs kill -9";
        try {
            String result = execGetResult(session, cmdTemplate.replace("%arg", feature));
            return result != null;
        } finally {
            session.disconnect();
        }
    }


    static Date lastQueryTime = new Date();
    static int lastQueryResult = 0;
    /**
     * getRunningPrcessNumber 带缓存功能，3秒内最多连接SSH一次
     */
    public static int getRunningPrcessNumber() {
        if (lastQueryTime.getTime()+3*1000 < new Date().getTime())
            return lastQueryResult;

        var session = getSession();
        if (session == null) return -1;

        // ps -ef | grep FlyGoose | grep -v grep  | wc -l
        String cmdTemplate = "ps -ef | grep %arg | grep -v grep  | wc -l";
        try {
            String numString = execGetResult(session, cmdTemplate.replace("%arg", feature));
            if (numString == null) return -1;
            if (numString.equals("")) return 0;
            try {
                int num = Integer.parseInt(numString);
                lastQueryTime = new Date();
                lastQueryResult = num;
                return num;
            } catch (NumberFormatException e) { return -1; }
        } finally {
            session.disconnect();
        }
    }

    static Session getSession() {
        Session session = null;

        int port = 22;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, ip, port);
            session.setPassword(password);
            session.setUserInfo(new UserInfoClass());
            var config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setTimeout(2000);
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
