package me.zhengjie.modules.sgmw.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class NewEmpTrnDto extends BaseDTO implements Serializable {
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
    private List<NewEmpTrnDto> children;

    private String docName;
    private String docRealName;
    private String docType;
    private String docSuffix;
    private String docSize;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewEmpTrnDto that = (NewEmpTrnDto) o;
        return id.equals(that.id) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
