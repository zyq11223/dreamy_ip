package com.dreamy.crawler;

/**
 * Created by wangyongxing on 16/3/31.
 */
import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext-mvc.xml"})
public class BaseJunitTest extends TestCase {

}
