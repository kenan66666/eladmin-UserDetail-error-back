package me.zhengjie.modules.security.rest;

import cn.hutool.json.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.annotation.AnonymousAccess;
import me.zhengjie.annotation.Log;
import me.zhengjie.modules.security.service.ShortMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jie
 * @date 2018-11-23
 * 授权、根据token获取用户详细信息
 */
@Slf4j
@RestController
@RequestMapping("auth")
@Api(tags = "系统：短信发送接口")
public class ShortMsgController {

    @Autowired
    private ShortMsgService msgService;

    /**
     * 发送短信
     * @param request
     * @return
     */
    @Log("发送认证码")
    @ApiOperation("发送认证码")
    @AnonymousAccess
    @PostMapping(value = "/phone/send")
    public ResponseEntity send(HttpServletRequest request,@RequestBody JSONObject jsonObject){
        log.info("-------------------注册用户，准备发送短信-------------------");
        return ResponseEntity.ok(msgService.send(request,jsonObject));
    }

}