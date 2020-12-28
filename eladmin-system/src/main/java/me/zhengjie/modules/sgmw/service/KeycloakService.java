package me.zhengjie.modules.sgmw.service;

import me.zhengjie.modules.sgmw.service.dto.KeycloakAccountDTO;
import me.zhengjie.modules.sgmw.service.dto.KeycloakAccountQueryCriteria;
import me.zhengjie.modules.sgmw.service.dto.UsernameIdMappingDTO;
import me.zhengjie.utils.result.Page;
import me.zhengjie.utils.result.Result;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

public interface KeycloakService {



    Result<KeycloakAccountDTO> getCurrentKeycloakAccount();

    Result<Page<KeycloakAccountDTO>> selectPageUsers(KeycloakAccountQueryCriteria criteria, Pageable pageable);

    Result<Void> importKeycloakAccount(Collection<UsernameIdMappingDTO> mappings);

}
