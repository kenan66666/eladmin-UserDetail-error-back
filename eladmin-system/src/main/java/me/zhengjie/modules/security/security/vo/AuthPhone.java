package me.zhengjie.modules.security.security.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthPhone {

    private String phone;

    private String code;

    private String uuid = "";
}
