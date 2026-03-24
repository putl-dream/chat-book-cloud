package fun.amireux.chat.book.gateway.filters;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;

public class DeviceUtil {

    private DeviceUtil() {
    }

    /**
     * 获取请求设备信息
     * @param userAgent User-Agent
     * @return 设备信息 pc端还是手机端
     */
    public static String getDeviceInfo(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知";
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("android") || ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod") || ua.contains("mobile")) {
            return "移动端";
        }
        return "PC端";
    }

    /**
     * 解析用户代理详细信息
     * @param userAgent User-Agent
     * @return 设备类型:%s,操作系统:%s,浏览器:%s
     */
    public static String getDeviceDetail(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知设备";
        }

        UserAgent ua = UserAgent.parseUserAgentString(userAgent);
        Browser browser = ua.getBrowser();
        OperatingSystem os = ua.getOperatingSystem();

        return String.format("设备类型:%s,操作系统:%s,浏览器:%s",
                os.getDeviceType(),
                os.getName(),
                browser.getName());
    }
}
