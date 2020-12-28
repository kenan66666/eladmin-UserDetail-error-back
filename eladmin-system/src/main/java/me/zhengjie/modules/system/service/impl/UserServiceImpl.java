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
package me.zhengjie.modules.system.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.modules.security.service.OnlineUserService;
import me.zhengjie.modules.security.service.UserCacheClean;
import me.zhengjie.modules.security.service.UserDetailsServiceCacheWrapper;
import me.zhengjie.modules.sgmw.domain.KeycloakUserExtend;
import me.zhengjie.modules.sgmw.repository.KeycloakUserExtendRepository;
import me.zhengjie.modules.sgmw.service.dto.KeycloakAccountDTO;
import me.zhengjie.modules.system.domain.Role;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.modules.system.domain.UserAvatar;
import me.zhengjie.modules.system.repository.UserAvatarRepository;
import me.zhengjie.modules.system.repository.UserRepository;
import me.zhengjie.modules.system.service.RoleService;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.modules.system.service.dto.JobSmallDto;
import me.zhengjie.modules.system.service.dto.RoleSmallDto;
import me.zhengjie.modules.system.service.dto.UserDto;
import me.zhengjie.modules.system.service.dto.UserQueryCriteria;
import me.zhengjie.modules.system.service.mapstruct.UserMapper;
import me.zhengjie.utils.*;
import me.zhengjie.utils.result.Result;
import me.zhengjie.utils.result.ResultEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "user")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RedisUtils redisUtils;
    private final UserAvatarRepository userAvatarRepository;
    private final UserDetailsServiceCacheWrapper userDetailsServiceCacheWrapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final KeycloakUserExtendRepository keycloakUserExtendRepository;
    // userDetailsServiceCacheWrapper.clearCache(username);// 清楚指定用户信息缓存
    // userDetailsServiceCacheWrapper.clearAllCache();// 清楚所有用户信息缓存

    @Value("${file.mac.avatar}")
    private String avatar;

    @Override
    @Cacheable
    public Object queryAll(UserQueryCriteria criteria, Pageable pageable) {
        Page<User> page = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        final Page<UserDto> dtoPage = page.map(userMapper::toDto);
        setExtend(dtoPage.getContent());
        return PageUtil.toPage(dtoPage);
    }

    @Override
    @Cacheable
    public List<UserDto> queryAll(UserQueryCriteria criteria) {
        List<User> users = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        final List<UserDto> userDtos = userMapper.toDto(users);
        setExtend(userDtos);
        return userDtos;
    }

    @Override
    @Cacheable(key = "#p0")
    public UserDto findById(long id) {
        User user = userRepository.findById(id).orElseGet(User::new);
        ValidationUtil.isNull(user.getId(), "User", "id", id);
        final UserDto userDto = userMapper.toDto(user);
        setExtend(Collections.singletonList(userDto));
        return userDto;
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public UserDto create(User resources) {
        if (userRepository.findByUsername(resources.getUsername()) != null) {
            throw new EntityExistException(User.class, "username", resources.getUsername());
        }
        if (userRepository.findByEmail(resources.getEmail()) != null) {
            throw new EntityExistException(User.class, "email", resources.getEmail());
        }
        userDetailsServiceCacheWrapper.clearCache(resources.getUsername());// 清楚指定用户信息缓存
        return userMapper.toDto(userRepository.save(resources));
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void update(User resources) {
        User user = userRepository.findById(resources.getId()).orElseGet(User::new);
        ValidationUtil.isNull(user.getId(), "User", "id", resources.getId());
        User user1 = userRepository.findByUsername(user.getUsername());

        if (user1 != null && !user.getId().equals(user1.getId())) {
            throw new EntityExistException(User.class, "username", resources.getUsername());
        }

        if (StringUtils.isNotBlank(user.getEmail())) {
            User user2 = userRepository.findByEmail(user.getEmail());
            if (user2 != null && !user.getId().equals(user2.getId())) {
                throw new EntityExistException(User.class, "email", resources.getEmail());
            }
        }

        // 如果用户的角色改变了，需要手动清理下缓存
        if (!resources.getRoles().equals(user.getRoles())) {
            String key = "role::loadPermissionByUser:" + user.getUsername();
            redisUtils.del(key);
            key = "role::findByUsers_Id:" + user.getId();
            redisUtils.del(key);
        }

        user.setUsername(resources.getUsername());
        user.setEmail(resources.getEmail());
        user.setEnabled(resources.getEnabled());
        user.setRoles(resources.getRoles());
        user.setDept(resources.getDept());
        user.setJob(resources.getJob());
        user.setPhone(resources.getPhone());
        user.setNickName(StringUtils.isBlank(resources.getNickName()) ? resources.getUsername() : resources.getNickName());
        user.setSex(resources.getSex());
        userDetailsServiceCacheWrapper.clearCache(resources.getUsername());// 清楚指定用户信息缓存
        userRepository.save(user);
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateCenter(User resources) {
        User user = userRepository.findById(resources.getId()).orElseGet(User::new);
        user.setNickName(resources.getNickName());
        user.setPhone(resources.getPhone());
        user.setSex(resources.getSex());
        userDetailsServiceCacheWrapper.clearCache(resources.getUsername());// 清楚指定用户信息缓存
        userRepository.save(user);
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            final Optional<User> opt = userRepository.findById(id);
            opt.ifPresent(user -> {
                userRepository.delete(user);
                keycloakUserExtendRepository.findById(user.getUsername())
                        .ifPresent(keycloakUserExtendRepository::delete);
            });
        }
        userDetailsServiceCacheWrapper.clearAllCache();// 清楚指定用户信息缓存
    }

    @Override
    @Cacheable(key = "'loadUserByUsername:'+#p0")
    public UserDto findByName(String userName) {
        User user;
        if (ValidationUtil.isEmail(userName)) {
            user = userRepository.findByEmail(userName);
        } else {
            user = userRepository.findByUsername(userName);
        }
        if (user == null) {
            throw new EntityNotFoundException(User.class, "name", userName);
        } else {
            final UserDto userDto = userMapper.toDto(user);
            setExtend(Collections.singletonList(userDto));
            return userDto;
        }
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updatePass(String username, String pass) {
        userRepository.updatePass(username, pass, new Date());
        userDetailsServiceCacheWrapper.clearCache(username);// 清楚指定用户信息缓存
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateAvatar(MultipartFile multipartFile) {
        User user = userRepository.findByUsername(SecurityUtils.getUsername());
        UserAvatar userAvatar = user.getUserAvatar();
        String oldPath = "";
        if (userAvatar != null) {
            oldPath = userAvatar.getPath();
        }
        File file = FileUtil.upload(multipartFile, avatar);
        assert file != null;
        userAvatar = userAvatarRepository.save(new UserAvatar(userAvatar, file.getName(), file.getPath(), FileUtil.getSize(multipartFile.getSize())));
        user.setUserAvatar(userAvatar);
        userRepository.save(user);
        if (StringUtils.isNotBlank(oldPath)) {
            FileUtil.del(oldPath);
        }
        userDetailsServiceCacheWrapper.clearCache(user.getUsername());// 清楚指定用户信息缓存
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(String username, String email) {
        userRepository.updateEmail(username, email);
        userDetailsServiceCacheWrapper.clearCache(username);// 清楚指定用户信息缓存
    }

    @Override
    public void download(List<UserDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserDto userDTO : queryAll) {
            List<String> roles = userDTO.getRoles().stream().map(RoleSmallDto::getName).collect(Collectors.toList());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", userDTO.getUsername());
            map.put("头像", userDTO.getAvatar());
            map.put("邮箱", userDTO.getEmail());
            map.put("状态", userDTO.getEnabled() ? "启用" : "禁用");
            map.put("手机号码", userDTO.getPhone());
            map.put("角色", roles);
            map.put("部门", userDTO.getDept().getName());
            map.put("岗位", userDTO.getJob().getName());
            map.put("最后修改密码的时间", userDTO.getLastPasswordResetTime());
            map.put("创建日期", userDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Result<Void> checkUserExist(String username) {
        try {
            UserDto userDto = findByName(username);
            return new Result<>(ResultEnum.SUCCESS);
        } catch (EntityNotFoundException ex) {
            return new Result<>(ResultEnum.FAILED);
        }
    }


    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public synchronized Result<UserDto> mergeUser(KeycloakAccountDTO keycloakAccountDTO) {
        String username = keycloakAccountDTO.getUsername();
        User user = userRepository.findByUsername(username);
        boolean userExist = true;
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if (user == null) {
            userExist = false;
            user = new User();
            user.setUsername(username);
            user.setCreateTime(timestamp);
            user.setSex("未知");
            user.setEnabled(Boolean.TRUE);
            user.setPassword(randomPassword());
            user.setRoles(new HashSet<>(roleService.getDefaultRoles()));
        }

        boolean emailEquals = Objects.equals(user.getEmail(), keycloakAccountDTO.getEmail());
        boolean phoneEquals = Objects.equals(user.getPhone(), keycloakAccountDTO.getMobileTelephone());
        if (!userExist || !emailEquals || !phoneEquals) {
            user.setNickName(StringUtils.isBlank(keycloakAccountDTO.getDisplayName()) ? keycloakAccountDTO.getUsername() : keycloakAccountDTO.getDisplayName());
            user.setEmail(StringUtils.isBlank(keycloakAccountDTO.getEmail()) ? keycloakAccountDTO.getUsername() + "@sgmw.com.cn" : keycloakAccountDTO.getEmail());
            user.setPhone(StringUtils.isBlank(keycloakAccountDTO.getMobileTelephone()) ? (StringUtils.isBlank(keycloakAccountDTO.getHomeTelephone()) ? "" : keycloakAccountDTO.getHomeTelephone()) : keycloakAccountDTO.getMobileTelephone());
            Long avatarId = user.getUserAvatar() == null ? null : user.getUserAvatar().getId();
            String email = user.getEmail();
            Boolean enabled = user.getEnabled();
            String password = user.getPassword();
            Long deptId = user.getDept() == null ? null : user.getDept().getId();
            String phone = user.getPhone();
            Long jobId = user.getJob() == null ? null : user.getJob().getId();
            Timestamp createTime = user.getCreateTime();
            Timestamp lastPasswordResetTime = user.getLastPasswordResetTime() == null ? null : new Timestamp(user.getLastPasswordResetTime().getTime());
            String nickName = user.getNickName();
            String sex = user.getSex();

            userRepository.insertOrUpdate(avatarId, email, enabled, password, username, deptId, phone, jobId, createTime, lastPasswordResetTime, nickName, sex);
        }

        {
            Long userId = userRepository.selectUserIdByUsername(username);
            Set<Role> roles = user.getRoles();
            if (CollectionUtils.isEmpty(roles)) {
                roles = new HashSet<>(roleService.getDefaultRoles());
            }
            userRepository.deleteRoleLinkByUserId(userId);
            for (Role role : roles) {
                final Long id = role.getId();
                userRepository.insertRoleLink(userId, id);
            }
        }

        final Optional<KeycloakUserExtend> opt = keycloakUserExtendRepository.findById(username);
        KeycloakUserExtend extend = opt.orElseGet(KeycloakUserExtend::new);

        BeanUtils.copyProperties(keycloakAccountDTO, extend);
        {
            String keycloakUserId = extend.getId();
            String email = extend.getEmail();
            String firstName = extend.getFirstName();
            String lastName = extend.getLastName();
            String departmentName = extend.getDepartmentName();
            String title = extend.getTitle();
            String displayName = extend.getDisplayName();
            String fullName = extend.getFullName();
            String homeTelephone = extend.getHomeTelephone();
            String mobileTelephone = extend.getMobileTelephone();

            keycloakUserExtendRepository.insertOrUpdate(username, keycloakUserId, email, firstName, lastName, departmentName, title, displayName, fullName, homeTelephone, mobileTelephone);
        }
//        keycloakUserExtendRepository.saveAndFlush(extend);

        final UserDto userDto = userMapper.toDto(user);
        userDto.setExtend(extend);

        return new Result<>(userDto);
    }

    @Override
    public String randomPassword() {
        return passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    public Set<String> selectUserExistState(Set<String> usernameSet) {
        return userRepository.selectUserExistState(usernameSet);
    }

    @Override
    public Map<String, String> selectDisplayNameMapping(Set<String> usernameSet) {
        Map<String, String> map = new HashMap<>();
        if (CollectionUtils.isEmpty(usernameSet)) {
            return map;
        }

        final List<Map<String, Object>> list = keycloakUserExtendRepository.findDisplayNameMappingByUsernameIn(usernameSet);

        if (CollectionUtils.isEmpty(list)) {
            return map;
        }

        for (Map<String, Object> row : list) {
            final Object username = row.get("username");
            final Object displayName = row.get("display_name");
            if (username != null && displayName != null) {
                map.put(username.toString(), displayName.toString());
            }
        }

        return map;
    }

    @Override
    public Result<Boolean> checkUserEnabled(String username) {
        final User u = userRepository.findByUsername(username);
        return new Result<>(u != null && Boolean.TRUE.equals(u.getEnabled()));
    }

    private void setExtend(List<UserDto> userDtos) {
        final List<UserDto> list = Optional.of(userDtos).orElseGet(ArrayList::new);
        final Set<String> usernameSet = list.stream().map(UserDto::getUsername).collect(Collectors.toSet());
        if (!usernameSet.isEmpty()) {
            final List<KeycloakUserExtend> extendList = keycloakUserExtendRepository.findAllByUsernameIn(usernameSet);
            for (UserDto item : list) {
                extendList.stream().filter(e -> Objects.equals(item.getUsername(), e.getUsername()))
                        .findFirst().ifPresent(item::setExtend);
            }
        }
    }

//    private final UserRepository userRepository;
//    private final UserMapper userMapper;
//    private final FileProperties properties;
//    private final RedisUtils redisUtils;
//    private final UserCacheClean userCacheClean;
//    private final OnlineUserService onlineUserService;
//
//    @Override
//    public Object queryAll(UserQueryCriteria criteria, Pageable pageable) {
//        Page<User> page = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
//        return PageUtil.toPage(page.map(userMapper::toDto));
//    }
//
//    @Override
//    public List<UserDto> queryAll(UserQueryCriteria criteria) {
//        List<User> users = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
//        return userMapper.toDto(users);
//    }
//
//    @Override
//    @Cacheable(key = "'id:' + #p0")
//    @Transactional(rollbackFor = Exception.class)
//    public UserDto findById(long id) {
//        User user = userRepository.findById(id).orElseGet(User::new);
//        ValidationUtil.isNull(user.getId(), "User", "id", id);
//        return userMapper.toDto(user);
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public UserDto create(User resources) {
//        if (userRepository.findByUsername(resources.getUsername()) != null) {
//            throw new EntityExistException(User.class, "username", resources.getUsername());
//        }
//        if (userRepository.findByEmail(resources.getEmail()) != null) {
//            throw new EntityExistException(User.class, "email", resources.getEmail());
//        }
//        userRepository.save(resources);
//        return null;
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void update(User resources) {
//        User user = userRepository.findById(resources.getId()).orElseGet(User::new);
//        ValidationUtil.isNull(user.getId(), "User", "id", resources.getId());
//        User user1 = userRepository.findByUsername(resources.getUsername());
//        User user2 = userRepository.findByEmail(resources.getEmail());
//
//        if (user1 != null && !user.getId().equals(user1.getId())) {
//            throw new EntityExistException(User.class, "username", resources.getUsername());
//        }
//
//        if (user2 != null && !user.getId().equals(user2.getId())) {
//            throw new EntityExistException(User.class, "email", resources.getEmail());
//        }
//        // 如果用户的角色改变
//        if (!resources.getRoles().equals(user.getRoles())) {
//            redisUtils.del(CacheKey.DATE_USER + resources.getId());
//            redisUtils.del(CacheKey.MENU_USER + resources.getId());
//            redisUtils.del(CacheKey.ROLE_AUTH + resources.getId());
//        }
//        // 如果用户名称修改
//        if(!resources.getUsername().equals(user.getUsername())){
//            redisUtils.del("user::username:" + user.getUsername());
//        }
//        // 如果用户被禁用，则清除用户登录信息
//        if(!resources.getEnabled()){
//            onlineUserService.kickOutForUsername(resources.getUsername());
//        }
//        user.setUsername(resources.getUsername());
//        user.setEmail(resources.getEmail());
//        user.setEnabled(resources.getEnabled());
//        user.setRoles(resources.getRoles());
//        user.setDept(resources.getDept());
//        user.setJobs(resources.getJobs());
//        user.setPhone(resources.getPhone());
//        user.setNickName(resources.getNickName());
//        user.setGender(resources.getGender());
//        userRepository.save(user);
//        // 清除缓存
//        delCaches(user.getId(), user.getUsername());
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updateCenter(User resources) {
//        User user = userRepository.findById(resources.getId()).orElseGet(User::new);
//        user.setNickName(resources.getNickName());
//        user.setPhone(resources.getPhone());
//        user.setGender(resources.getGender());
//        userRepository.save(user);
//        // 清理缓存
//        delCaches(user.getId(), user.getUsername());
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void delete(Set<Long> ids) {
//        for (Long id : ids) {
//            // 清理缓存
//            UserDto user = findById(id);
//            delCaches(user.getId(), user.getUsername());
//        }
//        userRepository.deleteAllByIdIn(ids);
//    }
//
//    @Override
//    @Cacheable(key = "'username:' + #p0")
//    public UserDto findByName(String userName) {
//        User user = userRepository.findByUsername(userName);
//        if (user == null) {
//            throw new EntityNotFoundException(User.class, "name", userName);
//        } else {
//            return userMapper.toDto(user);
//        }
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updatePass(String username, String pass) {
//        userRepository.updatePass(username, pass, new Date());
//        redisUtils.del("user::username:" + username);
//        flushCache(username);
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public Map<String, String> updateAvatar(MultipartFile multipartFile) {
//        User user = userRepository.findByUsername(SecurityUtils.getCurrentUsername());
//        String oldPath = user.getAvatarPath();
//        File file = FileUtil.upload(multipartFile, properties.getPath().getAvatar());
//        user.setAvatarPath(Objects.requireNonNull(file).getPath());
//        user.setAvatarName(file.getName());
//        userRepository.save(user);
//        if (StringUtils.isNotBlank(oldPath)) {
//            FileUtil.del(oldPath);
//        }
//        @NotBlank String username = user.getUsername();
//        redisUtils.del(CacheKey.USER_NAME + username);
//        flushCache(username);
//        return new HashMap<String, String>(1) {{
//            put("avatar", file.getName());
//        }};
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updateEmail(String username, String email) {
//        userRepository.updateEmail(username, email);
//        redisUtils.del(CacheKey.USER_NAME + username);
//        flushCache(username);
//    }
//
//    @Override
//    public void download(List<UserDto> queryAll, HttpServletResponse response) throws IOException {
//        List<Map<String, Object>> list = new ArrayList<>();
//        for (UserDto userDTO : queryAll) {
//            List<String> roles = userDTO.getRoles().stream().map(RoleSmallDto::getName).collect(Collectors.toList());
//            Map<String, Object> map = new LinkedHashMap<>();
//            map.put("用户名", userDTO.getUsername());
//            map.put("角色", roles);
//            map.put("部门", userDTO.getDept().getName());
//            map.put("岗位", userDTO.getJobs().stream().map(JobSmallDto::getName).collect(Collectors.toList()));
//            map.put("邮箱", userDTO.getEmail());
//            map.put("状态", userDTO.getEnabled() ? "启用" : "禁用");
//            map.put("手机号码", userDTO.getPhone());
//            map.put("修改密码的时间", userDTO.getPwdResetTime());
//            map.put("创建日期", userDTO.getCreateTime());
//            list.add(map);
//        }
//        FileUtil.downloadExcel(list, response);
//    }
//
//    /**
//     * 清理缓存
//     *
//     * @param id /
//     */
//    public void delCaches(Long id, String username) {
//        redisUtils.del(CacheKey.USER_ID + id);
//        redisUtils.del(CacheKey.USER_NAME + username);
//        flushCache(username);
//    }
//
//    /**
//     * 清理 登陆时 用户缓存信息
//     *
//     * @param username /
//     */
//    private void flushCache(String username) {
//        userCacheClean.cleanUserCache(username);
//    }
}
