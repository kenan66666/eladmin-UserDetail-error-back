package me.zhengjie.modules.sgmw.service;

import me.zhengjie.modules.sgmw.domain.LessonLearn;
import me.zhengjie.modules.sgmw.service.dto.LessonLearnDto;
import me.zhengjie.modules.sgmw.service.dto.LessonLearnQueryCriteria;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface LessonLearnService {

    List<LessonLearnDto> queryAll(LessonLearnQueryCriteria criteria, Boolean isQuery) throws Exception;

    LessonLearnDto findById(Long id);

    List<LessonLearn> findByPid(Long pid);

    List<LessonLearnDto> getSuperior(LessonLearnDto lessonLearnDto, List<LessonLearn> lessonLearns);

    void create(LessonLearn resources, MultipartFile multipartFile);

    void create(LessonLearn resources);

    void update(LessonLearn resources, MultipartFile multipartFile);

    void update(LessonLearn resources);

    void delete(Set<LessonLearnDto> lessonLearnDtos);

    Set<LessonLearnDto> getDeleteLessonLearns(List<LessonLearn> menuList, Set<LessonLearnDto> lessonLearnDtos);

    Object buildTree(List<LessonLearnDto> lessonLearnDtos);

    void verification(Set<LessonLearnDto> lessonLearnDtos);

    List<Long> getLessonLearnChildren(Long id,List<LessonLearn> lessonLearnList);
}
