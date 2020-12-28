package me.zhengjie.utils.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页封装
 */
@ApiModel("分页对象")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Page<T> implements Serializable {

    public static final long serialVersionUID = 1L;

    /* 返回的数据 */
    @ApiModelProperty("当前页码的数据")
    private List<T> rows;

    /* 当前页码 */
    @ApiModelProperty("当前页码")
    private int pageNum;

    /* 每页条数 */
    @ApiModelProperty("每页条数")
    private int pageSize;

    /* 总条数 */
    @ApiModelProperty("总条数")
    private long totalElements;

    /* 总页数 */
    @ApiModelProperty("总页数")
    private long totalPages;

}
