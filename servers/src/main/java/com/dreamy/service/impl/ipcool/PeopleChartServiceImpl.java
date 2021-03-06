package com.dreamy.service.impl.ipcool;

import com.dreamy.beans.Page;
import com.dreamy.dao.iface.ipcool.PeopleChartDao;
import com.dreamy.domain.ipcool.PeopleChart;
import com.dreamy.domain.ipcool.PeopleChartConditions;
import com.dreamy.service.iface.ipcool.PeopleChartService;
import com.dreamy.utils.BeanUtils;
import com.dreamy.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by wangyongxing on 16/6/2.
 */
@Service
public class PeopleChartServiceImpl implements PeopleChartService {
    @Autowired
    private PeopleChartDao peopleChartDao;
    @Override
    public void save(PeopleChart peopleChart) {
        peopleChartDao.save(peopleChart);
    }

    @Override
    public List<PeopleChart> getList(PeopleChart peopleChart, Page page) {

        Map<String, Object> params = BeanUtils.toQueryMap(peopleChart);
        PeopleChartConditions conditions = new PeopleChartConditions();
        conditions.createCriteria().addByMap(params);
        if(page!=null){
            page.setTotalNum(peopleChartDao.countByExample(conditions));
            conditions.setPage(page);
        }
        return peopleChartDao.selectByExample(conditions);
    }

    @Override
    public List<PeopleChart> getListByBookId(Integer bookId) {
        PeopleChartConditions conditions=new PeopleChartConditions();
        conditions.createCriteria().andBookIdEqualTo(bookId);
        return peopleChartDao.selectByExample(conditions);
    }

    @Override
    public void saveOrUpdate(PeopleChart peopleChart) {
        List<PeopleChart> list=getListByBookId(peopleChart.getBookId());
        if(CollectionUtils.isNotEmpty(list)){
            PeopleChart old=list.get(0);
            peopleChart.id(old.getId());
            peopleChartDao.update(peopleChart);
        }
        else{
            save(peopleChart);
        }
    }

    @Override
    public PeopleChart getByBookIdAndType(Integer bookId, Integer type) {
        PeopleChartConditions conditions=new PeopleChartConditions();
        conditions.createCriteria().andBookIdEqualTo(bookId);
        List<PeopleChart>  peopleChartList = peopleChartDao.selectByExample(conditions);
        return null;
    }
}
