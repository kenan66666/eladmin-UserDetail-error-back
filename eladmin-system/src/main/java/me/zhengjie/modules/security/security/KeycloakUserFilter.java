package me.zhengjie.modules.security.security;

import lombok.RequiredArgsConstructor;
import me.zhengjie.modules.security.service.UserDetailsServiceCacheWrapper;
import me.zhengjie.utils.SecurityUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class KeycloakUserFilter extends GenericFilterBean {

    private final UserDetailsServiceCacheWrapper userDetailsServiceCacheWrapper;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean flag = authentication != null && !CollectionUtils.isEmpty(authentication.getAuthorities());

        if (flag) {

            KeycloakAuthenticationToken keycloakAuthenticationToken = (KeycloakAuthenticationToken) authentication;
            // 有点疑惑，这个工具类怎么拿到的username
            String username = SecurityUtils.getUsername();
            UserDetails userDetails = userDetailsServiceCacheWrapper.loadUserByUsername(username);

            if (userDetails != null) {
                KeycloakAuthenticationToken newToken = new KeycloakAuthenticationToken(keycloakAuthenticationToken.getAccount(), false, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newToken);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);

    }

}
