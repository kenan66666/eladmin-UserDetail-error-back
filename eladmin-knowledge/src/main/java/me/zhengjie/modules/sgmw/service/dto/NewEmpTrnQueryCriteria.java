package me.zhengjie.modules.sgmw.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;
import me.zhengjie.annotation.DataPermission;

import java.sql.Timestamp;
import java.util.List;

/**
 * @auther: wukenan
 * @date: 2020/8/27
 * @description:
 */

@Data
@DataPermission(fieldName = "id")
public class NewEmpTrnQueryCriteria {

    @Query(type = Query.Type.INNER_LIKE)
    private String name;

    @Query
    private Boolean enabled;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

    @Query
    private Integer pid;

    @Query(type = Query.Type.IS_NULL,propName = "pid")
    private Boolean pidIsNull;
}
