package com.putl.articleservice.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlUtil {

    //从html中获取文本
    public static String getTextFromHtml(String html) {
        if (html == null) {
            return null;
        }
        return html.replaceAll("<[^>]+>", "");
    }

    public static String getLen200ByHtml(String html) {
        if (html == null) {
            return null;
        }
        String string = html.replaceAll("<[^>]+>", "");
        return string.length() > 200 ? string.substring(0, 200) : string;
    }


    public static String getFirstImg(String html) {
        if (html == null) {
            return null;
        }
        // 使用 Jsoup 解析 HTML
        Document doc = Jsoup.parse(html);
        // 选择所有的 <img> 标签
        Elements imgElements = doc.select("img");
        // 检查是否有图片
        if (imgElements.isEmpty()) {
            return null;
        }
        // 获取第一个 <img> 标签的 src 属性
        Element firstImg = imgElements.first();
        if (firstImg == null) {
            return null;
        }
        return firstImg.attr("src");
    }

    public static void main(String[] args) {
        String html = "<html><body><img src='https://example.com/image1.jpg'><img src='https://example.com/image2.jpg'></body></html>";
        String firstImgUrl = getFirstImg(html);
        System.out.println("First Image URL: " + firstImgUrl);
    }
}
