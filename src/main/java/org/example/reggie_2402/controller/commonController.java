package org.example.reggie_2402.controller;

import org.example.reggie_2402.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class commonController {
    //将图片放到目录从application.yml的reggie.path读取 注意目录尾部带\
    @Value("${reggie.path}")
    private String basePath;



//    文件上传
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file)
    {
        //重命名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename=UUID.randomUUID().toString()+suffix;

        //将上传的文件保存到指定地址
        File dir=new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            file.transferTo(new File(basePath + newFilename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //返回生成的文件名，页面用它拼接 /common/download 回显图片，保存菜品时也存这个文件名到image 字段

        return R.success(newFilename);
    }
}
