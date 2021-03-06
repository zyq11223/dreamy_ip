package com.dreamy.service.impl.ipcool;

import com.dreamy.beans.Page;
import com.dreamy.beans.dto.BookViewWithExt;
import com.dreamy.dao.iface.ipcool.BookRankDao;
import com.dreamy.domain.ipcool.BookRank;
import com.dreamy.domain.ipcool.BookRankConditions;
import com.dreamy.domain.ipcool.BookRankHistory;
import com.dreamy.domain.ipcool.BookView;
import com.dreamy.enums.BookIndexTypeEnums;
import com.dreamy.enums.BookLevelEnums;
import com.dreamy.enums.BookRankEnums;
import com.dreamy.enums.BookRankTrendEnums;
import com.dreamy.service.cache.RedisClientService;
import com.dreamy.service.iface.ipcool.BookRankHistoryService;
import com.dreamy.service.iface.ipcool.BookRankService;
import com.dreamy.service.iface.ipcool.BookViewService;
import com.dreamy.utils.BeanUtils;
import com.dreamy.utils.CollectionUtils;
import com.dreamy.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by wangyongxing on 16/4/26.
 */
@Service
public class BookRankServiceImpl implements BookRankService {
    @Resource
    BookRankDao bookRankDao;

    @Autowired
    private RedisClientService redisClientService;

    @Autowired
    private BookViewService bookViewService;

    @Autowired
    private BookRankHistoryService bookRankHistoryService;

    @Override
    public void save(BookRank bookRank) {
        bookRankDao.save(bookRank);
    }

    @Override
    public List<BookRank> getList(BookRank bookRank, Page page, String order) {
        Map<String, Object> params = BeanUtils.toQueryMap(bookRank);
        BookRankConditions conditions = new BookRankConditions();
        conditions.createCriteria().addByMap(params);
        if (page != null) {
            page.setTotalNum(bookRankDao.countByExample(conditions));
            conditions.setPage(page);
        }
        if (StringUtils.isNotEmpty(order)) {
            conditions.setOrderByClause(order);
        }

        return bookRankDao.selectByExample(conditions);
    }

    @Override
    public List<BookViewWithExt> getRankPositionAndDetailByBookIdAndType(Integer rankId, Integer rankType) {
        List<BookViewWithExt> bookViewWithExts = new LinkedList<>();
        BookRankConditions conditions = new BookRankConditions();

        Integer endIndex = rankId + 3;
        Integer startIndex = rankId - 1;
        if (startIndex < 0) {
            startIndex = 0;
        }

        conditions.createCriteria().andRankBetween(startIndex, endIndex).andTypeEqualTo(rankType);
        conditions.setOrderByClause("rank asc");

        List<BookRank> bookRanks = bookRankDao.selectByExample(conditions);
        if (CollectionUtils.isNotEmpty(bookRanks)) {
            for (BookRank r : bookRanks) {
                BookView bookview = bookViewService.getByBookId(r.getBookId());
                if (bookview != null) {
                    BookViewWithExt bookViewWithExt = new BookViewWithExt();
                    bookViewWithExt.setBookView(bookview);
                    bookViewWithExt.setCompositeRank(r.getRank());
                    bookViewWithExt.setTrend(getRankTrendByBookIdAndTypeAndIndex(r.getBookId(), rankType, r.getRankIndex()));

                    bookViewWithExts.add(bookViewWithExt);
                }
            }
        }

        return bookViewWithExts;
    }

    @Override
    public List<BookRank> getListByBookIds(List<Integer> bookIds) {


        return null;
    }

    @Override
    public BookRank getByBookIdAndType(Integer bookId, Integer type) {
        BookRankConditions conditions = new BookRankConditions();
        conditions.createCriteria().andBookIdEqualTo(bookId).andTypeEqualTo(type);

        Page page = new Page();
        page.setPageSize(1);
        conditions.setPage(page);

        List<BookRank> bookRanks = bookRankDao.selectByExample(conditions);
        if (CollectionUtils.isNotEmpty(bookRanks)) {
            return bookRanks.get(0);
        }

        return null;
    }

    @Override
    public Map<Integer, Integer> getBookRankMapFromRedisByCacheKey(String cacheKey) {
        Map<Integer, Integer> map = new HashMap<>();

        Set<Object> redisSetResult = redisClientService.reverseZrange(cacheKey, 0, -1);
        if (CollectionUtils.isNotEmpty(redisSetResult)) {
            Integer i = 1;
            for (Object bookId : redisSetResult) {
                map.put(Integer.parseInt(bookId.toString()), i);
                i++;
            }
        }

        return map;
    }

    @Override
    public Map<Integer, Integer> getCompositeRankMapByBookIds(List<Integer> bookIds) {
        if (CollectionUtils.isNotEmpty(bookIds)) {
//            Map<Integer, Integer> map = getCompositeRankMapByBookIdFromRedis(bookIds);
            Map<Integer, Integer> map = new HashMap<>();
            if (map.size() < bookIds.size()) {
                BookRankConditions bookRankConditions = new BookRankConditions();
                bookRankConditions.createCriteria().andBookIdIn(bookIds);

                List<BookRank> bookRanks = bookRankDao.selectByExample(bookRankConditions);
                if (CollectionUtils.isNotEmpty(bookRanks)) {
                    for (BookRank bookRank : bookRanks) {
                        if (bookRank.getType().equals(BookIndexTypeEnums.composite.getType())) {
                            map.put(bookRank.getBookId(), bookRank.getRank());
                        }
                    }
                }
            }
            return map;
        }

        return null;
    }

