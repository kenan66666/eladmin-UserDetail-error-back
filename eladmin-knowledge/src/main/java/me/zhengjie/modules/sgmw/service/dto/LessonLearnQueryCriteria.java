package me.zhengjie.modules.sgmw.service.dto;

import cn.hutool.core.date.DateTime;
import lombok.Data;
import me.zhengjie.annotation.Query;
import org.bouncycastle.asn1.cms.TimeStampAndCRL;

import java.sql.Timestamp;
import java.util.List;

/**
 * @auther: wukenan
 * @date: 2020/11/5
 * @description:
 */
@Data
public class LessonLearnQueryCriteria {
    @Query(type = Query.Type.INNER_LIKE)
    private String name;

    @Query(type = Query.Type.INNER_LIKE)
    private String sysName;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

    @Query
    private Boolean enabled;

    @Query
    private Integer pid;

    @Query
    private Integer id;

    @Query(type = Query.Type.IS_NULL,propName = "pid")
    private Boolean pidIsNull;

}
