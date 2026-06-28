package com.atguigu.java.springboot_01.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class test {

    @GetMapping("/hello")
    public String hello(){
        return "hello ";
    }

}
