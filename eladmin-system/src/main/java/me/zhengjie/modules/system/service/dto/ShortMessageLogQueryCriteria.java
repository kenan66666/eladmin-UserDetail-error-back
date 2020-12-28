package me.zhengjie.modules.system.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;

import java.sql.Timestamp;

/**
* @author Kermit
* @date 2020-01-13
*/
@Data
public class ShortMessageLogQueryCriteria {

    /** 精确 */
    @Query
    private Integer id;

    /** 精确 */
    @Query
    private String phone;

    /** 模糊 */
    @Query(type = Query.Type.INNER_LIKE)
    private String content;

    /** 精确 */
    @Query
    private Timestamp sendTime;
}