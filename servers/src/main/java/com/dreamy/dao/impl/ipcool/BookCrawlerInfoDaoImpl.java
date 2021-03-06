package com.dreamy.dao.impl.ipcool;

import com.dreamy.dao.BaseDaoImpl;
import com.dreamy.dao.iface.ipcool.BookCrawlerInfoDao;
import com.dreamy.domain.ipcool.BookCrawlerInfo;
import com.dreamy.domain.ipcool.BookCrawlerInfoConditions;
import com.dreamy.mapper.ipcool.BookCrawlerInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * Created by wangyongxing on 16/4/11.
 */
@Repository("bookCrawlerInfoDao")
public class BookCrawlerInfoDaoImpl extends BaseDaoImpl<BookCrawlerInfo,Integer,BookCrawlerInfoConditions>implements BookCrawlerInfoDao {
   @Resource
    private BookCrawlerInfoMapper bookCrawlerInfoMapperl;
    @Override
    @Autowired
    public void setBaseMapper() {
        super.setBaseMapper(bookCrawlerInfoMapperl);

    }
}
