package me.zhengjie.modules.sgmw.service.mapstruct;

import me.zhengjie.base.BaseMapper;
import me.zhengjie.modules.sgmw.domain.BussLogic;
import me.zhengjie.modules.sgmw.service.dto.BussLogicDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BussLogicMapper extends BaseMapper<BussLogicDto, BussLogic> {
}
