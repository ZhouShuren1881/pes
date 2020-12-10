package cn.edu.xmu.pes.controller;

import cn.edu.xmu.pes.liteservice.Lite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// ----------------------------------------------------------------
//java -jar参数携带问题
//方式一
//
//        -DpropName=propValue的形式携带，要放在-jar参数前面，亲测，放在它后面好像取不到值
//
//        java -DprocessType=1 -jar dataProcess.jar
//        System.getProperty("processType")用来取值
// * java -cp   jarname         classname
// * java -cp   .\Goose-1.0.jar Goose
// ----------------------------------------------------------------

@RestController
public class Major {

    Lite lite;

    public Major(@Autowired Lite lite) {
        this.lite = lite;
    }

    @PostMapping("/ajax/newtask")
    public void newtask(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.newtask(request, response);
    }

    @PostMapping("/ajax/progress")
    public void progress(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.progress(request, response);
    }

    @PostMapping("/ajax/runningnumber")
    public void runningnumber(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.runningnumber(request, response);
    }

    @PostMapping("/ajax/killall")
    public void killall(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.killall(request, response);
    }

    @RequestMapping("/")
    public void root(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.serveStaticHtml(request, response, "index.html");
    }

    @RequestMapping("/superadmin")
    public void superadmin(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.serveStaticHtml(request, response, "pannel.html");
    }

    @RequestMapping("/report") // ?file=1 (使用遍历文件夹，然后在map中查找值);?groupname=...;?online=...
    public void report(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.report(request, response);
    }

}
