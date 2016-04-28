package com.dreamy.ipcool.controllers.homepage;

import com.dreamy.ipcool.controllers.IpcoolController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created with IntelliJ IDEA.
 * User: yujianfu (yujianfu@duotin.com)
 * Date: 16/4/28
 * Time: 上午10:37
 */
@Controller
@RequestMapping("/")
public class HomepageController extends IpcoolController{

    public String index() {
        return "/index";
    }
}