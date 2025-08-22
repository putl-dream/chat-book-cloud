package fun.amireux.chat.book.minio.utils.impl;

import fun.amireux.chat.book.minio.config.MinIOConfigProperties;
import fun.amireux.chat.book.minio.pojo.FileInfo;
import fun.amireux.chat.book.minio.utils.MinioUpdateUtil;
import io.minio.*;
import io.minio.http.Method;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUpdateUtilImpl implements MinioUpdateUtil {

    private final MinIOConfigProperties minIOConfigProperties;
    private final MinioClient minioClient;

    @Override
    public String uploadFile(FileInfo fileInfo, Object userId, String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            fileName = fileInfo.file().getOriginalFilename();
        }
        // 构建存储路径
        String storagePath = "files/" + userId + "/" + fileName;
        // 上传文件
        boolean isSuccess = uploadFile(fileInfo, storagePath);
        return isSuccess ? storagePath : null;
    }

    @Override
    public String getFileUrl(String storagePath, int hours) {
        return selectFileUrl(storagePath, hours);
    }

    @Override
    public void downloadFile(String storagePath, HttpServletResponse response) {
        StatObjectResponse stat = getFileStatus(storagePath);

        // 设置响应头
        response.setContentType(stat.contentType());
        response.setContentLengthLong(stat.size());

        // 处理中文文件名或特殊字符
        String filename = storagePath.substring(storagePath.lastIndexOf("/") + 1);
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);

        // 从 MinIO 获取输入流并写入响应
        streamDownloadFile(storagePath, response);
    }


    @Override
    public String uploadChunkFile(FileInfo chunkInfo, int chunkIndex) {
        // 构建存储路径
        String storagePath = "chunks/" + chunkInfo.fileMd5() + "/" + chunkIndex;
        boolean isSuccess = uploadFile(chunkInfo, storagePath);
        return isSuccess ? storagePath : null;
    }


    private String selectFileUrl(String storagePath, int hours) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket("uploads")
                            .object(storagePath)
                            .expiry(hours, TimeUnit.HOURS) // 设置有效期为 1 小时
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 文件下载
     *
     * @param storagePath 文件存储路径
     * @param response    HttpServletResponse对象
     */
    private void streamDownloadFile(String storagePath, HttpServletResponse response) {
        try (GetObjectResponse objectResponse = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minIOConfigProperties.getBucket())
                        .object(storagePath)
                        .build());
             ServletOutputStream outputStream = response.getOutputStream()) {

            objectResponse.transferTo(outputStream);
        } catch (Exception e) {
            log.error("文件下载失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 获取文件状态
     *
     * @param storagePath 文件存储路径
     */
    private StatObjectResponse getFileStatus(String storagePath) {
        // 1. 获取文件元信息（用于设置响应头）
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minIOConfigProperties.getBucket())
                            .object(storagePath)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 文件上传
     *
     * @param fileInfo    文件信息
     * @param storagePath 文件存储路径
     */
    private boolean uploadFile(FileInfo fileInfo, String storagePath) {
        // 校验文件MD5
        if (isMd5Invalid(fileInfo.file(), fileInfo.fileMd5())) {
            return false;
        }

        // 判断文件是否存在
        if (isFileExists(storagePath)) {
            return false;
        }

        // 上传文件
        return uploadFile(storagePath, fileInfo.file());
    }

    /**
     * 校验文件MD5值是否不匹配
     *
     * @param file        待校验的文件
     * @param expectedMd5 期望的MD5值
     * @return MD5是否不匹配
     */
    private boolean isMd5Invalid(MultipartFile file, String expectedMd5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream inputStream = file.getInputStream();
            DigestInputStream digestInputStream = new DigestInputStream(inputStream, md);

            // 读取文件内容以计算MD5
            byte[] buffer = new byte[8192];
            while (digestInputStream.read(buffer) > 0) ;

            byte[] digest = md.digest();
            StringBuilder md5StringBuilder = new StringBuilder();
            for (byte b : digest) {
                md5StringBuilder.append(String.format("%02x", b));
            }

            String calculatedMd5 = md5StringBuilder.toString();
            return !calculatedMd5.equalsIgnoreCase(expectedMd5);
        } catch (Exception e) {
            log.error("MD5校验失败：{}", e.getMessage(), e);
            return true; // 出错时认为校验失败
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param storagePath 文件存储路径
     */
    private boolean isFileExists(String storagePath) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minIOConfigProperties.getBucket())
                    .object(storagePath)
                    .build()
            );
            log.debug("MinIO中已存在该文件：{}", storagePath);
            return true;
        } catch (Exception e) {
            log.info("MinIO中不存在该文件：{}", storagePath);
            return false;
        }
    }

    private boolean uploadFile(String storagePath, MultipartFile file) {
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(minIOConfigProperties.getBucket())
                    .object(storagePath)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();
            minioClient.putObject(putObjectArgs);
            log.info("上传文件成功：{}", storagePath);
            return true;
        } catch (Exception e) {
            log.error("上传文件失败：{} \n {}", storagePath, e.getMessage(), e);
            return false;
        }
    }
}
