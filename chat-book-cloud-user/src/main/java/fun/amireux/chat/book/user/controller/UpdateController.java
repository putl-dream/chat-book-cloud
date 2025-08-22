package fun.amireux.chat.book.user.controller;

import fun.amireux.chat.book.minio.pojo.FileInfo;
import fun.amireux.chat.book.minio.utils.MinioUpdateUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UpdateController {
    private final MinioUpdateUtil minioUpdateUtil;

    @PostMapping("/updateFile")
    public String updateFile(
            @RequestParam("fileMd5") String fileMd5,
            @RequestParam("fileName") String fileName,
            @RequestParam("file") MultipartFile file
    ) {
        return minioUpdateUtil.uploadFile(new FileInfo(fileMd5, file), "001", fileName);
    }


    @GetMapping("get")
    public String getFile(@RequestParam("addr") String addr) {
        return minioUpdateUtil.getFileUrl(addr, 1);
    }

    @GetMapping("/download")
    public void downloadFile(@RequestParam("addr") String addr, HttpServletResponse response) {
        minioUpdateUtil.downloadFile(addr, response);
    }
}
