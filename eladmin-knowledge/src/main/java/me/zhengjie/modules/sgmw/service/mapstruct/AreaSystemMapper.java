package me.zhengjie.modules.sgmw.service.mapstruct;

import me.zhengjie.base.BaseMapper;
import me.zhengjie.modules.sgmw.domain.AreaSystem;
import me.zhengjie.modules.sgmw.service.dto.AreaSystemDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AreaSystemMapper extends BaseMapper<AreaSystemDto, AreaSystem> {
}
