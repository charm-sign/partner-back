package com.zf.partnerback.mapper;

import com.zf.partnerback.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ZF
 * @since 2022-12-22
 */
public interface UserMapper extends BaseMapper<User> {

    void select1();
}
