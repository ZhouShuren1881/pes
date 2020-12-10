package cn.edu.xmu.pes.liteservice.models;

import java.util.regex.Pattern;

public class HttpParaValidator {
    // 中文字符和 a-z A-Z 0-9 空格_-
    static String RegEXStr_title = "[A-Za-z0-9_\\-\\u4e00-\\u9fa5]+";
    static Pattern pattern_title = Pattern.compile(RegEXStr_title);

    public static boolean checkTitle(String title) {
        if (title == null || title.length() > 50 || title.length() < 3) return false;
        var m = pattern_title.matcher(title);
        return m.find() && m.start() == 0 && m.end() == title.length();
    }

    // 密码只能使用数字和字母
    static String RegEXStr_password = "[0-9a-zA-Z]+";
    static Pattern pattern_password = Pattern.compile(RegEXStr_password);

    public static boolean checkPassword(String pw) {
        if (pw == null || pw.length() > 20 || pw.length() < 3) return false;
        var m = pattern_password.matcher(pw);
        return m.find() && m.start() == 0 && m.end() == pw.length();
    }

    // 中文字符和 a-z A-Z 0-9 空格_-
    static String RegEXStr_groupname = "[A-Za-z0-9_\\-\\u4e00-\\u9fa5]+";
    static Pattern pattern_groupname = Pattern.compile(RegEXStr_groupname);

    public static boolean checkGroupname(String gn) {
        if (gn == null || gn.length() > 30 || gn.length() < 3) return false;
        var m = pattern_groupname.matcher(gn);
        return m.find() && m.start() == 0 && m.end() == gn.length();
    }

    // ip:port形式的地址
    static String RegEXStr_ipport
            = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d):[1-9][0-9]{3,4}";
    static Pattern pattern_ipport = Pattern.compile(RegEXStr_ipport);

    public static boolean checkIpport(String ipport) {
        if (ipport == null || ipport.length() > 20 || ipport.length() < 3) return false;
        var m = pattern_ipport.matcher(ipport);
        return m.find() && m.start() == 0 && m.end() == ipport.length();
    }
}
