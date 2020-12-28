package me.zhengjie.modules.sgmw.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.modules.sgmw.domain.OverviewMain;
import me.zhengjie.modules.sgmw.repository.OverviewMainRepository;
import me.zhengjie.modules.sgmw.service.OverviewMainService;
import me.zhengjie.modules.sgmw.service.dto.OverviewMainDto;
import me.zhengjie.modules.sgmw.service.mapstruct.OverviewMainMapper;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @auther: wukenan
 * @date: 2020/11/23
 * @description:
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "overviewmain")
public class OverviewMainImpl implements OverviewMainService {

    private final OverviewMainRepository overviewMainRepository;
    public final OverviewMainMapper overviewMainMapper;
    
    @Override
    public OverviewMainDto findById(Long id) {
        OverviewMain overviewMain = overviewMainRepository.findById(id).orElseGet(OverviewMain::new);
        return overviewMainMapper.toDto(overviewMain);
    }

    @Override
    public OverviewMainDto findBySysId(String sysId) {
        OverviewMain overviewMain = overviewMainRepository.findBySysId(sysId);
        return overviewMainMapper.toDto(overviewMain);
    }

    @Override
    public OverviewMainDto findBySysName(String sysName) {
        OverviewMain overviewMain = overviewMainRepository.findBySysName(sysName);
        return overviewMainMapper.toDto(overviewMain);
    }

    @Override
    public List<OverviewMainDto> findByChangeId(String changeId) {
        return null;
    }

    @Override
    public void create(OverviewMain resources) {
        overviewMainRepository.save(resources);
    }

    @Override
    public void update(OverviewMain resources) {
        overviewMainRepository.save(resources);
    }
}
