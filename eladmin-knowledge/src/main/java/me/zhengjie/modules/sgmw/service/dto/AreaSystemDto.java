package me.zhengjie.modules.sgmw.service.dto;

import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

/**
 * @auther: wukenan
 * @date: 2020/11/11
 * @description:
 */
@Getter
@Setter
public class AreaSystemDto extends BaseEntity implements Serializable {
    private Long id;
    private String area;
    private String sysId;
    private String sysName;
    private String sysEnglishName;
    private Integer remarkId;
    private String remark;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaSystemDto that = (AreaSystemDto) o;
        return sysName.equals(that.sysName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sysName);
    }
}
