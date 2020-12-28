package me.zhengjie.modules.sgmw.domain;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@ApiModel("keycloak上的用户信息扩展")
@Table(name = "keycloak_user_extend")
public class KeycloakUserExtend {

    @Id
    @Column(name = "username")
    private String username;

    @Column(name = "keycloak_user_id")
    private String id;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "department_name")
    private String departmentName;

    @Column(name = "title")
    private String title;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "home_telephone")
    private String homeTelephone;

    @Column(name = "mobile_telephone")
    private String mobileTelephone;

}
