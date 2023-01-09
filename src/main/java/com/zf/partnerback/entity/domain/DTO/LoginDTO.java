package com.zf.partnerback.entity.domain.DTO;

import com.zf.partnerback.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

/**
 * @ClassName: Login
 * @Description: TODO
 * @Author: ZF
 * @date: 2023/1/9 10:42
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private User user;
    private String token;
}