    public Map<Integer, Integer> getCompositeRankMapByBookIdFromRedis(List<Integer> bookIds) {
        Map<Integer, Integer> map = new HashMap<>();
        for (Integer bookId : bookIds) {
            Long crank = redisClientService.reverseZrank(BookRankEnums.composite.getCacheKey(), bookId.toString());
            if (crank != null && crank > 0) {
                map.put(bookId, crank.intValue());
            }
        }

        return map;
    }


    @Override
    public Integer getRankTrendByBookIdAndTypeAndIndex(Integer bookId, Integer type, Integer randIndex) {
        Integer res = BookRankTrendEnums.keep.getType();
        Page page = new Page();
        page.setPageSize(1);
        List<BookRankHistory> bookRankHistoryList = bookRankHistoryService.getByBookIdAndType(bookId, type, page);
        if (CollectionUtils.isNotEmpty(bookRankHistoryList)) {
            BookRankHistory bookRankHistory = bookRankHistoryList.get(0);
            Integer lastIndex = bookRankHistory.getRankIndex();
            if (randIndex > lastIndex) {
                res = BookRankTrendEnums.up.getType();
            } else if (randIndex < lastIndex) {
                res = BookRankTrendEnums.down.getType();
            }
        }

        return res;
    }

    @Override
    public Integer deleteById(Integer id) {
        return bookRankDao.deleteById(id);
    }

    @Override
    public Integer deleteByBookId(Integer bookId) {
        BookRankConditions conditions = new BookRankConditions();
        conditions.createCriteria().andBookIdEqualTo(bookId);
        return bookRankDao.deleteByExample(conditions);
    }

    @Override
    public Integer deleteByBookIdAndType(Integer bookId, Integer type) {
        BookRankConditions conditions = new BookRankConditions();
        conditions.createCriteria().andBookIdEqualTo(bookId).andTypeEqualTo(type);
        return bookRankDao.deleteByExample(conditions);
    }

    @Override
    public Integer getRankClassByPosition(Integer position, Integer totalNum) {
        Integer classLevel = BookLevelEnums.five_class.getLevel();
        if (position < 0 || totalNum < 0) {
            return classLevel;
        }

        Double percent = ((double) totalNum) / position;
        if (percent >= BookLevelEnums.one_class.getValue()) {
            classLevel = BookLevelEnums.one_class.getLevel();
        } else if (percent >= BookLevelEnums.two_class.getValue()) {
            classLevel = BookLevelEnums.two_class.getLevel();
        } else if (percent >= BookLevelEnums.three_class.getValue()) {
            classLevel = BookLevelEnums.three_class.getLevel();
        } else if (percent >= BookLevelEnums.four_class.getValue()) {
            classLevel = BookLevelEnums.four_class.getLevel();
        }

        return classLevel;

    }

    @Override
    public Integer updateByRecord(BookRank bookRank) {

        return bookRankDao.update(bookRank);
    }

    @Override
    public List<BookRank> getBookRankByBookId(Integer bookId) {
        BookRankConditions conditions = new BookRankConditions();
        conditions.createCriteria().andBookIdEqualTo(bookId);

        return bookRankDao.selectByExample(conditions);
    }

    public List<BookRank> getBookRankByBookIdFromRedis(Integer bookId){
        List<BookRank> bookRankList = new LinkedList<>();
        Long crank = redisClientService.reverseZrank(BookRankEnums.composite.getCacheKey(), bookId.toString());
        Long drank = redisClientService.reverseZrank(BookRankEnums.develop.getCacheKey(), bookId.toString());
        Long prank = redisClientService.reverseZrank(BookRankEnums.propagation.getCacheKey(), bookId.toString());
        Long hrank = redisClientService.reverseZrank(BookRankEnums.hot.getCacheKey(), bookId.toString());


        BookRank bookRank = new BookRank();
        if (crank == null || crank <= 0) {
            bookRank = getByBookIdAndType(bookId, BookIndexTypeEnums.composite.getType());
        } else {
            bookRank.setType(BookIndexTypeEnums.composite.getType());
            bookRank.setRank(crank.intValue());
        }
        bookRankList.add(bookRank);

        bookRank = new BookRank();
        if (drank == null || drank <= 0) {
            bookRank = getByBookIdAndType(bookId, BookIndexTypeEnums.develop.getType());
        } else {
            bookRank.setType(BookIndexTypeEnums.develop.getType());
            bookRank.setRank(drank.intValue());
        }
        bookRankList.add(bookRank);

        bookRank = new BookRank();
        if (prank == null || prank <= 0) {
            bookRank = getByBookIdAndType(bookId, BookIndexTypeEnums.propagate.getType());
        } else {
            bookRank.setType(BookIndexTypeEnums.propagate.getType());
            bookRank.setRank(prank.intValue());
        }
        bookRankList.add(bookRank);

        bookRank = new BookRank();
        if (hrank == null || hrank <= 0) {
            bookRank = getByBookIdAndType(bookId, BookIndexTypeEnums.hot.getType());
        } else {
            bookRank.setType(BookIndexTypeEnums.hot.getType());
            bookRank.setRank(hrank.intValue());
        }
        bookRankList.add(bookRank);

        return bookRankList;
    }

    @Override
    public List<BookRank> getBookRankByOrderAndType(String order, Integer type, Page page) {
        BookRankConditions conditions = new BookRankConditions();
        conditions.createCriteria().andTypeEqualTo(type);

        conditions.setPage(page);
        conditions.setOrderByClause(order);

        return bookRankDao.selectByExample(conditions);
    }
}
