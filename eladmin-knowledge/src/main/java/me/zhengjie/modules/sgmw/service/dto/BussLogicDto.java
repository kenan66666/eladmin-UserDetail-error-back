package me.zhengjie.modules.sgmw.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @auther: wukenan
 * @date: 2020/10/12
 * @description:
 */
@Getter
@Setter
public class BussLogicDto extends BaseDTO implements Serializable {
    private Long id;
    private Long pid;
    private Integer subCount;
    private String name;
    private Integer knowsSort;
    private String docDir;
    private boolean enabled;
    public Boolean getLeaf() {
        return subCount <= 0;
    }
    public String getLabel() {
        return name;
    }

    public Boolean getHasChildren() {
        return subCount > 0;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<BussLogicDto> children;

    private String docName;
    private String docRealName;
    private String docType;
    private String docSuffix;
    private String docSize;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BussLogicDto that = (BussLogicDto) o;
        return id.equals(that.id) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
