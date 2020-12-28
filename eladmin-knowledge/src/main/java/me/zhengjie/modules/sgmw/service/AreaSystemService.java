package me.zhengjie.modules.sgmw.service;

import me.zhengjie.modules.sgmw.domain.AreaSystem;
import me.zhengjie.modules.sgmw.service.dto.AreaSystemDto;
import me.zhengjie.modules.sgmw.service.dto.AreaSystemQueryCriteria;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface AreaSystemService {
    List<AreaSystemDto> queryAll(AreaSystemQueryCriteria criteria, Boolean isQuery) throws Exception;

    List<AreaSystem> findBySysName(String sysName);

    List<AreaSystem> findAll();

    AreaSystem findBySysId(String sysId);

    void create(AreaSystem resources);

    void update(AreaSystem resources);

    void delete(Set<AreaSystemDto> AreaSystemDtos);

    Set<AreaSystemDto> getDeleteAreaSystems(List<AreaSystem> menuList, Set<AreaSystemDto> AreaSystemDtos);

    void verification(Set<AreaSystemDto> AreaSystemDtos);

    void download(List<AreaSystemDto> queryAll, HttpServletResponse response) throws IOException;
}
