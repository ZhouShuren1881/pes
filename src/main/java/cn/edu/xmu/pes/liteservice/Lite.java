package cn.edu.xmu.pes.liteservice;

import cn.edu.xmu.pes.liteservice.models.*;
import cn.edu.xmu.pes.liteservice.models.ProgressMsg;
import cn.edu.xmu.pes.litessh.LiteSSHCaller;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class Lite {
    @Value("${admin.fileroot}")
    String unsolvedroot;
    String pesroot;

    @Value("${admin.host.process.bashname}")
    String bashname;

    /**
     *
     */
    @PostConstruct
    public void postConstruct() {
        if ( unsolvedroot.startsWith("~")
                && System.getProperty("os.name").
                toLowerCase().startsWith("win")) {
            pesroot = new File(
                    System.getProperty("user.home") + unsolvedroot.substring(1)
            ).getAbsolutePath();
        } else {
            pesroot = new File( unsolvedroot ).getAbsolutePath();
        }

        // 保证根文件夹存在
        var dir = new File(pesroot);
        dir.mkdirs();
    }


    /**
     *
     */
    public void newtask(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        String groupname = request.getParameter("groupname");
        String mallGateway = request.getParameter("mallGateway");
        String adminGateway = request.getParameter("adminGateway");
        if (groupname == null
                || !HttpParaValidator.checkIpport(mallGateway)
                || !HttpParaValidator.checkIpport(adminGateway))  {
            serveReplyObject(response, new ErrorMsg("没有设置组名(Groupname)或者地址格式不对."));
            return;
        }

        String foldername = getDirectionNameWithBase64(groupname);

        File folderinfo = new File(foldername);
        if (folderinfo.exists()) {
            serveReplyObject(response, new ErrorMsg("这个命名下已经有一个测试任务了"));
            return;
        }

        LiteSSHCaller.runNew(foldername, mallGateway, adminGateway);
        serveReplyObject(response, new InfoMsg("执行成功，请记住您的Key"));
    }

    /**
     *
     */
    public void progress(HttpServletRequest request, HttpServletResponse response) throws Throwable {

        String groupname = request.getParameter("groupname");
        if (groupname == null)  {
            serveReplyObject(response, new ErrorMsg("没有设置组名(Groupname)."));
            return;
        }

        String foldername = getDirectionNameWithBase64(groupname);

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
        int num = LiteSSHCaller.getRunningPrcessNumber();
        if (num == -1) {
            serveReplyObject(response, new ErrorMsg("服务器错误"));
            return;
        }
        serveReplyObject(response, new RunningNumberModel(num));
    }


    /**
     *
     */
    public void killall(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        String key = request.getParameter("key");

        if (!bashname.equals(key)) {
            serveReplyObject(response, new ErrorMsg("Key错误"));
            return;
        }

        if (!LiteSSHCaller.killall()) {
            serveReplyObject(response, new ErrorMsg("服务器错误，无法在SSH中杀死进程"));
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
            serve404(response);
            return;
        }

        int fileindex = Integer.parseInt(str_fileindex);
        String foldername = getDirectionNameWithBase64(groupname);
        List<String> filepathList = getFiles(foldername);
        if (filepathList == null || fileindex < 0 || fileindex >= filepathList.size()) {
            serve404(response);
            return;
        }

        String content = "";
        try {
            content = getFileContent(filepathList.get(fileindex));
        } catch (Exception e) {
            serve404(response);
        }

        response.setStatus(200);
        response.setContentType("text/plain;charset=utf-8");
        response.setHeader("content-disposition",(online==null?"attachment":"inline")+";fileName="
                + URLEncoder.encode("report_"+fileindex+".txt", StandardCharsets.UTF_8));
        response.getWriter().write( content );

    }


    /**
     *
     */
    public void serveStaticHtml(HttpServletRequest request, HttpServletResponse response, String filename) throws Throwable {
        String s = getFileContent("statics/"+filename);
        if (s == null) {
            serve404(response);
            return;
        }
        response.setStatus(200);
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write(s);
    }



    String getFileContent(String filepath) throws Throwable {
        File f = new File(filepath);
        String s = new String(new FileInputStream(f).readAllBytes());
        return null;
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

    String getDirectionNameWithBase64(String s) {
        String f = Base64.getEncoder().encodeToString(s.getBytes());
        f.replace('+', '-');
        f.replace('/', '.');
        return pesroot+"/"+f;
    }

    void serve404(HttpServletResponse response) {
        response.setStatus(404);
    }

    void serveReply(HttpServletResponse response, String reply) throws Throwable {
        response.setStatus(200);
        response.getWriter().write(reply);
    }

    void serveReplyObject(HttpServletResponse response, Object object) throws Throwable {
        response.setStatus(200);
        response.getWriter().write(JSON.toJSONString(object));
    }
}
