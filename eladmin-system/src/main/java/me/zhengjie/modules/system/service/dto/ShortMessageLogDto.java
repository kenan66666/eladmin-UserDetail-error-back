package me.zhengjie.modules.system.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
* @author Kermit
* @date 2020-01-13
*/
@Data
public class ShortMessageLogDto implements Serializable {

    /** id */
    private Integer id;

    /** 手机号 */
    private String phone;

    /** 内容 */
    private String content;

    /** 发送时间 */
    private Timestamp sendTime;
}