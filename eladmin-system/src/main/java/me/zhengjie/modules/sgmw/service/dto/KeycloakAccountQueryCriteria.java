package me.zhengjie.modules.sgmw.service.dto;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @author yl
 * @date 2020-01-03
 */
@Data
public class KeycloakAccountQueryCriteria {

    private String username;
    private String firstName;
    private String lastName;
    private String email;

    // 如果上述的过滤条件都没有，则search生效
    private String search;

    public boolean hasValue() {
        return StringUtils.hasText(username)
                || StringUtils.hasText(firstName)
                || StringUtils.hasText(lastName)
                || StringUtils.hasText(email)
                || StringUtils.hasText(search);
    }

    public boolean onlySearchHasValue() {
        return !StringUtils.hasText(username)
                && !StringUtils.hasText(firstName)
                && !StringUtils.hasText(lastName)
                && !StringUtils.hasText(email);
    }


}