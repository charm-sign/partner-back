package com.zf.partnerback.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.zf.partnerback.common.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.UEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: FileController
 * @Description: 文件处理接口
 * @Author: ZF
 * @date: 2023/1/18 17:17
 */
@RestController
@Slf4j
@RequestMapping("/file")
public class FileController {
    @Value("${file.upload.path:}") //:表示为空时不报错
    private String uploadPath;
    private static final String FILE_DIR = "/files/";
    @Value("${server.port}")
    private String port;//端口
    @Value("${file.download.ip}")
    private String ip;//端口

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result upload(MultipartFile file) {

        String uniqueFlag = IdUtil.fastSimpleUUID();
        String suffix = FileUtil.extName(file.getOriginalFilename());//得到文件后缀名 不带.
        String fileFullName = uniqueFlag + StrUtil.DOT + suffix;
        String fileUploadPath = getFilePath(fileFullName);//光是目录不能输出，需要具体文件 12664.jpg  此处注意不能直接赋值给uploadPath，会一直增加导致越来越长
        try {
            File uploadFile = new File(fileUploadPath);
            File parentFile = uploadFile.getParentFile();
            if (!parentFile.exists()) { //如果files目录不存在则创建
                parentFile.mkdirs();
            }
            file.transferTo(uploadFile);
        } catch (IOException e) {
            log.info("文件上传失败", e);
            return Result.error("文件上传失败");
        }
        String url = "http://" + ip + ":" + port + "/file/download/" + fileFullName;//返回下载接口
        return Result.success(url);//要返回一个文件地址，供前端调用
    }

    @ApiOperation("文件下载")
    @GetMapping("/download/{fileFullName}")
    public void download(@PathVariable String fileFullName, HttpServletResponse response,
                         @RequestParam(required = false) String loginId,
                         @RequestParam(required = false) String token) throws IOException {
//手动校验token
        List<String> tokenList = StpUtil.getTokenValueListByLoginId(loginId);
        if (CollUtil.isEmpty(tokenList) || !tokenList.contains(token)) {
            return;
        }
        String downloadPath = getFilePath(fileFullName);//获取完整路径
        String extName = FileUtil.extName(fileFullName);//获取后缀，方便后面判断是否以附件展示
        byte[] bytes = FileUtil.readBytes(downloadPath);//根据文件路径读取
        response.addHeader("Content-Disposition", "inline;fileName=" + URLEncoder.encode((fileFullName), "UTF-8"));//inline 预览（图片）
        List<String> list = CollUtil.newArrayList("docx", "doc", "xlsx", "xls", "mp3", "mp4");
        if (list.contains(extName)) {//要以附件形式展示
            response.addHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode((fileFullName), "UTF-8"));//attachment 附件（文档）
        }
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(bytes);//将读取的输出
        outputStream.flush();
        outputStream.close();
    }

    //获取完整路径方法
    private String getFilePath(String fileFullName) {
        if (StrUtil.isBlank(uploadPath)) {
            uploadPath = System.getProperty("user.dir");
        }
        return uploadPath + FILE_DIR + fileFullName;
    }
}
