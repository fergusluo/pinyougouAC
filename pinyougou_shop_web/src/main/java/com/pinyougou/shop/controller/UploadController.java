package com.pinyougou.shop.controller;

import com.pinyougou.utils.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.shop.controller
 * @date 2019-5-27
 */
@RestController
public class UploadController {
    @Value("${FAST_DFS_SERVER}")
    private String FAST_DFS_SERVER;

    @RequestMapping("upload")
    public Result upload(MultipartFile file){
        try {
            //1、获取后缀名
            //原来的文件名
            String oldName = file.getOriginalFilename();
            //后缀名:abc.jpg
            String extName = oldName.substring(oldName.lastIndexOf(".") + 1);
            //2、通过fastDfs上传文件
            FastDFSClient dfsClient = new FastDFSClient("classpath:fdfs_client.conf");
            String fileId = dfsClient.uploadFile(file.getBytes(), extName, null);
            //3、拼接成功后的图片路径并返回
            String url = FAST_DFS_SERVER + fileId;
            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "上传失败！");
    }
}
