package com.messageboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置
 */
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    /** 附件上传目录 */
    private String path = "./uploads";

    /** 单个文件最大大小 */
    private String maxSize = "10MB";

    /** 允许的文件类型 */
    private String allowTypes = "jpg,jpeg,png,gif,bmp,pdf,doc,docx,xls,xlsx,ppt,pptx,txt,zip,rar";

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getMaxSize() { return maxSize; }
    public void setMaxSize(String maxSize) { this.maxSize = maxSize; }

    public String getAllowTypes() { return allowTypes; }
    public void setAllowTypes(String allowTypes) { this.allowTypes = allowTypes; }
}
