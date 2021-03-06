package com.crawler.test;

import com.dreamy.crawler.handler.info.netbook.qd.QiDian;
import com.dreamy.mogodb.beans.NetBookInfo;
import com.dreamy.utils.HttpUtils;
import com.dreamy.utils.PatternUtils;
import com.dreamy.utils.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.model.OOSpider;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangyongxing on 16/6/6.
 */
public class QianDiMmMain {
    public static void main(String[] args) {
        String url="http://all.qdmm.com/MMWeb/BookStore.aspx?ChannelId=41&SubCategoryId=-1&Tag=all&Size=-1&Action=-1&OrderId=6&P=all&PageIndex=1&update=-1&Vip=1";
        OOSpider ooSpider = OOSpider.create(Site.me().setSleepTime(0), QiDian.class);
        QiDian qiDian = ooSpider.<QiDian>get(url);
        NetBookInfo info = new NetBookInfo();
        int size = qiDian.getUrls().size();
        List<String> urls = qiDian.getUrls();
        List<String> names = qiDian.getTitles();
        List<String> authoers = qiDian.getAuthoers();
        for (int i = 0; i < size; i++) {
            String utl = urls.get(i);
            info.setAuthor(authoers.get(i));
            info.setTitle(names.get(i));
            String bookId = PatternUtils.getNum(utl);
            info.setImage("http://image.cmfu.com/books/" + bookId + "/" + bookId + ".jpg");
            info.setBookId(Integer.valueOf(bookId));
            crawler(info,utl);
        }
    }

    public static void crawler(NetBookInfo info, String url) {

        String html = HttpUtils.getHtmlGet(url);
        if (StringUtils.isNotEmpty(html)) {
            Document document = Jsoup.parse(html);
            if (document != null) {
                getInfo(info, document);
                getOverInfo(info, document);
                getOverAuthority(info, document);
                //getLabel(info, document);
                getCategory(info, document);
                getScore(info, document);
                getTicketNum(info, document);
                getClickNum(info, document);
                getRecommendNum(info, document);

            }
        }

    }

    public static void getInfo(NetBookInfo info, Document document) {

        Elements elements = document.getElementById("contentdiv").getElementsByAttributeValue("itemprop", "description");
        if (elements != null && elements.size() > 0) {
            Element element = elements.first();
            info.setInfo(element.text());
        }


    }

    public static void getClickNum(NetBookInfo info, Document document) {

        Elements elements = document.getElementById("bookdiv").getElementsByAttributeValue("itemprop", "totalClick");
        if (elements != null && elements.size() > 0) {
            Element element = elements.first();
            String num = element.text() != null ? element.text() : "0";
            info.setClickNum(Integer.valueOf(num));
        }


    }

    public static void getRecommendNum(NetBookInfo info, Document document) {

        Elements elements = document.getElementById("bookdiv").getElementsByAttributeValue("itemprop", "totalRecommend");
        if (elements != null && elements.size() > 0) {
            Element element = elements.first();
            String num = element.text() != null ? element.text() : "0";
            info.setRecommendNum(Integer.valueOf(num));
        }
    }

    public static void getOverInfo(NetBookInfo info, Document document) {

        Elements elements = document.getElementById("bookdiv").getElementsByAttributeValue("itemprop", "updataStatus");
        if (elements != null && elements.size() > 0) {
            Element element = elements.first();
            info.setOver(element.text());
        }


    }


    public static void getCategory(NetBookInfo info, Document document) {

        Elements elements = document.getElementById("bookdiv").getElementsByAttributeValue("itemprop", "genre");
        if (elements != null && elements.size() > 0) {
            Element element = elements.first();
            info.setCategory(element.text());
        }


    }

    /**
     * 作者 自定义标签
     *
     * @param info
     * @param document
     */
    public static void getLabel(NetBookInfo info, Document document) {

        StringBuffer labels = new StringBuffer();
        Elements elements = document.select("div.labels>div.box>a");
        if (elements != null && elements.size() > 0) {
            for (Element element : elements) {
                labels.append(element.text() + ",");
            }
        }

        String str = labels.toString();
        info.setLabel(str.substring(0, str.length() - 1));


    }


    public static void getOverAuthority(NetBookInfo info, Document document) {

        Elements elements = document.getElementById("bookdiv").getElementsByTag("tr");
        if (elements != null && elements.size() > 0) {
            Element element = elements.last();
            Elements tds = element.children();
            if (tds != null && tds.size() > 2) {
                Element td = tds.get(2);
                info.setAuthority(td.text().replace("授权状态：", "").replace(" ", ""));
                System.out.println(td.text().replace("授权状态：", "").replace(" ", ""));
            }
        }


    }

    public static void getScore(NetBookInfo info, Document document) {

        Element element = document.getElementById("bzhjshu");
        if (element != null) {
            info.setScore(element.text());
        }
    }


    public static void getTicketNum(NetBookInfo info, Document document) {

        Elements elements = document.getElementsByClass("ballot_data");
        if (elements != null && elements.size() > 0) {
            Element element = elements.first();
            String num = getResult(element.text());
            info.setTicketNum(Integer.valueOf(num));
        }
    }

    public static String getResult(String str) {
        String result = "0";
        Pattern p = Pattern.compile("本月票数：(.*?)票");
        Matcher m = p.matcher(str);
        while (m.find()) {
            result = m.group(1);
        }
        if (StringUtils.isEmpty(result)) {
            return "0";
        }
        return result;

    }
}
