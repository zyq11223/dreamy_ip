package com.dreamy.service.impl.upload;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.dreamy.beans.InterfaceBean;
import com.dreamy.beans.params.ImageUploadParams;
import com.dreamy.enums.ErrorCodeEnums;
import com.dreamy.service.iface.upload.ImgUploadService;
import com.dreamy.utils.StringUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: yujianfu (yujianfu@duotin.com)
 * Date: 16/5/10
 * Time: 下午5:29
 */
@Controller
public class ImgUploadServiceImpl implements ImgUploadService {

    private static final Logger LOG = LoggerFactory.getLogger(ImgUploadServiceImpl.class);

    @Override
    public OSSClient getOssClient() {
        String endpoint = "oss-cn-hangzhou.aliyuncs.com";
        String accessKeyId = "F5ySitlEtTLdJ3pp";
        String accessKeySecret = "wmF80JSkM1pe6jhh3xsKfl6XyPaktP";

        ClientConfiguration conf = new ClientConfiguration();
        conf.setMaxConnections(100);
        conf.setConnectionTimeout(5000);
        conf.setMaxErrorRetry(3);
        conf.setSocketTimeout(2000);

        return new OSSClient(endpoint, accessKeyId, accessKeySecret, conf);
    }

    @Override
    public String uploadImage(MultipartFile upfile) {
        String bucketName = "ipcool";

        OSSClient client = getOssClient();

        String tmpFileName = UUID.randomUUID().toString() + upfile.getOriginalFilename().substring(upfile.getOriginalFilename().lastIndexOf(".")).toLowerCase();
        String filePath = "Users/mac/dreamy_ip/uploads/temp/";

        File f = new File(filePath);
        File tmp = new File(f, tmpFileName);
        try {
            if(!f.exists()) {
                f.mkdirs();
            }
            if(!tmp.exists()) {
                tmp.createNewFile();
            }
            IOUtils.copy(upfile.getInputStream(), new FileOutputStream(tmp));
            client.putObject(new PutObjectRequest(bucketName, tmpFileName, tmp));
        } catch (IOException e) {
            LOG.error("upload image to oss failed", e);
        } finally {
            client.shutdown();
            tmp.delete();
            f.delete();
        }

        return bucketName + ".oss-cn-hangzhou.aliyuncs.com" + "/" + tmpFileName;
    }

    @Override
    public InterfaceBean propertyCheck(Integer width, Integer height, Integer size, MultipartFile upfile) {
        InterfaceBean bean = new InterfaceBean().success();
        if (upfile == null) {
            bean.failure(ErrorCodeEnums.image_upload_failed.getErrorCode(), "图片文件不存在");
        } else {
            String imageType = upfile.getOriginalFilename().substring(upfile.getOriginalFilename().lastIndexOf(".") + 1).toLowerCase();
            if (!imageType.equals("jpg") && !imageType.equals("png")) {
                bean.failure(ErrorCodeEnums.image_upload_failed.getErrorCode(), "图片支持jpg和png格式");
            } else {
                long fileSize = upfile.getSize();
                if (fileSize > 1024 * 1024 * 10) {
                    bean.failure(ErrorCodeEnums.image_upload_failed.getErrorCode(), "图片大小不能超过10M");
                }
            }
        }

        return bean;
    }
}
