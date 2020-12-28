package me.zhengjie.modules.sgmw.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;

/**
 * @auther: wukenan
 * @date: 2020/11/11
 * @description:
 */
@Data
public class AreaSystemQueryCriteria {

    @Query(type = Query.Type.INNER_LIKE)
    private String sysName;

    @Query(type = Query.Type.INNER_LIKE)
    private String area;

    @Query(type = Query.Type.INNER_LIKE)
    private String sysId;

    @Query
    private Integer remarkId;
}
