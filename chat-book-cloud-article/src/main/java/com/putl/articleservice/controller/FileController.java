package com.putl.articleservice.controller;

import com.putl.articleservice.utils.ImageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Tag(name = "文件上传接口")
@RestController
@RequestMapping("/article/file")
public class FileController {

    @Value("${file.storage.base-url}")
    private String fileBaseUrl;

    private static final String BASE_UPLOAD_DIR = "upload/";
    private static final Map<String, String> DIRECTORY_MAP = new HashMap<>();

    static{
        // 图片类型
        DIRECTORY_MAP.put("jpg", "images/");
        DIRECTORY_MAP.put("jpeg", "images/");
        DIRECTORY_MAP.put("png", "images/");
        DIRECTORY_MAP.put("gif", "images/");
        // 视频类型
        DIRECTORY_MAP.put("mp4", "videos/");
        DIRECTORY_MAP.put("mov", "videos/");
        DIRECTORY_MAP.put("avi", "videos/");
    }

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public ImageResult<Img> uploadFile(@RequestParam("file") MultipartFile file){
        log.info("开始上传文件: {}", file.getOriginalFilename());
        if (file.isEmpty()) return ImageResult.error("请上传文件");

        try {
            //查看文件类型
            String contentType = getTypeByFile(file);
            log.info("文件类型new: {}", contentType);
            String directory = DIRECTORY_MAP.getOrDefault(contentType, "others");

            if (directory == null || directory.isEmpty()) {
                return ImageResult.error("不支持的文件类型");
            }

            Path savePath = Paths.get(BASE_UPLOAD_DIR, directory);
            File uploadDir = savePath.toFile();
            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                return ImageResult.error("创建上传目录失败");
            }

            // 创建文件
            byte[] bytes = file.getBytes();
            String fileName = UUID.randomUUID() + "." + contentType;
            Path filePath = savePath.resolve(fileName);
            Files.write(filePath, bytes);

            // 返回文件路径
            String str = fileBaseUrl + "/" + directory + fileName;
            log.info("文件保存成功: {}", str);
            return ImageResult.success(new Img(str, file.getOriginalFilename(), null));
        } catch (IOException e) {
            log.error("文件上传过程中发生IO异常", e);
            return ImageResult.error("文件上传失败: " + e.getMessage());
        }
    }

    private static String getTypeByFile(MultipartFile file){
        //获取文件类型
        String contentType = file.getContentType();

        //查看文件的名字
        String filename = file.getOriginalFilename();

        if (contentType == null && filename != null) {
            String[] split = filename.split("\\.");
            contentType = split[split.length - 1];
        } else {
            assert contentType != null;
            String[] split = contentType.split("/");
            contentType = split[split.length - 1];
        }
        return contentType;
    }


    @Data
    @AllArgsConstructor
    public static class Img {
        private String url;
        private String alt;
        private String href;
    }
}