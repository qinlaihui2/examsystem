package com.atguigu.exam.service.impl;

import com.atguigu.exam.entity.Banner;
import com.atguigu.exam.mapper.BannerMapper;
import com.atguigu.exam.service.BannerService;

import com.atguigu.exam.service.FileUploadService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.minio.errors.*;
import io.netty.util.internal.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图服务实现类
 * 该类实现了BannerService接口，提供轮播图相关的业务功能
 * 主要包括轮播图图片的上传功能
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Autowired
    private FileUploadService fileUploadService;

    @Override
    public String uploadBannerImage(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        /**
         *   1.文件非空校验
         *   2.文件类型 image
         *   3.调用文件上传业务
         *   4.回显地址
         */
        if(file.isEmpty()){
            throw new RuntimeException("上传的文件对象为空，上传失败");
        }
        String contentType = file.getContentType();
        if(ObjectUtils.isEmpty(contentType) || !contentType.startsWith("image")){
            throw new RuntimeException("上传的文件类型错误，上传失败");
        }
        String url = fileUploadService.uploadFile("banners", file);
        return url;
    }
}