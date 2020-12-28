package me.zhengjie.modules.sgmw.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.modules.security.security.KeycloakWrapper;
import me.zhengjie.modules.sgmw.service.KeycloakService;
import me.zhengjie.modules.sgmw.service.dto.KeycloakAccountDTO;
import me.zhengjie.modules.sgmw.service.dto.KeycloakAccountQueryCriteria;
import me.zhengjie.modules.sgmw.service.dto.UsernameIdMappingDTO;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.utils.SecurityUtils;
import me.zhengjie.utils.result.Page;
import me.zhengjie.utils.result.Result;
import me.zhengjie.utils.result.ResultEnum;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private final KeycloakWrapper keycloakWrapper;
    private final UserService userService;

    @Override
    public Result<KeycloakAccountDTO> getCurrentKeycloakAccount() {
        final String username = SecurityUtils.getUsername();
        KeycloakAccountQueryCriteria criteria = new KeycloakAccountQueryCriteria();
        criteria.setUsername(username);
        final Result<Page<KeycloakAccountDTO>> pageResult = selectPageUsers(criteria, null);
        if (pageResult.isNotSuccess()) {
            return Result.copyMessage(pageResult);
        }

        final Page<KeycloakAccountDTO> page = pageResult.getData();
        final List<KeycloakAccountDTO> rows = page.getRows();
        if (rows.isEmpty()) {
            return new Result<>(ResultEnum.PARAMS_ERROR);
        }

        final Optional<KeycloakAccountDTO> opt = rows.stream().filter(e -> username.equals(e.getUsername())).findFirst();
        return opt.map(Result::new).orElseGet(() -> new Result<>(ResultEnum.PARAMS_ERROR));
    }

    @Override
    public Result<Page<KeycloakAccountDTO>> selectPageUsers(KeycloakAccountQueryCriteria criteria, Pageable pageable) {

        pageable = Optional.ofNullable(pageable).orElseGet(() -> PageRequest.of(0, 20));
        final int first = Long.valueOf(pageable.getOffset()).intValue();
        final int max = Long.valueOf(pageable.getPageSize()).intValue();

        final List<KeycloakAccountDTO> rows = new LinkedList<>();
        final List<UserRepresentation> list;
        Integer count;
        try {
            final UsersResource usersResource = getUsersResource();

            if (criteria.hasValue()) {
                if (criteria.onlySearchHasValue()) {
                    list = usersResource.search(criteria.getSearch(), first, max);
                    count = usersResource.count(criteria.getSearch());
                } else {
                    list = usersResource.search(criteria.getUsername(), criteria.getFirstName(), criteria.getLastName(), criteria.getEmail(), first, max);
                    count = usersResource.count(criteria.getLastName(), criteria.getFirstName(), criteria.getEmail(), criteria.getUsername());
                }
            } else {
                list = usersResource.list(first, max);
                count = usersResource.count();
            }

            for (UserRepresentation userRepresentation : list) {
                rows.add(buildDTO(userRepresentation));
            }
        } catch (Exception e) {
            return new Result<>(ResultEnum.FAILED);
        }

        if (!CollectionUtils.isEmpty(rows)) {
            final Set<String> usernameSet = rows.stream().map(KeycloakAccountDTO::getUsername).collect(Collectors.toSet());
            final Set<String> userExistState = Optional.ofNullable(userService.selectUserExistState(usernameSet)).orElseGet(HashSet::new);
            rows.forEach(row -> row.setExist(userExistState.contains(row.getUsername())));
        }

        return new Result<>(Page.<KeycloakAccountDTO>builder()
                .rows(rows)
                .pageNum(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(count)
                .totalPages((count % pageable.getPageSize() == 0 ? count / pageable.getPageSize() : count / pageable.getPageSize() + 1))
                .build());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Void> importKeycloakAccount(Collection<UsernameIdMappingDTO> mappings) {
        if (CollectionUtils.isEmpty(mappings)) {
            return new Result<>();
        }
        final List<KeycloakAccountDTO> importList = new LinkedList<>();
        final UsersResource usersResource = getUsersResource();
        for (UsernameIdMappingDTO mapping : mappings) {
            String username = mapping.getUsername();
            String id = mapping.getId();
            if (!StringUtils.hasText(username) || !StringUtils.hasText(id)) {
                continue;
            }
            username = username.trim();
            id = id.trim();
            final List<UserRepresentation> list = usersResource.search(username.trim());
            if (CollectionUtils.isEmpty(list)) {
                continue;
            }

            for (UserRepresentation userRepresentation : list) {
                if (userRepresentation == null) continue;
                final KeycloakAccountDTO dto = buildDTO(userRepresentation);
                if (id.equals(dto.getId())) {
                    importList.add(dto);
                }
            }
        }

        if (CollectionUtils.isEmpty(importList)) {
            return new Result<>();
        }

        // 执行导入
        for (KeycloakAccountDTO keycloakAccountDTO : importList) {
            userService.mergeUser(keycloakAccountDTO);
        }

        return new Result<>(ResultEnum.SUCCESS);
    }

    private Keycloak getKeycloak() {
        return keycloakWrapper.getKeycloak();
    }

    private UsersResource getUsersResource() {
        return getKeycloak().realm(keycloakWrapper.getRealm()).users();
    }

    private Map<String, List<String>> keyUpperCase(Map<String, List<String>> attributes) {
        Map<String, List<String>> newMap = new HashMap<>();
        if (attributes == null)
            return newMap;
        final Set<Map.Entry<String, List<String>>> entries = attributes.entrySet();
        for (Map.Entry<String, List<String>> entry : entries) {
            String key = entry.getKey();
            if (key != null) {
                key = key.toUpperCase(Locale.ENGLISH);
            }
            newMap.put(key, entry.getValue());
        }

        return newMap;
    }


    private KeycloakAccountDTO buildDTO(UserRepresentation userRepresentation) {
        final String id = userRepresentation.getId();
        final String username = userRepresentation.getUsername();
        final String email = userRepresentation.getEmail();
        final String firstName = userRepresentation.getFirstName();
        final String lastName = userRepresentation.getLastName();

        final Map<String, List<String>> attributes = keyUpperCase(userRepresentation.getAttributes());
        final List<String> departmentNameList = attributes.get("DEPARTMENT");
        final List<String> titleList = attributes.get("TITLE");
        final List<String> displayNameList = attributes.get("DISPLAYNAME");
        final List<String> fullNameList = attributes.get("FULLNAME");
        final List<String> homeTelephoneList = attributes.get("HOMETELEPHONE");
        final List<String> mobileTelephoneList = attributes.get("MOBILETELEPHONE");

        final String departmentName = departmentNameList == null ? null : String.join(",", departmentNameList);
        final String title = titleList == null ? null : String.join(",", titleList);
        final String displayName = displayNameList == null ? null : String.join(",", displayNameList);
        final String fullName = fullNameList == null ? null : String.join(",", fullNameList);
        final String homeTelephone = homeTelephoneList == null ? null : String.join(",", homeTelephoneList);
        final String mobileTelephone = mobileTelephoneList == null ? null : String.join(",", mobileTelephoneList);

        return KeycloakAccountDTO.builder()
                .id(id)
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .departmentName(departmentName)
                .title(title)
                .displayName(displayName)
                .fullName(fullName)
                .homeTelephone(homeTelephone)
                .mobileTelephone(mobileTelephone).build();
    }
}
