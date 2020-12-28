package me.zhengjie.modules.security.security.vo;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author Kermit
 * @date 2020-01-13
 */
@Setter
@Getter
public class RegisterUser {
    @NotBlank
    private String username;

    @NotBlank
    private String phone;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String code;



    private String uuid = "";

    @Override
    public String toString() {
        return "{username=" + username  + ", password= ******}";
    }
}
