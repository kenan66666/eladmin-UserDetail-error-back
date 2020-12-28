package me.zhengjie.modules.sgmw.service.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakAccountDTO {

    @ApiModelProperty("唯一键")
    private String id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("名称")
    private String firstName;

    @ApiModelProperty("姓氏")
    private String lastName;

    @ApiModelProperty("部门名称")
    private String departmentName;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("展示名称")
    private String displayName;

    @ApiModelProperty("全称")
    private String fullName;

    @ApiModelProperty("家庭电话")
    private String homeTelephone;

    @ApiModelProperty("移动电话")
    private String mobileTelephone;

    @ApiModelProperty("是否已经存在")
    private Boolean exist;

}
