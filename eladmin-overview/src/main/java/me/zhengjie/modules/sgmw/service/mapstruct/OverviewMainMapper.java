package me.zhengjie.modules.sgmw.service.mapstruct;

import me.zhengjie.base.BaseMapper;
import me.zhengjie.modules.sgmw.domain.OverviewMain;
import me.zhengjie.modules.sgmw.service.dto.OverviewMainDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OverviewMainMapper extends BaseMapper<OverviewMainDto, OverviewMain> {
}
