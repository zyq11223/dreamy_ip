package com.dreamy.service.admin;

import com.dreamy.dao.admin.RoleDao;
import com.dreamy.domain.admin.Role;
import com.dreamy.domain.admin.RoleConditions;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by wangyongxing on 16/4/6.
 */
@Service
public class RoleServiceImpl implements RoleService {
    @Resource
    RoleDao roleDao;



}
