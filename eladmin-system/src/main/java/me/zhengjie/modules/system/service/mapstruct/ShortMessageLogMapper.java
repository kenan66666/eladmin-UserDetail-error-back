package me.zhengjie.modules.system.service.mapstruct;

import me.zhengjie.base.BaseMapper;
import me.zhengjie.modules.system.domain.ShortMessageLog;
import me.zhengjie.modules.system.service.dto.ShortMessageLogDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
* @author Kermit
* @date 2020-01-13
*/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShortMessageLogMapper extends BaseMapper<ShortMessageLogDto, ShortMessageLog> {

}