//package fun.amireux.chat.book.gateway.config;
//
//import com.alibaba.cloud.nacos.annotation.NacosConfig;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class ConfigController {
//    @Value("${plainKey}")
//    String testKey;
//    @NacosConfig(dataId = "routeconfig", group = "config", key = "rate")
//    String rate;
//
//    @RequestMapping("/testPlainKey")
//    public String test() {
//        return testKey;
//    }
//
//    @RequestMapping("/rate")
//    public String rate() {
//        return rate;
//    }
//}