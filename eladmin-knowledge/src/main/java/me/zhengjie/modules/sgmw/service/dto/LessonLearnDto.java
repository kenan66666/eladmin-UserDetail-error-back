package me.zhengjie.modules.sgmw.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @auther: wukenan
 * @date: 2020/11/5
 * @description:
 */
@Getter
@Setter
public class LessonLearnDto extends BaseEntity implements Serializable {

    private Long id;
    private Long pid;
    private Integer subCount;
    private String name;
    private String sysId;
    private String sysName;
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
    private List<LessonLearnDto> children;

    private String docName;
    private String docRealName;
    private String docType;
    private String docSuffix;
    private String docSize;

    private String incidentId;
    private String requireId;
    private String changeId;
    private String releaseId;
    private String projectId;
    private String area;
    private String rootCause;
    private String appear;
    private String solution;
    private String tags;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LessonLearnDto that = (LessonLearnDto) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
