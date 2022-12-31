package com.zf.partnerback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zf.partnerback.common.Result;
import com.zf.partnerback.entity.User;
import com.zf.partnerback.exception.ServiceException;
import com.zf.partnerback.mapper.UserMapper;
import com.zf.partnerback.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ZF
 * @since 2022-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
@Autowired
private UserMapper userMapper;
    @Override
    public User login(User user) {
        User dbUser =null;
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.eq(User::getUsername,user.getUsername());
        try {
            dbUser = userMapper.selectOne(lqw);
        }catch (Exception e){
            throw new RuntimeException("系统异常");
        }
        if (dbUser==null){
            throw new ServiceException("未找到用户");
        }
        if (!user.getPassword().equals(dbUser.getPassword())){
            throw new ServiceException("用户名或密码错误");
        }
        return dbUser;
    }
}
