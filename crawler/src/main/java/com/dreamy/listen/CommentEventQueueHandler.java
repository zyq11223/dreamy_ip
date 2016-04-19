package com.dreamy.listen;

import com.alibaba.fastjson.JSONObject;
import com.dreamy.handler.CommentHandler;
import com.dreamy.mogodb.beans.Comment;
import com.dreamy.mogodb.beans.Comments;
import com.dreamy.mogodb.dao.CommentDao;
import com.dreamy.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by wangyongxing on 16/4/18.
 */
public class CommentEventQueueHandler  extends  AbstractQueueHandler{

    private static final Logger log = LoggerFactory.getLogger(CrawlerEventQueueHandler.class);

    @Autowired
    private CommentHandler commentHandler;
    @Autowired
    private CommentDao commentDao;

    @Override
    public void consume(JSONObject jsonObject) {
        //获取类型
        Integer type = jsonObject.getInteger("type");
        Integer ipId=jsonObject.getInteger("ipId");
        String url=jsonObject.getString("url");
        List<Comment> commentList= commentHandler.getByUrl(url);
        if(CollectionUtils.isNotEmpty(commentList))
        {
            Comments comment=new Comments();
            comment.setIpId(ipId);
            comment.setComments(commentList);
            commentDao.save(comment);
        }

    }
}
