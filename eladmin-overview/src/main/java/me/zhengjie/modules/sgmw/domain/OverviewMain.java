package me.zhengjie.modules.sgmw.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * @auther: wukenan
 * @date: 2020/11/18
 * @description:
 */
@Entity
@Getter
@Setter
@Table(name="overview_main")
public class OverviewMain extends BaseEntity implements Serializable {
    @Id
    @Column(name="id")
    @NotNull(groups = BaseEntity.Update.class)
    @ApiModelProperty(value = "ID" , hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(groups = BaseEntity.Update.class)
    @Column(unique = true, nullable = false, length = 20)
    @ApiModelProperty(value = "系统编号")
    private String sysId;

    @NotNull(groups = BaseEntity.Update.class)
    @ApiModelProperty(value = "系统名称")
    @Column(unique = true, nullable = false, length = 20)
    private String sysName;

    @NotNull(groups = BaseEntity.Update.class)
    @ApiModelProperty(value = "区域")
    private String area;

    @ApiModelProperty(value = "系统类型")
    private String systemType;

    @ApiModelProperty(value = "功能简述")
    private String functionDescription;

    @ApiModelProperty(value = "系统负责人")
    private String owner;

    @ApiModelProperty(value = "开发语言")
    private String developmentLanguage;

    @ApiModelProperty(value = "客户端类型")
    private String clientType;

    @ApiModelProperty(value = "支持智能终端")
    private String supportSmartPhone;

    @ApiModelProperty(value = "网页链接")
    private String weblink;

    @ApiModelProperty(value = "是否支持AD认证")
    private String supportAd;

    @ApiModelProperty(value = "客户端类型")
    private String serverLocation;

    @ApiModelProperty(value = "应用软件类型")
    private String softwareType;

    @ApiModelProperty(value = "是否关键系统")
    private String vitalOrNot;

    @ApiModelProperty(value = "业务服务级别")
    private String businessServiceLevel;

    @ApiModelProperty(value = "业务作业性质")
    private String businessWorkProperty;

    @ApiModelProperty(value = "业务范围")
    private String businessScope;

    @ApiModelProperty(value = "是否POC")
    private String pocOrNot;

    @ApiModelProperty(value = "信息安全等级")
    private String informationSecurityLevel;

    @ApiModelProperty(value = "数据是否出国")
    private String dataAboard;

    @ApiModelProperty(value = "系统使用边界")
    private String systemUseScope;

    @ApiModelProperty(value = "个人信息数量")
    private String personalInfoAmount;

    @ApiModelProperty(value = "系统等保等级")
    private String systemSecurityLevel;

    @ApiModelProperty(value = "系统等保备案号")
    private String systemSecurityRecordNo;

    @ApiModelProperty(value = "系统使用边界")
    private String internetPoliceRecordNo;

    @ApiModelProperty(value = "互联网备案工信部备案号")
    private String internetMiitRecordNo;

    @ApiModelProperty(value = "rto")
    private String rto;

    @ApiModelProperty(value = "rpo")
    private String rpo;

    @ApiModelProperty(value = "mop")
    private String mop;

    @ApiModelProperty(value = "业务量峰值")
    private String businessPeakValue;

    @ApiModelProperty(value = "系统极限处理能力")
    private String systemCapability;

    @ApiModelProperty(value = "关联变更编号")
    private String changeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverviewMain that = (OverviewMain) o;
        return sysId.equals(that.sysId) &&
                sysName.equals(that.sysName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sysId, sysName);
    }
}
