package com.zf.partnerback.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.generator.IFill;
import com.zf.partnerback.common.Result;
import com.zf.partnerback.entity.User;
import com.zf.partnerback.entity.domain.DTO.UserRequest;
import com.zf.partnerback.exception.ServiceException;
import com.zf.partnerback.mapper.UserMapper;
import com.zf.partnerback.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zf.partnerback.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ZF
 * @since 2022-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    EmailUtils emailUtils;
    private static final Map<String, Long> CODE_MAP = new ConcurrentHashMap<>();
    private static final long TIME_IN_MS5 = 5 * 60 * 1000;  // 表示5分钟的毫秒数

    /**
     * 登录
     * @param user
     * @return
     */
    @Override
    public User login(User user) {
        User dbUser = null;
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        //允许通过用户名以及邮箱登录
        lqw.eq(User::getUsername, user.getUsername()).or().eq(User::getEmail,user.getUsername());
        try {
            dbUser = userMapper.selectOne(lqw);
        } catch (Exception e) {
            throw new RuntimeException("系统异常");
        }
        if (dbUser == null) {
            throw new ServiceException("未找到用户");
        }
        if (!user.getPassword().equals(dbUser.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        return dbUser;
    }

    /**
     * 注册
     *
     * @param user
     * @return
     */
    @Override
    public User register(UserRequest user) {
        //校验邮箱
        //校验验证码
        validateEmail(user.getEmail(),user.getCode());
        try {
            User user1 = new User();
            BeanUtils.copyProperties(user, user1);
            User dbUser = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user1.getUsername()));
            if (dbUser != null) {
                throw new ServiceException("该用户已存在");
            }
            if (StrUtil.isBlank(user.getPassword())) {
                user.setPassword("123456");//设置默认密码
            }
            user1.setUid(IdUtil.fastSimpleUUID());//设置随机用户唯一标识
            boolean b = save(user1);
            if (!b) {
                throw new RuntimeException("注册失败");
            }
            return user1;
        } catch (Exception e) {
            throw new RuntimeException("数据库异常");
        }

    }

    //邮件发送验证码
    @Override
    public void sendEmail(String email, String type) {

        String code = RandomUtil.randomNumbers(6);
        log.info("本次验证码为:" + code);
        String context = "<b>尊敬的用户：</b><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;您好，【Partner交友网】提醒您，本次的验证码是:<b>{}</b>，有效期5分钟。<br><br><br><b>【Partner交友网】</b>";
        String html = StrUtil.format(context, code);//拼接进去{}
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if ("REGISTER".equals(type)) {//如果是注册
            //校验邮箱是否已注册，邮箱是唯一的
            if (user != null) {
                throw new ServiceException("该邮箱已注册");
            }
        } else if ("RESETPASSWORD".equals(type)) {
            if (user == null) {
                throw new ServiceException("该邮箱未注册");
            }
        }
        ThreadUtil.execAsync(() -> {//多线程 异步 执行任务，不会影响后面的操作
            emailUtils.sendHtml("Partner交友网", html, email);
        });
        String key=email+code;//将邮箱与验证码结合存储，方便后期校验
        //模拟缓存
        CODE_MAP.put(key, System.currentTimeMillis());//将验证码作为键存储
    }

    /**
     * 密码重置
     *
     * @param userRequest
     * @return
     */
    @Override
    public String resetPassword(UserRequest userRequest) {
        User dbUser = getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, userRequest.getEmail()));
        if (dbUser == null) {
            throw new ServiceException("未找到该用户");
        }
        //校验验证码
        validateEmail(userRequest.getEmail(),userRequest.getCode());
        String newPass = "123456";
        dbUser.setPassword(newPass);
        try {
            updateById(dbUser);
        } catch (Exception e) {
            throw new RuntimeException("重置密码失败", e);
        }
        return newPass;
    }


    /**
     * 校验邮箱验证码
     *
     * @param code
     */
    private void validateEmail(String email,String code) {
        String key=email+code;
        //校验邮箱验证码
        Long timestamp = CODE_MAP.get(key);//根据前台传来的验证码作为键获取
        if (timestamp == null) {
            throw new ServiceException("请先校验邮箱");
        }
        if (timestamp + TIME_IN_MS5 < System.currentTimeMillis()) {
            throw new ServiceException("验证码已过期");
        }
        CODE_MAP.remove(key);//验证之后将验证码删除
    }
    //数据库优化
    @Override
    public void select1() {
        userMapper.select1();
    }
}
