package cn.edu.xmu.pes.liteservice;

import cn.edu.xmu.pes.liteservice.models.UserInfoClass;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

@Repository
public class LiteSSHCaller {
    Logger logger = LoggerFactory.getLogger(LiteSSHCaller.class);

    @Value("${admin.pesroot}")
    String pesroot;

    @Value("${admin.host.process.bashpath}")
    String bashpath;

    @Value("${admin.host.process.feature}")
    String feature;

    @Value("${admin.host.username}")
    String username;

    @Value("${admin.host.password}")
    String password;

    @Value("${admin.host.ip}")
    String ip;


    @PostConstruct
    public void postConstruct() {
        getSession();
    }

    public boolean runNew(String taskDirPath, String... args) {
        var session = getSession();
        if (session == null) return false;

        /* * WARNING: setsid xx & 命令如果在后台有输出，会导致进程卡死(bug of JSch) * */
        /* * WARNING: 所以需要将输出重定向到xxx.log中 * */
        String cmdTemplate = "mkdir -p %dir 1>/dev/null 2>&1; cd %dir; setsid %bashname %dir %arg0 %arg1 >/dev/null 2>&1 &";
        String result = execGetResult(session,
                cmdTemplate
                        .replace("%bashname", bashpath)
                        .replace("%dir",    taskDirPath)
                        .replace("%arg0",   args[0])
                        .replace("%arg1",   args[1])
        );
        return result != null;
    }

    public boolean killall() {
        var session = getSession();
        if (session == null) return false;

        // ps -ef | grep FlyGoose | grep -v grep |cut -c 9-15|xargs kill -9
        String cmdTemplate = "ps -ef | grep %arg | grep -v grep | cut -c 9-15 | xargs kill -9";
        String result = execGetResult(session, cmdTemplate.replace("%arg", feature));
        return result != null;
    }


    Date lastQueryTime = new Date(3661000);
    int lastQueryResult = 0;
    /**
     * getRunningPrcessNumber 带缓存功能，3秒内最多连接SSH一次
     */
    public int getRunningPrcessNumber() {
        if (lastQueryTime.getTime()+6*1000 >= new Date().getTime())
            return lastQueryResult;

        var session = getSession();
        if (session == null) return -1;

        // ps -ef | grep FlyGoose | grep -v grep  | wc -l
        String cmdTemplate = "ps -ef | grep %arg | grep -v grep  | wc -l";
        String numString = execGetResult(session, cmdTemplate.replace("%arg", feature));
        if (numString == null) return -1;
        numString = numString.trim();
        if (numString.equals("")) return 0;
        try {
            int num = Integer.parseInt(numString);
            lastQueryTime = new Date();
            lastQueryResult = num;
            return num;
        } catch (NumberFormatException e) { return -1; }
    }

    // session池，提高响应速度
    Session private_get_session;

    Session getSession() {
        if (private_get_session == null
            || !private_get_session.isConnected()) {
            private_get_session = null;

            int port = 22;

            try {
                JSch jsch = new JSch();
                private_get_session = jsch.getSession(username, ip, port);
                private_get_session.setPassword(password);
                private_get_session.setUserInfo(new UserInfoClass());
                var config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                private_get_session.setConfig(config);
                private_get_session.setTimeout(2000);
                private_get_session.connect();
            } catch (JSchException e) {
                return null;
            }
        }
        return private_get_session;
    }

    String execGetResult(Session session, String cmd) {
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
