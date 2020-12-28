package me.zhengjie.modules.system.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
* @author Kermit
* @date 2020-01-13
*/
@Entity
@Data
@Table(name="short_message_log")
public class ShortMessageLog implements Serializable {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 手机号 */
    @Column(name = "phone")
    private String phone;

    /** 内容 */
    @Column(name = "content")
    private String content;

    /** 发送时间 */
    @Column(name = "send_time")
    private Timestamp sendTime;

    public void copy(ShortMessageLog source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}