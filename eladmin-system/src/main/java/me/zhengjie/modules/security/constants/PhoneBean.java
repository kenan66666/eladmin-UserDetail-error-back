package me.zhengjie.modules.security.constants;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 
 * @author weiben
 *
 */
@Setter
@Getter
public class PhoneBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;

    private long activeTime;

}
