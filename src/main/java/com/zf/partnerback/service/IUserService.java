package com.zf.partnerback.service;

import com.zf.partnerback.common.Result;
import com.zf.partnerback.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zf.partnerback.entity.domain.DTO.UserRequest;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ZF
 * @since 2022-12-22
 */
public interface IUserService extends IService<User> {

    User login(User user);

    User register(UserRequest user);

    void sendEmail(String email, String type);

    String resetPassword(UserRequest userRequest);
}
