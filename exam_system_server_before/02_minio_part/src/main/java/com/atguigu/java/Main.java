package com.atguigu.java;

import io.minio.*;
import io.minio.errors.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String endpoint = "http://192.168.100.104:9000";
        String accessKey = "minioadmin";
        String secretKey = "minioadmin";
        String bucketName = "mybucket";
        //1.登录端点
        //构建者模式，就是一种结构可以在创建对象的同时传入必要的参数
        MinioClient minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
        //2.检查桶是否存在
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        //3.不存在就创建桶，并设置访问权限
        if(!bucketExists) {
            //创建一个通
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            //设置hello-minio桶的访问权限
            String policy = """
                        {
                          "Statement" : [ {
                            "Action" : "s3:GetObject",
                            "Effect" : "Allow",
                            "Principal" : "*",
                            "Resource" : "arn:aws:s3:::%s/*"
                          } ],
                          "Version" : "2012-10-17"
                        }""".formatted(bucketName);
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().config(policy).bucket(bucketName).build());
        }
        //4.上传文件对象
        minioClient.uploadObject(UploadObjectArgs.builder()
                        .bucket(bucketName)
                        .object("p1.jpg")
                        .filename("C:\\Users\\eeleg\\Desktop\\imgs\\p1.jpg")
                        .contentType("image/jpeg")
                .build());//要求传递文件在本地磁盘中
        //minioClient.putObject();//传递文件数据已经读取到当前方法中 byte[]
        //5.拼接访问地址
        String url=endpoint+"/"+bucketName+"/p1.jpg";
        System.out.println(url);

    }
}