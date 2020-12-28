package me.zhengjie.modules.sgmw.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.modules.sgmw.domain.AreaSystem;
import me.zhengjie.modules.sgmw.domain.BussLogic;
import me.zhengjie.modules.sgmw.repository.AreaSystemRepository;
import me.zhengjie.modules.sgmw.service.AreaSystemService;
import me.zhengjie.modules.sgmw.service.dto.AreaSystemDto;
import me.zhengjie.modules.sgmw.service.dto.AreaSystemQueryCriteria;
import me.zhengjie.modules.sgmw.service.dto.BussLogicDto;
import me.zhengjie.modules.sgmw.service.mapstruct.AreaSystemMapper;
import me.zhengjie.utils.*;
import me.zhengjie.utils.enums.DataScopeEnum;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @auther: wukenan
 * @date: 2020/11/12
 * @description:
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "areaSystem")
public class AreaSystemServiceImpl implements AreaSystemService {

    public final AreaSystemRepository areaSystemRepository;
    public final AreaSystemMapper areaSystemMapper;
    public RedisUtils redisUtils;

    @Override
    public List<AreaSystemDto> queryAll(AreaSystemQueryCriteria criteria, Boolean isQuery) throws Exception {
        Sort sort = new Sort(Sort.Direction.DESC,"id");
        return areaSystemMapper.toDto(areaSystemRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder),sort));
    }

    @Override
    public List<AreaSystem> findBySysName(String sysName) {
        List<AreaSystem> areaSystems = areaSystemRepository.findBySysNameLike(sysName);
        return areaSystems;
    }

    @Override
    public List<AreaSystem> findAll() {
        List<AreaSystem> areaSystems = areaSystemRepository.findAll();
        return areaSystems;
    }

    @Override
    public AreaSystem findBySysId(String sysId) {
        AreaSystem areaSystem = areaSystemRepository.findBySysId(sysId);
        return areaSystem;
    }

    @Override
    public void create(AreaSystem resources) {

    }

    @Override
    public void update(AreaSystem resources) {

    }

    @Override
    public void delete(Set<AreaSystemDto> AreaSystemDtos) {

    }

    @Override
    public Set<AreaSystemDto> getDeleteAreaSystems(List<AreaSystem> menuList, Set<AreaSystemDto> AreaSystemDtos) {
        return null;
    }

    @Override
    public void verification(Set<AreaSystemDto> AreaSystemDtos) {

    }

    @Override
    public void download(List<AreaSystemDto> areaSystemDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AreaSystemDto areaSystemDTO : areaSystemDtos) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("部门名称", areaSystemDTO.getSysName());
            map.put("部门状态", areaSystemDTO.getArea());
            map.put("创建日期", areaSystemDTO.getSysId());
            map.put("创建日期", areaSystemDTO.getSysEnglishName());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
