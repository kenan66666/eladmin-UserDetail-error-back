package me.zhengjie.modules.sgmw.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

/**
 * @auther: wukenan
 * @date: 2020/11/11
 * @description:
 */
@Entity
@Getter
@Setter
@Table(name="knlge_area_system")
public class AreaSystem {
    @Id
    @Column(name="id")
    @NotNull(groups = BaseEntity.Update.class)
    @ApiModelProperty(value = "ID" , hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(groups = BaseEntity.Update.class)
    @ApiModelProperty(value = "系统编号")
    private String sysId;

    @NotNull(groups = BaseEntity.Update.class)
    @ApiModelProperty(value = "系统名称")
    private String sysName;

    @NotNull(groups = BaseEntity.Update.class)
    @ApiModelProperty(value = "区域")
    private String area;

    @ApiModelProperty(value = "系统英文名")
    private String sysEnglishName;

    @ApiModelProperty(value = "系统上线时间")
    private Date goLiveDate;

    @ApiModelProperty(value = "系统下线时间")
    private Date shutDownDate;

    @ApiModelProperty(value = "备注编号")
    private Integer remarkId = 0;

    @ApiModelProperty(value = "备注")
    private String remark;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaSystem that = (AreaSystem) o;
        return sysName.equals(that.sysName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sysName);
    }
}
