package com.dreamy.crawler.listen;

import com.alibaba.fastjson.JSONObject;
import com.dreamy.crawler.handler.so.SoHandler;
import com.dreamy.crawler.service.CrawlerService;
import com.dreamy.enums.IndexSourceEnums;
import com.dreamy.mogodb.beans.BookIndexData;
import com.dreamy.mogodb.dao.BookIndexDataDao;
import com.dreamy.utils.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by wangyongxing on 16/5/5.
 */
@Component
public class SoIndexQueueHandler extends AbstractQueueHandler {


    private static final Logger log = LoggerFactory.getLogger(SoIndexQueueHandler.class);

    @Autowired
    private SoHandler soHandler;

    @Autowired
    BookIndexDataDao bookIndexDataDao;

    @Autowired
    private CrawlerService crawlerService;

    @Override
    public void consume(JSONObject jsonObject) {
        //获取类型
        String word = jsonObject.getString("word");
        Integer bookId = jsonObject.getInteger("bookId");
        String key = jsonObject.getString("key");
        Integer ipType = Integer.parseInt(jsonObject.getString("type"));

        try {
            BookIndexData bookIndexData = soHandler.getByUrl(word, "全国");
            if (bookIndexData != null) {
                bookIndexData.setId(bookId + "_" + IndexSourceEnums.s360.getType());
                bookIndexData.setBookId(bookId);
                bookIndexData.setSource(IndexSourceEnums.s360.getType());
                bookIndexData.setUpdatedAt(new Date());
                bookIndexDataDao.updateInser(bookIndexData);
                crawlerService.saveBookIndexDataHistory(bookIndexData);
            }
        } catch (Exception e) {
            log.error("SoIndexEventQueueHandler  failed: bookId:" + bookId + " word:" + word, e);
        } finally {
            crawlerService.check(key, bookId, ipType);
            try {
                Thread.sleep(NumberUtils.randomInt(1000, 3000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

}
