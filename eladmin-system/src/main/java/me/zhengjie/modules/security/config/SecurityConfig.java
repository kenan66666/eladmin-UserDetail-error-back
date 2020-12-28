/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.modules.security.config;

import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.AnonymousAccess;
import me.zhengjie.modules.security.config.bean.SecurityProperties;
import me.zhengjie.modules.security.security.*;
import me.zhengjie.modules.security.service.OnlineUserService;
import me.zhengjie.modules.security.service.UserCacheClean;
import me.zhengjie.modules.security.service.UserDetailsServiceCacheWrapper;
import me.zhengjie.utils.enums.RequestMethodEnum;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

/**
 * @author Zheng Jie
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

//    private final TokenProvider tokenProvider;
//    private final CorsFilter corsFilter;
//    private final JwtAuthenticationEntryPoint authenticationErrorHandler;
//    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
//    private final ApplicationContext applicationContext;
//    private final SecurityProperties properties;
//    private final OnlineUserService onlineUserService;
//    private final UserCacheClean userCacheClean;
//
//    @Bean
//    GrantedAuthorityDefaults grantedAuthorityDefaults() {
//        // 去除 ROLE_ 前缀
//        return new GrantedAuthorityDefaults("");
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        // 密码加密方式
//        return new BCryptPasswordEncoder();
//    }
//
//    @Override
//    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
//        return new NullAuthenticatedSessionStrategy();
//    }
//
//    @Override
//    protected void configure(HttpSecurity httpSecurity) throws Exception {
//        // 搜寻匿名标记 url： @AnonymousAccess
//        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = applicationContext.getBean(RequestMappingHandlerMapping.class).getHandlerMethods();
//        // 获取匿名标记
//        Map<String, Set<String>> anonymousUrls = getAnonymousUrl(handlerMethodMap);
//        httpSecurity
//                // 禁用 CSRF
//                .csrf().disable()
//                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
//                // 授权异常
//                .exceptionHandling()
//                .authenticationEntryPoint(authenticationErrorHandler)
//                .accessDeniedHandler(jwtAccessDeniedHandler)
//                // 防止iframe 造成跨域
//                .and()
//                .headers()
//                .frameOptions()
//                .disable()
//                // 不创建会话
//                .and()
//                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .and()
//                .authorizeRequests()
//                // 静态资源等等
//                .antMatchers(
//                        HttpMethod.GET,
//                        "/*.html",
//                        "/**/*.html",
//                        "/**/*.css",
//                        "/**/*.js",
//                        "/webSocket/**"
//                ).permitAll()
//                // swagger 文档
//                .antMatchers("/swagger-ui.html").permitAll()
//                .antMatchers("/swagger-resources/**").permitAll()
//                .antMatchers("/webjars/**").permitAll()
//                .antMatchers("/*/api-docs").permitAll()
//                // 文件
//                .antMatchers("/avatar/**").permitAll()
//                .antMatchers("/file/**").permitAll()
//                // 阿里巴巴 druid
//                .antMatchers("/druid/**").permitAll()
//                // 放行OPTIONS请求
//                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                // 自定义匿名访问所有url放行：允许匿名和带Token访问，细腻化到每个 Request 类型
//                // GET
//                .antMatchers(HttpMethod.GET, anonymousUrls.get(RequestMethodEnum.GET.getType()).toArray(new String[0])).permitAll()
//                // POST
//                .antMatchers(HttpMethod.POST, anonymousUrls.get(RequestMethodEnum.POST.getType()).toArray(new String[0])).permitAll()
//                // PUT
//                .antMatchers(HttpMethod.PUT, anonymousUrls.get(RequestMethodEnum.PUT.getType()).toArray(new String[0])).permitAll()
//                // PATCH
//                .antMatchers(HttpMethod.PATCH, anonymousUrls.get(RequestMethodEnum.PATCH.getType()).toArray(new String[0])).permitAll()
//                // DELETE
//                .antMatchers(HttpMethod.DELETE, anonymousUrls.get(RequestMethodEnum.DELETE.getType()).toArray(new String[0])).permitAll()
//                // 所有类型的接口都放行
//                .antMatchers(anonymousUrls.get(RequestMethodEnum.ALL.getType()).toArray(new String[0])).permitAll()
//                // 所有请求都需要认证
//                .anyRequest().authenticated()
//                .and().apply(securityConfigurerAdapter());
//    }
//
//    private Map<String, Set<String>> getAnonymousUrl(Map<RequestMappingInfo, HandlerMethod> handlerMethodMap) {
//        Map<String, Set<String>> anonymousUrls = new HashMap<>(6);
//        Set<String> get = new HashSet<>();
//        Set<String> post = new HashSet<>();
//        Set<String> put = new HashSet<>();
//        Set<String> patch = new HashSet<>();
//        Set<String> delete = new HashSet<>();
//        Set<String> all = new HashSet<>();
//        for (Map.Entry<RequestMappingInfo, HandlerMethod> infoEntry : handlerMethodMap.entrySet()) {
//            HandlerMethod handlerMethod = infoEntry.getValue();
//            AnonymousAccess anonymousAccess = handlerMethod.getMethodAnnotation(AnonymousAccess.class);
//            if (null != anonymousAccess) {
//                List<RequestMethod> requestMethods = new ArrayList<>(infoEntry.getKey().getMethodsCondition().getMethods());
//                RequestMethodEnum request = RequestMethodEnum.find(requestMethods.size() == 0 ? RequestMethodEnum.ALL.getType() : requestMethods.get(0).name());
//                switch (Objects.requireNonNull(request)) {
//                    case GET:
//                        get.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
//                        break;
//                    case POST:
//                        post.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
//                        break;
//                    case PUT:
//                        put.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
//                        break;
//                    case PATCH:
//                        patch.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
//                        break;
//                    case DELETE:
//                        delete.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
//                        break;
//                    default:
//                        all.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
//                        break;
//                }
//            }
//        }
//        anonymousUrls.put(RequestMethodEnum.GET.getType(), get);
//        anonymousUrls.put(RequestMethodEnum.POST.getType(), post);
//        anonymousUrls.put(RequestMethodEnum.PUT.getType(), put);
//        anonymousUrls.put(RequestMethodEnum.PATCH.getType(), patch);
//        anonymousUrls.put(RequestMethodEnum.DELETE.getType(), delete);
//        anonymousUrls.put(RequestMethodEnum.ALL.getType(), all);
//        return anonymousUrls;
//    }
//
//    private TokenConfigurer securityConfigurerAdapter() {
//        return new TokenConfigurer(tokenProvider, properties, onlineUserService, userCacheClean);
//    }

    @Autowired
    private UserDetailsServiceCacheWrapper userDetailsServiceCacheWrapper;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(keycloakAuthenticationProvider());
    }

    // 不使用keycloak提供的配置解析器，希望有适当的spring boot配置，所以我们使用KeycloakSpringBootConfigResolver来
    // 覆盖keycloak的配置解析器，返回KeycloakSpringBootConfigResolver对象
    @Bean
    KeycloakConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    // 跨域访问相关的方法
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");//修改为添加而不是设置，* 最好改为实际的需要，我这是非生产配置，所以粗暴了一点
        configuration.addAllowedMethod("*");//修改为添加而不是设置
        configuration.addAllowedHeader("*");//这里很重要，起码需要允许 Access-Control-Allow-Origin
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        // 去除 ROLE_ 前缀
        return new GrantedAuthorityDefaults("");
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    @Bean
    public FilterRegistrationBean keycloakAuthenticationProcessingFilterRegistrationBean(
            KeycloakAuthenticationProcessingFilter filter) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    // 阻值双重注册KeyCloak过滤器
    @Bean
    public FilterRegistrationBean keycloakPreAuthActionsFilterRegistrationBean(
            KeycloakPreAuthActionsFilter filter) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 密码加密方式
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .sessionAuthenticationStrategy(sessionAuthenticationStrategy()).and()
                .addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class)
                .addFilterBefore(keycloakAuthenticationProcessingFilter(), X509AuthenticationFilter.class)
                .addFilterBefore(new KeycloakUserFilter(userDetailsServiceCacheWrapper), X509AuthenticationFilter.class)
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint()).and()
                .authorizeRequests()
                .requestMatchers(CorsUtils::isCorsRequest).permitAll()
                .antMatchers(SgmwPermitAllUri.urls).permitAll()
                .antMatchers(
                        HttpMethod.GET,
                        "/*.html",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/webSocket/**"
                ).permitAll()
                // swagger 文档
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/doc.html").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/*/api-docs").permitAll()
                // 文件
                .antMatchers("/avatar/**").permitAll()
                .antMatchers("/file/**").permitAll()
                // 阿里巴巴 druid
                .antMatchers("/druid/**").permitAll()
                // 订单回调
                .antMatchers("/api/orderCallback").permitAll()
                // API校验
                .antMatchers("/api/userApp/checkAndGetApiCallContext").permitAll()
                .antMatchers("/api/iconInfo/download/**").permitAll()
//        .antMatchers("/family/*").hasAnyAuthority("user").antMatchers("/admin/*").hasRole("ADMIN")
                .antMatchers("/**").authenticated()
                .anyRequest().permitAll();
    }


}
