package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService {
    private Logger logger= LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file,String path)  {
        //获取文件名  原始文件名
        String fileName= file.getOriginalFilename();
        //获取拓展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf("."));
        //防止上传文件名相同时  前者文件被覆盖  UUID方法 随机码
        String uploadFileName= UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("开始上传文件，上传文件的文件名：{}，上传的路径：{}，新文件：{}",fileName,path,uploadFileName);
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);

        try {

            file.transferTo(targetFile);
            //文件已经成功上传
            //todo 将targetFile上传的FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //已经上传的ftp服务器上
            //todo 上传完以后，删除upload下面的文件
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常 ",e);
            return  null;
        }
        return null;

    }
}
