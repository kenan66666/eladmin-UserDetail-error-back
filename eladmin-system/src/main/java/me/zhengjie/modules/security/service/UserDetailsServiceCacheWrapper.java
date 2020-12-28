package me.zhengjie.modules.security.service;

import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.modules.security.config.bean.SecurityProperties;
import me.zhengjie.modules.security.service.dto.JwtUserDto;
import me.zhengjie.modules.sgmw.service.KeycloakService;
import me.zhengjie.modules.sgmw.service.dto.KeycloakAccountDTO;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.utils.RedisUtils;
import me.zhengjie.utils.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import me.zhengjie.modules.security.security.vo.JwtUser;

import java.util.List;

/**
 * @auther: wukenan
 * @date: 2020/12/15
 * @description:
 */

@Service
public class UserDetailsServiceCacheWrapper {
    @Autowired
    private UserService userService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private KeycloakService keycloakService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private SecurityProperties properties;
    private final static String CACHE_KEY_PREFIX = "data_store_user::";

    public UserDetails loadUserByUsername(String username) {
        String cacheKey = buildCacheKey(username);
        UserDetails userDetails = (UserDetails) redisUtils.get(cacheKey);
        if (userDetails == null) {
            try {
                final Result<KeycloakAccountDTO> currentKeycloakAccount = keycloakService.getCurrentKeycloakAccount();
                if (currentKeycloakAccount.isSuccess()) {
                    userService.mergeUser(currentKeycloakAccount.getData());
                }
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (EntityNotFoundException ignored) {
            }
            if (userDetails != null) {
                redisUtils.set(cacheKey, userDetails, getTokenValidityInSeconds());
            }
        }
        return userDetails;
    }

    public void clearAllCache() {
        List<String> keys = redisUtils.scan(CACHE_KEY_PREFIX + "*");
        redisUtils.del(keys.toArray(new String[0]));
    }

    public void clearCache(String username) {
        redisUtils.del(buildCacheKey(username));
    }

    private String buildCacheKey(String username) {
        return CACHE_KEY_PREFIX + username;
    }

    private long getTokenValidityInSeconds() {
        return properties.getTokenValidityInSeconds() == null ? 0 : properties.getTokenValidityInSeconds() / 1000;
    }
}
