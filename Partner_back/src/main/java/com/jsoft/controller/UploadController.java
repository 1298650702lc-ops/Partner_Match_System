package com.jsoft.controller;

import com.jsoft.Common.BaseResponse;
import com.jsoft.Common.ErrorCode;
import com.jsoft.Common.ResultUtil;
import com.jsoft.utils.AliyunOSSOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.UUID;

@Slf4j
@RestController
public class UploadController {
    @Resource
    private AliyunOSSOperator aliyunOSSOperator;
    @PostMapping("/upload")
    public BaseResponse<String> upload(MultipartFile file) throws Exception {
        log.info("上传文件：{}", file);
        if (!file.isEmpty()) {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + extName;
            // 上传文件
            String url = aliyunOSSOperator.upload(file.getBytes(), uniqueFileName);
            return ResultUtil.success(url);
        }
        return ResultUtil.error(ErrorCode.valueOf("上传失败"));
    }
}
