package com.zf.partnerback.entity.domain.DTO;

import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String password;
    private String email;
    private String name;
    private String code;
}
