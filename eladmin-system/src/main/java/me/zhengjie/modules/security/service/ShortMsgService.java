package me.zhengjie.modules.security.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.modules.security.constants.PhoneBean;
import me.zhengjie.modules.security.security.vo.RegisterUser;
import me.zhengjie.modules.system.domain.Role;
import me.zhengjie.modules.system.domain.ShortMessageLog;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.service.ShortMessageLogService;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.modules.system.service.dto.UserDto;
import me.zhengjie.modules.system.service.dto.UserQueryCriteria;
import me.zhengjie.utils.EncryptUtils;
import me.zhengjie.utils.Pbkdf2Sha256;
import me.zhengjie.utils.RedisUtils;
import me.zhengjie.utils.StringUtils;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author jie
 * @date 2018-11-22
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class ShortMsgService {
    @Value("${short-message.url}")
    private String url;

    @Value("${short-message.pwd}")
    private String pwd;

    @Value("${short-message.content}")
    private String content;

    @Value("${short-message.sp}")
    private String sp;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private ShortMessageLogService shortMessageLogService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisUtils redisUtils;

    private Pbkdf2Sha256 pbkdf2Sha256;


    public JSONObject send(HttpServletRequest request, JSONObject jsonObject) {
        String phone = jsonObject.getStr("phone");//手机号
        JSONObject result = new JSONObject();
        result.put("code", "200");
        result.put("msg", "发送成功。");
        if (null == phone || "".equals(phone)) {
            result.put("code", "415");
            result.put("msg", "手机号码不能为空。");
            result.put("uuid", "");
            return result;
        }
        //需要校验上次发送的短信的时间间隔
        Object timeObj = request.getSession().getAttribute(phone);
        if (null != timeObj) {
            PhoneBean bean = (PhoneBean) timeObj;
            long curTime = System.currentTimeMillis();
            if (curTime - bean.getActiveTime() < 60000) {
                result.put("code", "415");
                result.put("msg", "一分钟内同一手机只能发送一次");
                log.info("-----------一分钟内同一手机只能发送一次--------------");
                return result;
            }
        }
        //解密短信平台密码，并发送短信
        try {
            String temp = EncryptUtils.desDecrypt(pwd);
            String realPwd = temp.substring(4, temp.length() - 4);
            //log.info("-----------秘钥为：" + realPwd + "--------------");
            String time = System.currentTimeMillis() + "";
            Map<String,Object> body = new HashMap<>();
            Map<String,Object> child = new HashMap<>();
            child.put("dc", 15);
            //随机产生4位数字验证码
            int random = (int) ((Math.random() * 9 + 1) * 1000);
            log.info("-----------" + phone + "准备发送的验证码：" + random + "--------------");
            child.put("sm", encoding(content.replace("{}", random + ""), 15));
            List<Map<String,Object>> numList = new ArrayList<>();
            Map<String,Object> map = new HashMap<>();
            map.put("da", "86" + phone);
            numList.add(map);
            child.put("num_list", numList);
            body.put("mt_req", child);
            HttpRequest.post(url)
                    .header("Content-Type", "application/json;charset=utf-8")
                    .header("Accept", "application/json")
                    .header("sp", sp)
                    .header("timestamp", time)
                    .header("signature", SecureUtil.md5(realPwd + time))
                    .body(JSON.toJSONString(body)).execute().bodyBytes();
            PhoneBean bean = new PhoneBean();
            bean.setActiveTime(System.currentTimeMillis());
            bean.setCode(random + "");
            request.getSession().setAttribute(phone, bean);
            //生成uuid存入redis
            String uuid = IdUtil.simpleUUID();
            result.put("uuid", uuid);
            redisUtils.set(uuid, random + "", 15, TimeUnit.MINUTES);
            /**
             * 保存短信息
             */
            ShortMessageLog message = new ShortMessageLog();
            message.setContent(content.replace("{}", random + ""));
            message.setPhone(phone);
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currTime = sd.format(new Date());
            Timestamp t = Timestamp.valueOf(currTime);
            message.setSendTime(t);
            shortMessageLogService.create(message);//记录发送日志
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送短信报错：" + e.getMessage());
            result.put("code", "415");
            result.put("msg", "发送短信异常。");
            result.put("uuid", "");
            return result;
        }
        log.info("-----------" + phone + "短信发送完成--------------");
        return result;
    }

    public JSONObject register(HttpServletRequest request, RegisterUser resources) {
        JSONObject result = new JSONObject();
        result.put("code", "200");
        result.put("msg", "手机号码绑定成功。");
        String username = resources.getUsername();//微信用户id
        String code = resources.getCode();//验证码
        String phone = resources.getPhone();
        //String token = jsonObject.getStr("token");
        if (null == phone || "".equals(phone)) {
            result.put("code", "415");
            result.put("msg", "手机号码不能为空。");
            return result;
        }
        //验证码有效期校验
//        Object timeObj = request.getSession().getAttribute(phone);
//        if(null != timeObj){
//            PhoneBean bean = (PhoneBean) timeObj;
//            long curTime = System.currentTimeMillis();
//            if(curTime - bean.getActiveTime() > 15 * 60 * 1000){
//                result.put("code","415");
//                result.put("msg","该验证码已过期，请重新获取。");
//                log.info("-----------该验证码已过期，请重新获取。--------------");
//                return result;
//            }
//            //验证码是否正确
//            if (!code.equals(bean.getCode())) {
//                result.put("code","415");
//                result.put("msg","验证码错误，请确认。");
//                log.info("-----------验证码错误，请确认。输入验证码：" + code + ",发送的验证码：" + bean.getCode() + "--------------");
//                return result;
//            }
//        } else {
//            result.put("code","415");
//            result.put("msg","页面已过期，请重新刷新页面。");
//            log.info("-----------页面已过期，请重新刷新页面。--------------");
//            return result;
//        }

        // 查询验证码
        String codeCheck = (String) redisUtils.get(resources.getUuid());

        if (StringUtils.isBlank(codeCheck)) {
            result.put("code", "415");
            result.put("msg", "该验证码已过期，请重新获取。");
            log.info("-----------该验证码已过期，请重新获取。--------------");
            return result;
        }
        if (StringUtils.isBlank(resources.getCode()) || !resources.getCode().equalsIgnoreCase(codeCheck)) {
            result.put("code", "415");
            result.put("msg", "验证码错误，请确认。");
            log.info("-----------验证码错误，请确认。--------------");
            return result;
        }
        //手机号唯一性校验
        UserQueryCriteria userPhoneQueryCriteria = new UserQueryCriteria();
        userPhoneQueryCriteria.setPhone(phone);
        List<UserDto> phoneList = userService.queryAll(userPhoneQueryCriteria);
        if (null != phoneList && phoneList.size() > 0) {
            result.put("code", "415");
            result.put("msg", "该手机号已注册，请确认。");
            log.info("-----------该手机号已注册，请确认：" + phone + "--------------");
            return result;
        }

        //用户名唯一性校验
        UserQueryCriteria userNameQueryCriteria = new UserQueryCriteria();
        userNameQueryCriteria.setUsername(username);
        List<UserDto> usernameList = userService.queryAll(userNameQueryCriteria);
        if (null != usernameList && usernameList.size() > 0) {
            result.put("code", "415");
            result.put("msg", "该用户名已注册，请确认。");
            log.info("-----------该用户名已注册，请确认：" + username + "--------------");
            return result;
        }


        //创建user用户
        User user = new User();
        user.setUsername(resources.getUsername());
        user.setPhone(resources.getPhone());
        user.setEmail(resources.getEmail());
        user.setPassword(passwordEncoder.encode(resources.getPassword()));//加密user密码
        user.setEnabled(true);
        //设置用户为普通角色
        Set<Role> roles = new HashSet<Role>();
        Role role = new Role();
        role.setId(Long.valueOf(2));
        roles.add(role);
        user.setRoles(roles);

        UserDto userInfo = userService.create(user);

        // 清除验证码
        redisUtils.del(resources.getUuid());
        result.put("userInfo", userInfo);
        log.info("-----------手机号注册成功：" + phone + "--------------");
        return result;
    }

    //编码
    public String encoding(String sm, int dc) throws Exception {
        String charSet = null;
        if (0 == dc) {
            charSet = "ISO-8859-1";
        } else if (8 == dc) {
            charSet = "UTF-16BE";
        } else if (15 == dc) {
            charSet = "GBK";
        }
        byte[] textByte = sm.getBytes(charSet);
        char[] smChar = Hex.encodeHex(textByte);
        return String.valueOf(smChar);
    }

    //解码
    public String decoding(String sm, int dc) throws Exception {
        String charSet = null;
        if (0 == dc) {
            charSet = "ISO-8859-1";
        } else if (8 == dc) {
            charSet = "UTF-16BE";
        } else if (15 == dc) {
            charSet = "GBK";
        }
        byte[] textByte = Hex.decodeHex(sm.toCharArray());
        return new String(textByte, charSet);
    }
}
