package me.zhengjie.modules.sgmw.service;

import me.zhengjie.modules.sgmw.domain.OverviewMain;
import me.zhengjie.modules.sgmw.service.dto.AreaSystemDto;
import me.zhengjie.modules.sgmw.service.dto.OverviewMainDto;

import java.util.List;

public interface OverviewMainService {
    OverviewMainDto findById(Long id);
    OverviewMainDto findBySysId(String sysId);
    OverviewMainDto findBySysName(String sysName);
    List<OverviewMainDto> findByChangeId(String changeId);
    void create(OverviewMain resources);
    void update(OverviewMain resources);
}
