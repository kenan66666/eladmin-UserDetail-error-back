package me.zhengjie.modules.sgmw.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @auther: wukenan
 * @date: 2020/11/5
 * @description:
 */
@Entity
@Getter
@Setter
@Table(name = "knlge_lesson_learn")
public class LessonLearn extends BaseEntity implements Serializable {
    @Id
    @Column(name="id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID" , hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "父的编号")
    private Long pid;

    @NotBlank
    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "系统编号")
    private String sysId;

    @ApiModelProperty(value = "系统名称")
    private String sysName;

    @ApiModelProperty(value = "文档路径")
    private String docDir;

    @ApiModelProperty(value = "子的数目", hidden = true)
    private Integer subCount = 0;

    @ApiModelProperty(value = "排序")
    private Integer knowsSort;
//
//    @JsonIgnore
//    @ApiModelProperty(value = "角色")
//    @ManyToMany(mappedBy = "NewEmpTrns")
//    private Set<Role> roles;

    @NotNull
    @ApiModelProperty(value = "状态")
    private Boolean enabled;

    @ApiModelProperty(value = "文件名称")
    private String docName;

    @ApiModelProperty(value = "文件真正名称")
    private String docRealName;

    @ApiModelProperty(value = "文件类型")
    private String docType;

    @ApiModelProperty(value = "文件后缀")
    private String docSuffix;

    @ApiModelProperty(value = "文件大小")
    private String docSize;

    @ApiModelProperty(value = "事件编号")
    private String incidentId;

    @ApiModelProperty(value = "需求编号")
    private String requireId;

    @ApiModelProperty(value = "变更编号")
    private String changeId;

    @ApiModelProperty(value = "发布编号")
    private String releaseId;

    @ApiModelProperty(value = "项目编号")
    private String projectId;

    @ApiModelProperty(value = "区域")
    private String area;

    @ApiModelProperty(value = "根本原因")
    private String rootCause;

    @ApiModelProperty(value = "问题现象")
    private String appear;

    @ApiModelProperty(value = "解决方案")
    private String solution;

    @ApiModelProperty(value = "标签")
    private String tags;
}
