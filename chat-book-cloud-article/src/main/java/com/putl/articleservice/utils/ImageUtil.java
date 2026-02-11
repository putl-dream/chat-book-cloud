package com.putl.articleservice.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;

public class ImageUtil {

    /**
     * 生成包含文字的图片
     *
     * @param text       要生成的文字
     * @param width      图片宽度
     * @param height     图片高度
     * @param outputPath 生成的图片保存路径
     * @throws IOException 如果保存图片时发生错误
     */
    private static void generateImageFromText(String text, int width, int height, String outputPath) throws IOException{
        // 创建一个 BufferedImage 对象，支持透明度
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // 设置随机背景颜色
        Color backgroundColor = generateRandomColor();
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);

        // 设置文字颜色和字体
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("宋体", Font.BOLD, 150));

        // 计算文字的宽度和高度
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        int textHeight = g2d.getFontMetrics().getHeight();

        // 计算文字的位置以使其居中
        int x = (width - textWidth) / 2;
        int y = (height - textHeight) / 2 + g2d.getFontMetrics().getAscent();

        // 绘制文字
        g2d.drawString(text, x, y);

        // 释放资源
        g2d.dispose();

        // 保存图片
        File outputFile = new File(outputPath);
        ImageIO.write(image, "png", outputFile);
    }

    // 生成随机颜色的方法
    private static Color generateRandomColor(){
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        int alpha = (int) (0.1 * 255); // 透明度为0.1
        return new Color(r, g, b, alpha);
    }

    public static String getImageByText(String text){
        // 使用相对路径
        String ImgName = UUID.randomUUID() + ".png";
        String relativePath = "upload/images/" + ImgName;
        Path path = Paths.get(relativePath);
        try {
            Files.createDirectories(path.getParent()); // 确保目录存在
            String outputPath = path.toAbsolutePath().toString();
            generateImageFromText(text, 1100, 720, outputPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baseUrl + "/images/" + ImgName;
    }

    private static String baseUrl = "http://localhost:8080";

    public static void setBaseUrl(String baseUrl) {
        ImageUtil.baseUrl = baseUrl;
    }

    public static void main(String[] args){
        String text = "Hello, World!123";
        String outputPath = getImageByText(text);
        System.out.println("图片已生成: " + outputPath);
    }
}
