package me.zhengjie.modules.sgmw.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * @auther: wukenan
 * @date: 2020/10/12
 * @description:
 */

@Entity
@Getter
@Setter
@Table(name="knlge_busslogic")
public class BussLogic extends BaseEntity implements Serializable {

    @Id
    @Column(name="id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID" , hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "父的编号")
    private Long pid;

    @NotBlank
    @ApiModelProperty(value = "业务逻辑与流程目录")
    private String name;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BussLogic bussLogic = (BussLogic) o;
        return id.equals(bussLogic.id) &&
                name.equals(bussLogic.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
