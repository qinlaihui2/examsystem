package com.atguigu.exam.service;


import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 文件上传服务
 * 支持MinIO和本地文件存储两种方式
 */

public interface FileUploadService {

    /**
     * FileUploadService
     *
     * @author 李嘉宇
     * @date 2025/10/31 13:13
     * @param folder 在minio中存储的文件夹（轮播图：banners 视频：videos)
     * @param file 上传的文件
     * @return 返回的回显地址
     * @version 1.0
     * @description 文件上传业务方法
     */
    String uploadFile(String folder,MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
} 