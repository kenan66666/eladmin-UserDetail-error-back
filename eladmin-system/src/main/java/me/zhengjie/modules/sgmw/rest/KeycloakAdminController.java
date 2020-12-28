package me.zhengjie.modules.sgmw.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.modules.sgmw.service.KeycloakService;
import me.zhengjie.modules.sgmw.service.dto.KeycloakAccountDTO;
import me.zhengjie.modules.sgmw.service.dto.KeycloakAccountQueryCriteria;
import me.zhengjie.modules.sgmw.service.dto.UsernameIdMappingDTO;
import me.zhengjie.utils.result.Page;
import me.zhengjie.utils.result.Result;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Api(tags = "Keycloak用户管理")
@RestController
@RequestMapping("/api/KeycloakAdminx")
@RequiredArgsConstructor
public class KeycloakAdminController {

    private final KeycloakService keycloakService;

    @Log("查询当前用户信息")
    @ApiOperation("查询当前用户信息")
    @GetMapping("/getCurrentKeycloakAccount")
    // @PreAuthorize("@el.check('KeycloakAdmin:getCurrentKeycloakAccount')")
    public Result<KeycloakAccountDTO> getCurrentKeycloakAccount() {
        return keycloakService.getCurrentKeycloakAccount();
    }

    @Log("查询用户列表")
    @ApiOperation("查询用户列表")
    @GetMapping("/selectPageUsers")
    @PreAuthorize("@el.check('KeycloakAdmin:selectPageUsers')")
    public Result<Page<KeycloakAccountDTO>> selectPageUsers(KeycloakAccountQueryCriteria criteria, Pageable pageable) {
        return keycloakService.selectPageUsers(criteria, pageable);
    }

    @Log("导入用户列表")
    @ApiOperation("导入用户列表")
    @PostMapping("/importKeycloakAccount")
    @PreAuthorize("@el.check('KeycloakAdmin:importKeycloakAccount')")
    public Result<Void> importKeycloakAccountUsernameIdMappingDTO(@RequestBody Collection<UsernameIdMappingDTO> mappings) {
        return keycloakService.importKeycloakAccount(mappings);
    }


}
