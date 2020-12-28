package me.zhengjie.modules.sgmw.service.mapstruct;

import me.zhengjie.base.BaseMapper;
import me.zhengjie.modules.sgmw.domain.NewEmpTrn;
import me.zhengjie.modules.sgmw.service.dto.NewEmpTrnDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NewEmpTrnMapper extends BaseMapper<NewEmpTrnDto, NewEmpTrn> {
}
