package com.messageboard.controller;

import com.messageboard.common.Result;
import com.messageboard.config.FileUploadConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Resource
    private FileUploadConfig fileUploadConfig;

    /**
     * 上传附件（支持多文件）
     */
    @PostMapping("/upload")
    public Result<List<Map<String, String>>> upload(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("请选择文件");
        }

        // 解析允许的文件类型
        Set<String> allowSet = new HashSet<>();
        for (String type : fileUploadConfig.getAllowTypes().split(",")) {
            allowSet.add(type.trim().toLowerCase());
        }

        // 按日期分目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        File uploadDir = new File(fileUploadConfig.getPath(), dateDir);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        List<Map<String, String>> resultList = new ArrayList<>();
        for (MultipartFile file : files) {
            // 校验文件类型
            String originalName = file.getOriginalFilename();
            String suffix = "";
            if (originalName != null && originalName.contains(".")) {
                suffix = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
            }
            if (!allowSet.contains(suffix)) {
                return Result.error("不支持的文件类型: " + suffix + "，允许的类型: " + fileUploadConfig.getAllowTypes());
            }

            // 生成唯一文件名
            String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + suffix;
            File destFile = new File(uploadDir, newFileName);

            try {
                file.transferTo(destFile);
                Map<String, String> fileInfo = new HashMap<>();
                // 返回相对路径（按日期/文件名）
                fileInfo.put("path", dateDir + "/" + newFileName);
                fileInfo.put("name", originalName);
                resultList.add(fileInfo);
                log.info("文件上传成功: {} -> {}", originalName, destFile.getAbsolutePath());
            } catch (IOException e) {
                log.error("文件上传失败: {}", originalName, e);
                return Result.error("文件上传失败: " + originalName);
            }
        }

        return Result.success(resultList);
    }
}
