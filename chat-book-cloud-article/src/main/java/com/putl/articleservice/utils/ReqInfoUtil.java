package com.putl.articleservice.utils;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReqInfoUtil {

    /**
     * 解析 用户代理(User-Agent)
     * @param userAgent 用户代理User-Agent ,UA
     * @return "设备类型:%s,操作系统:%s,浏览器:%s,浏览器版本:%s,浏览器引擎:%s,用户代理(User-Agent):[%s]"
     * @author GongLiHai
     */
    public static String getDevice(String userAgent) {
        //解析agent字符串
        UserAgent ua = UserAgent.parseUserAgentString(userAgent);
        //获取浏览器对象
        Browser browser = ua.getBrowser();
        //获取操作系统对象
        OperatingSystem os = ua.getOperatingSystem();
        return String.format("设备类型:%s,操作系统:%s,浏览器:%s,浏览器版本:%s,浏览器引擎:%s,用户代理(User-Agent):[%s]",
                os.getDeviceType(),
                os.getName(),
                browser.getName(),
                browser.getVersion(userAgent),
                browser.getRenderingEngine(),
                userAgent
        );
    }

    /**
     * 获取请求设备信息
     * @author gaodongyang
     * @param request 请求
     * @return String 设备信息 pc端还是手机端
     **/
    public static String getDeviceInfo(HttpServletRequest request) {
        ///定义正则
        String pattern = "^Mozilla/\\d\\.\\d\\s+\\(+.+?\\)";
        String pattern2 = "\\(+.+?\\)";
        ///将给定的正则表达式编译到模式中
        Pattern r = Pattern.compile(pattern);
        Pattern r2 = Pattern.compile(pattern2);

        String userAgent = request.getHeader("User-Agent");
        ///创建匹配给定输入与此模式的匹配器
        Matcher m = r.matcher(userAgent);
        String result = null;
        if (m.find()) {
            result = m.group(0);
        }
        if(result == null){
            return null;
        }
        Matcher m2 = r2.matcher(result);
        if (m2.find()) {
            result = m2.group(0);
        }
        result = result.replace("(", "");
        result = result.replace(")", "");

        if (StringUtils.isBlank(result)) {
            return null;
        }
        result = result.replace(" U;", "");
        result = result.replace(" zh-cn;", "");

        String android = "Android";
        String iPhone = "iPhone";
        String iPad = "iPad";
        if(result.contains(android) || result.contains(iPhone) || result.contains(iPad)){
            return "移动端";
        }else{
            return "PC端";
        }
    }
}
