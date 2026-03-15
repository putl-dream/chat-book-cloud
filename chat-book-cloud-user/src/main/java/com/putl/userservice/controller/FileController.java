package com.putl.userservice.controller;

import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.minio.jackson.FileUrlSerializer;
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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.putl.userservice.common.ReqInfoContext;

import java.util.UUID;

@Slf4j
@Tag(name = "用户文件接口")
@RestController
@RequestMapping("/user/file")
@RequiredArgsConstructor
public class FileController {

    private final MinioUpdateUtil minioUpdateUtil;

    @Operation(summary = "上传头像")
    @PostMapping("/avatar/upload")
    public CommonResult<AvatarVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("开始上传头像: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            return CommonResult.error(500, "请上传文件");
        }

        try {
            // 获取文件类型
            String contentType = getTypeByFile(file);
            log.info("头像文件类型: {}", contentType);

            // 验证文件类型
            if (!isImageFile(contentType)) {
                return CommonResult.error(500, "仅支持图片格式：jpg、jpeg、png、gif");
            }

            String fileName = UUID.randomUUID() + "." + contentType;
            String objectName = "avatar/" + fileName;

            // 上传到MinIO
            String resultPath = minioUpdateUtil.uploadFile(file, objectName);

            if (resultPath == null) {
                return CommonResult.error(500, "头像上传失败");
            }

            log.info("头像保存成功: {}", resultPath);
            return CommonResult.success(new AvatarVO(resultPath));
        } catch (Exception e) {
            log.error("头像上传过程中发生异常", e);
            return CommonResult.error(500, "头像上传失败: " + e.getMessage());
        }
    }

    private static String getTypeByFile(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        if (contentType == null && filename != null) {
            String[] split = filename.split("\\.");
            contentType = split[split.length - 1];
        } else {
            if (contentType != null) {
                String[] split = contentType.split("/");
                contentType = split[split.length - 1];
            } else {
                contentType = "unknown";
            }
        }
        return contentType.toLowerCase();
    }

    private static boolean isImageFile(String contentType) {
        return "jpg".equals(contentType) || "jpeg".equals(contentType)
                || "png".equals(contentType) || "gif".equals(contentType);
    }

    @Data
    @AllArgsConstructor
    public static class AvatarVO {
        @JsonSerialize(using = FileUrlSerializer.class)
        private String url;
    }
}
