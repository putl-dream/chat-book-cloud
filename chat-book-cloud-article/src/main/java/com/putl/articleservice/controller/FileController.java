package com.putl.articleservice.controller;

import com.putl.articleservice.utils.ImageResult;
import fun.amireux.chat.book.minio.utils.MinioUpdateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Tag(name = "文件上传接口")
@RestController
@RequestMapping("/article/file")
@RequiredArgsConstructor
public class FileController {

    private final MinioUpdateUtil minioUpdateUtil;

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
            String directory = DIRECTORY_MAP.getOrDefault(contentType, "others/");
            if (!directory.endsWith("/")) {
                directory += "/";
            }

            String fileName = UUID.randomUUID() + "." + contentType;
            String objectName = directory + fileName;

            // 上传到MinIO
            String resultPath = minioUpdateUtil.uploadFile(file, objectName);
            
            if (resultPath == null) {
                return ImageResult.error("文件上传失败");
            }

            // 获取访问地址
            String publicUrl = minioUpdateUtil.getPublicFileUrl(resultPath);
            if (publicUrl == null) {
                 // 如果未配置公共URL，则使用预签名URL（有效期24小时）
                 publicUrl = minioUpdateUtil.getFileUrl(resultPath, 24);
            }

            log.info("文件保存成功: {}", publicUrl);
            return ImageResult.success(new Img(publicUrl, file.getOriginalFilename(), null));
        } catch (Exception e) {
            log.error("文件上传过程中发生异常", e);
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
            // 简单的防空判断
            if (contentType != null) {
                String[] split = contentType.split("/");
                contentType = split[split.length - 1];
            } else {
                contentType = "unknown";
            }
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
