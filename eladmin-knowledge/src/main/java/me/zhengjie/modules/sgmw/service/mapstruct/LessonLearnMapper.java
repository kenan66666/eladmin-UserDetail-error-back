package me.zhengjie.modules.sgmw.service.mapstruct;

import me.zhengjie.base.BaseMapper;
import me.zhengjie.modules.sgmw.domain.LessonLearn;
import me.zhengjie.modules.sgmw.service.dto.LessonLearnDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LessonLearnMapper extends BaseMapper<LessonLearnDto, LessonLearn> {
}
