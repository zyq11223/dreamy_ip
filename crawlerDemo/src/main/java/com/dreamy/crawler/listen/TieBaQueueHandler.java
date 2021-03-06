package com.dreamy.crawler.listen;

import com.alibaba.fastjson.JSONObject;
import com.dreamy.crawler.handler.tieba.TieBaHandler;
import com.dreamy.crawler.service.CrawlerService;
import com.dreamy.mogodb.beans.tieba.TieBa;
import com.dreamy.service.iface.mongo.TieBaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by wangyongxing on 16/6/1.
 */
public class TieBaQueueHandler extends AbstractQueueHandler {

    private static final Logger log = LoggerFactory.getLogger(TieBaQueueHandler.class);

    @Autowired
    TieBaHandler tieBaHandler;
    @Autowired
    private CrawlerService crawlerService;
    @Autowired
    private TieBaService tieBaService;

    @Override
    public void consume(JSONObject jsonObject) {

        String word = jsonObject.getString("word");
        Integer bookId = jsonObject.getInteger("bookId");
        String key = jsonObject.getString("key");
        Integer type = Integer.parseInt(jsonObject.getString("type"));

        try {
            TieBa tieBa = tieBaHandler.crawler(word, bookId);
            if (tieBa != null) {
                tieBa.setName(word);
                tieBaService.updateInser(tieBa);
                crawlerService.saveTieBaHistory(tieBa);
            }
        } catch (Exception e) {
            log.warn("TieBaQueueHandler  failed: bookId:" + bookId + " word:" + word);
        } finally {
            crawlerService.check(key, bookId, type);
        }


    }
}
