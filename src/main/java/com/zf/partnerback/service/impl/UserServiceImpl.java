package com.zf.partnerback.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zf.partnerback.common.Constants;
import com.zf.partnerback.common.enums.EmailCodeEnum;
import com.zf.partnerback.entity.User;

import com.zf.partnerback.entity.domain.DTO.LoginDTO;
import com.zf.partnerback.entity.domain.DTO.UserRequest;
import com.zf.partnerback.exception.ServiceException;
import com.zf.partnerback.mapper.UserMapper;
import com.zf.partnerback.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zf.partnerback.utils.EmailUtils;
import com.zf.partnerback.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
    @Autowired
    RedisUtils redisUtils;

    //    private static final Map<String, Long> CODE_MAP = new ConcurrentHashMap<>();
    private static final long TIME_IN_MS5 = 5 * 60 * 1000;  // 表示5分钟的毫秒数

    /**
     * 登录
     *
     * @param user
     * @return
     */
    @Override
    public LoginDTO login(User user) {
        User dbUser = null;
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        //允许通过用户名以及邮箱登录
        lqw.eq(User::getUsername, user.getUsername()).or().eq(User::getEmail, user.getUsername());
        try {
            dbUser = userMapper.selectOne(lqw);
        } catch (Exception e) {
            throw new RuntimeException("系统异常");
        }
        if (dbUser == null) {
            throw new ServiceException("未找到用户");
        }
        if (!BCrypt.checkpw(user.getPassword(),dbUser.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        StpUtil.login(dbUser.getUid());//证明登陆了，自动存储相关信息
        // 会在redis记录用户id与token
//       用户id satoken:login:session:   会包含其它信息，也可存储用户信息
//       登录的token satoken:login:token:  值是存储的用户id
        StpUtil.getSession().set("userInfo", dbUser);//此操作就是将用户信息存入
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String tokenValue = tokenInfo.getTokenValue();//token值
        return LoginDTO.builder().user(dbUser).token(tokenValue).build();
    }

    //邮件发送验证码
    @Override
    public void sendEmail(String email, String type) {
        //通过枚举校验验证类型，枚举中定义了支持的类型
        String emailPrefix = EmailCodeEnum.getValue(type);  //将其作为键的前缀
        if (StrUtil.isBlank(emailPrefix)) {
            throw new ServiceException("不支持的验证类型");
        }
        //1分钟内不能重复发送
        String key = Constants.EMAIL_CODE + emailPrefix + email;
        Long expireTime = redisUtils.getExpireTime(key);//倒计时
        if (expireTime != null && expireTime > 4 * 60) {
            throw new ServiceException("发送次数过于频繁");
        }
        Integer code = Integer.valueOf(RandomUtil.randomNumbers(6));
        log.info("本次验证码为:" + code);
        String context = "<b>尊敬的用户：</b><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;您好，【Partner交友网】提醒您，本次的验证码是:<b>{}</b>，有效期5分钟。<br><br><br><b>【Partner交友网】</b>";
        String html = StrUtil.format(context, code);//拼接进去{}
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (EmailCodeEnum.REGISTER.equals(EmailCodeEnum.getEnum(type))) {//如果是注册
            //校验邮箱是否已注册，邮箱是唯一的
            if (user != null) {
                throw new ServiceException("该邮箱已注册");
            }
        } else if (EmailCodeEnum.RESET_PASSWORD.equals(EmailCodeEnum.getEnum(type))) {
//            System.out.println("***************"+EmailCodeEnum.RESET_PASSWORD);//RESET_PASSWORD
//            System.out.println("***************"+EmailCodeEnum.getEnum(type));//RESET_PASSWORD
            if (user == null) {
                throw new ServiceException("该邮箱未注册");
            }
        }
        ThreadUtil.execAsync(() -> {//多线程 异步 执行任务，不会影响后面的操作
            emailUtils.sendHtml("Partner交友网", html, email); //应该此步骤之后再缓存验证码，但发送的服务可能玩坏，所以直接存
        });
        //缓存验证码并设置有效时长5分钟 （key在规定时间之后会失效）
        redisUtils.setCacheObject(key, code, TIME_IN_MS5, TimeUnit.MILLISECONDS);

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
        String key = Constants.EMAIL_CODE + EmailCodeEnum.REGISTER.getValue() + user.getEmail();
        System.out.println("key" + key);
        validateEmail(key, user.getCode());
        try {
           user.setPassword(BCrypt.hashpw(user.getPassword()));
            User user1 = new User();
            BeanUtils.copyProperties(user, user1);
            User dbUser = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user1.getUsername()));
            if (dbUser != null) {
                throw new ServiceException("该用户已存在");
            }
            if (StrUtil.isBlank(user1.getPassword())) {
                user1.setPassword(BCrypt.hashpw("123456"));//设置默认密码
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
        String key = Constants.EMAIL_CODE + EmailCodeEnum.RESET_PASSWORD.getValue() + userRequest.getEmail();
        validateEmail(key, userRequest.getCode());
        String newPass = "123456";
        dbUser.setPassword(BCrypt.hashpw(newPass));
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
    private void validateEmail(String key, String code) {
        //校验邮箱验证码
        Integer emailCode = redisUtils.getCacheObject(key);
        if (emailCode == null) {
            throw new ServiceException("验证码失效");
        }
        if (!code.equals(emailCode.toString())) {
            throw new ServiceException("验证码错误");
        }
        redisUtils.deleteObject(key);
    }

    //数据库优化
    @Override
    public void select1() {
        userMapper.select1();
    }
}
