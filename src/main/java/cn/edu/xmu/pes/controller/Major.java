package cn.edu.xmu.pes.controller;

import cn.edu.xmu.pes.liteservice.LiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class Major {

    LiteService lite;

    public Major(@Autowired LiteService liteService) {
        this.lite = liteService;
    }

    @PostMapping("/ajax/newtask")
    public void newtask(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.newtask(request, response);
    }

    @RequestMapping("/ajax/progress")
    public void progress(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.progress(request, response);
    }

    @RequestMapping("/ajax/runningnumber")
    public void runningnumber(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.runningnumber(request, response);
    }

    @RequestMapping("/ajax/killall")
    public void killall(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.killall(request, response);
    }

    @RequestMapping("/")
    public void root(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.serveStaticHtml(request, response, "index.html");
    }

    @RequestMapping("/superadmin")
    public void superadmin(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.serveStaticHtml(request, response, "superadmin.html");
    }

    @RequestMapping("/report.txt") // ?file=1 (使用遍历文件夹，然后在map中查找值);?groupname=...;?online=...
    public void report(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        lite.report(request, response);
    }

}
