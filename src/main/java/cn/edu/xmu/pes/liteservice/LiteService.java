package cn.edu.xmu.pes.liteservice;

import cn.edu.xmu.pes.liteservice.models.*;
import cn.edu.xmu.pes.liteservice.req_models.*;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

// TODO validate applcation.properties

@Service
public class LiteService {
    Logger logger = LoggerFactory.getLogger(LiteService.class);

    @Value("${admin.pesroot}")
    String rawroot;
    String masterPesroot;
    String slavePesroot;

    @Value("${admin.key}")
    String key;

    LiteSSHCaller ssh;

    public LiteService(@Autowired LiteSSHCaller ssh) {
        this.ssh = ssh;
    }

    /**
     *
     */
    @PostConstruct
    public void postConstruct() {

        // Linux程序，在Windows下兼容调试
        if ( rawroot.startsWith("~")
                && System.getProperty("os.name").toLowerCase().startsWith("win")) {
            masterPesroot = new File(
                    System.getProperty("user.home") + rawroot.substring(1)
            ).getAbsolutePath();
        } else {
            masterPesroot = new File(rawroot).getAbsolutePath();
        }
        slavePesroot = rawroot;

        // 重复创建保证根文件夹存在
        var dir = new File(masterPesroot);
        dir.mkdirs();

    }


    /**
     *
     */
    public void newtask(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        var model = JSON.parseObject(new String(request.getInputStream().readAllBytes()), NewTaskModel.class);
        String groupname = model.groupname;
        String mallGateway = model.mallGateway;
        String adminGateway = model.adminGateway;
        if (groupname == null
                || !HttpParaValidator.checkIpport(mallGateway)
                || !HttpParaValidator.checkIpport(adminGateway))  {
            serveError(response, 403, "没有设置组名(Groupname)或者地址格式不对.");
            return;
        }

        String slaveTaskDirPath  = getSlaveBase64Path( groupname);
        String masterTaskDirPath = getMasterBase64Path(groupname);

        File dir = new File(masterTaskDirPath);
        if (dir.exists()) {
            serveError(response, 404, "这个命名下已经有一个测试任务了");
            return;
        }

        // 主服务器先建一个文件夹
        new File(masterTaskDirPath).mkdirs();

        if (!ssh.runNew(slaveTaskDirPath, mallGateway, adminGateway)) {
            serveError(response, 503, "任务异常终止");
            return;
        }
        serveReplyObject(response, new InfoMsg("执行成功，请记住您的组名"));
    }


    /**
     *
     */
    public void progress(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        var model = JSON.parseObject(new String(request.getInputStream().readAllBytes()), ProgressModel.class);
        String groupname = model.groupname;
        if (groupname == null)  {
            serveError(response, 403, "没有设置组名.");
            return;
        }

        String foldername = getMasterBase64Path(groupname);

        File folderinfo =  new File(foldername);
        if (!folderinfo.exists() || !folderinfo.isDirectory()) {
            serveReplyObject(response, new ProgressMsg(0, 0));
            return;
        }

        List<String> filepathList = getFiles(foldername);
        int filenum = (filepathList == null) ? 0 : filepathList.size();

        serveReplyObject(response, new ProgressMsg(1, filenum));
    }

    /**
     *
     */
    public void runningnumber(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        int num = ssh.getRunningPrcessNumber();
        if (num == -1) {
            serveError(response, 503, "服务器错误");
            return;
        }
        serveReplyObject(response, new RunningNumberMsg(num));
    }


    /**
     *
     */
    public void killall(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        var model = JSON.parseObject(new String(request.getInputStream().readAllBytes()), KillallModel.class);
        String key = model.key;

        if (!this.key.equals(key)) {
            serveError(response, 403, "管理员Key错误");
            return;
        }

        if (!ssh.killall()) {
            serveError(response, 503, "服务器错误，无法在SSH中杀死进程");
            return;
        }
        serveReplyObject(response, new InfoMsg("成功杀死所有进程"));
    }


    /**
     *
     */
    public void report(HttpServletRequest request, HttpServletResponse response) throws Throwable {

        String str_fileindex = request.getParameter("file");
        String groupname = request.getParameter("groupname");
        String online = request.getParameter("online");
        if (str_fileindex == null || groupname == null)  {
            serveError(response, 403, "找不到所指定的文件");
            return;
        }

        int fileindex = 0;
        try {
            fileindex = Integer.parseInt(str_fileindex);
        } catch (Exception e) {
            serveError(response, 403, "找不到所指定的文件");
            return;
        }

        String foldername = getMasterBase64Path(groupname);
        List<String> filepathList = getFiles(foldername);
        if (filepathList == null || fileindex < 0 || fileindex >= filepathList.size()) {
            serveError(response, 403, "找不到所指定的文件");
            return;
        }

        byte[] content = null;
        try {
            content = getFileContent(filepathList.get(fileindex));
        } catch (Exception e) { }
        if (content == null) {
            serveError(response, 403, "指定的文件内容为空");
            return;
        }

        response.setStatus(200);
        if ("yes".equals(online)) {
            response.setContentType("text/plain;charset=utf-8");
        } else {
            response.setContentType("application/download");
            response.setHeader("content-disposition", "attachment;fileName="
                    + URLEncoder.encode("report_" + fileindex + ".txt", StandardCharsets.UTF_8));
            response.setHeader("Content-Transfer-Encoding","binary");
        }

        response.getOutputStream().write(content);

    }


    /**
     *
     */
    public void serveStaticHtml(HttpServletRequest request, HttpServletResponse response, String filename) throws Throwable {
        byte[] content = getFileContent("statics/"+filename);
        if (content == null) {
            serveError(response, 403, "找不到所指定的文件");
            return;
        }
        response.setStatus(200);
        response.setContentType("text/html;charset=utf-8");
        response.getOutputStream().write(content);
    }



    byte[] getFileContent(String filepath) {
        File f = new File(filepath);
        byte[] s = null;
        try {
            s= new FileInputStream(f).readAllBytes();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return s;
    }

    List<String> getFiles(String path) {
        List<String> files = new ArrayList<String>();
        File file = new File(path);
        File[] tempList = file.listFiles();

        if (tempList == null) return null;
        for (File value : tempList) {
            if (value.isFile()) {
                files.add(value.toString());
            }
        }
        return files;
    }

    String getSlaveBase64Path(String s) {
        String f = Base64.getEncoder().encodeToString(s.getBytes());
        if (f.length() > 40) f = f.substring(0, 40);
        f.replace('+', '_');
        f.replace('/', '_');
        return slavePesroot +"/"+f;
    }

    String getMasterBase64Path(String s) {
        String f = Base64.getEncoder().encodeToString(s.getBytes());
        if (f.length() > 40) f = f.substring(0, 40);
        f.replace('+', '_');
        f.replace('/', '_');
        return masterPesroot +"/"+f;
    }

    void serveError(HttpServletResponse response, int status, String reply) throws Throwable {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(status);
        response.getWriter().write( JSON.toJSONString(new ErrorMsg(reply)) );
    }

    void serveReplyObject(HttpServletResponse response, Object object) throws Throwable {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(200);
        response.getWriter().write(JSON.toJSONString(object));
    }
}
