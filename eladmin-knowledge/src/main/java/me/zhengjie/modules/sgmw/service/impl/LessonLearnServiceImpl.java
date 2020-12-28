package me.zhengjie.modules.sgmw.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.modules.sgmw.domain.BussLogic;
import me.zhengjie.modules.sgmw.domain.LessonLearn;
import me.zhengjie.modules.sgmw.repository.LessonLearnRepository;
import me.zhengjie.modules.sgmw.service.LessonLearnService;
import me.zhengjie.modules.sgmw.service.dto.BussLogicDto;
import me.zhengjie.modules.sgmw.service.dto.LessonLearnDto;
import me.zhengjie.modules.sgmw.service.dto.LessonLearnQueryCriteria;
import me.zhengjie.modules.sgmw.service.mapstruct.LessonLearnMapper;
import me.zhengjie.utils.*;
import me.zhengjie.utils.enums.DataScopeEnum;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @auther: wukenan
 * @date: 2020/11/5
 * @description:
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "lessonLearn")
public class LessonLearnServiceImpl implements LessonLearnService {

    public final LessonLearnRepository lessonLearnRepository;
    public final LessonLearnMapper lessonLearnMapper;
    public RedisUtils redisUtils;
    private final FileProperties properties;

    @Override
    public List<LessonLearnDto> queryAll(LessonLearnQueryCriteria criteria, Boolean isQuery) throws Exception {
        Sort sort = new Sort(Sort.Direction.ASC,"knowsSort");
        String dataScopeType = SecurityUtils.getDataScopeType();
        if (isQuery){
            if (dataScopeType.equals(DataScopeEnum.ALL.getValue())){
                criteria.setPidIsNull(true);
            }
            List<Field> fields = QueryHelp.getAllFields(criteria.getClass(),new ArrayList<>());
            List<String> fieldNames = new ArrayList<String>(){{ add("pidIsNull");add("enabled");}};
            for (Field field : fields) {
                //设置对象的访问权限，保证对private的属性的访问
                field.setAccessible(true);
                Object val = field.get(criteria);
                if(fieldNames.contains(field.getName())){
                    continue;
                }
                if (ObjectUtil.isNotNull(val)) {
                    criteria.setPidIsNull(null);
                    break;
                }
            }
        }
        List<LessonLearn> data = lessonLearnRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), sort);
        List<LessonLearnDto> list = lessonLearnMapper.toDto(data);
        // 如果为空，就代表为自定义权限或者本级权限，就需要去重，不理解可以注释掉，看查询结果
        if(StringUtils.isAllBlank(dataScopeType)){
            return deduplication(list);
        };
        return list;
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    public LessonLearnDto findById(Long id) {
        LessonLearn lessonLearn = lessonLearnRepository.findById(id).orElseGet(LessonLearn::new);
        ValidationUtil.isNull(lessonLearn.getId(),"LessonLearn","id",id);
        return lessonLearnMapper.toDto(lessonLearn);
    }

    @Override
    @Cacheable(key = "'pid:' + #p0")
    public List<LessonLearn> findByPid(Long pid) {
        return lessonLearnRepository.findByPid(pid);
    }

    @Override
    public List<LessonLearnDto> getSuperior(LessonLearnDto lessonLearnDto, List<LessonLearn> lessonLearns) {
        if(lessonLearnDto.getPid() == null){
            lessonLearns.addAll(lessonLearnRepository.findByPidIsNull());
            return lessonLearnMapper.toDto(lessonLearns);
        }
        lessonLearns.addAll(lessonLearnRepository.findByPid(lessonLearnDto.getPid()));
        return getSuperior(findById(lessonLearnDto.getPid()), lessonLearns);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(LessonLearn resources, MultipartFile multipartFile) {
        if (multipartFile == null){
            lessonLearnRepository.save(resources);
        }else {
            FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
            String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
            String type = FileUtil.getFileType(suffix);
            File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
            if (ObjectUtil.isNull(file)) {
                throw new BadRequestException("上传失败");
            }
            try {
                String docName = StringUtils.isBlank(resources.getDocName()) ? FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename()) : resources.getDocName();
                resources.setDocName(docName);
                resources.setDocRealName(file.getName());
                resources.setDocSuffix(suffix);
                resources.setDocDir(file.getPath());
                resources.setDocType(type);
                resources.setDocSize(FileUtil.getSize(multipartFile.getSize()));
                lessonLearnRepository.save(resources);
                // redisUtils.del("bussLogic::pid:" + (resources.getPid() == null ? 0 : resources.getPid()));
            } catch (Exception e) {
                FileUtil.del(file);
                throw e;
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(LessonLearn resources) {
        lessonLearnRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(LessonLearn resources, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type +  File.separator);
        if(ObjectUtil.isNull(file)){
            throw new BadRequestException("上传失败");
        }
        try {
            LessonLearn lessonLearn = lessonLearnRepository.findById(resources.getId()).orElseGet(LessonLearn::new);
            ValidationUtil.isNull( lessonLearn.getId(),"LessonLearn","id",resources.getId());
            resources.setId(lessonLearn.getId());
            String docName = StringUtils.isBlank(resources.getDocName()) ? FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename()) : resources.getDocName();
            resources.setDocName(docName);
            resources.setDocRealName(file.getName());
            resources.setDocSuffix(suffix);
            resources.setDocDir(file.getPath());
            resources.setDocType(type);
            resources.setDocSize(FileUtil.getSize(multipartFile.getSize()));
            lessonLearnRepository.save(resources);
            // 清理缓存
            // delCaches(resources.getId(), oldPid, newPid);
        }catch (Exception e){
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(LessonLearn resources) {
        lessonLearnRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<LessonLearnDto> lessonLearnDtos) {
        for(LessonLearnDto lessonLearnDto:lessonLearnDtos){
            // delCaches(deptDto.getId(), deptDto.getPid(), null);
            lessonLearnRepository.deleteById(lessonLearnDto.getId());
            updateSubCnt(lessonLearnDto.getPid());
        }
    }

    @Override
    public Set<LessonLearnDto> getDeleteLessonLearns(List<LessonLearn> menuList, Set<LessonLearnDto> lessonLearnDtos) {
        for (LessonLearn lessonLearn : menuList) {
            lessonLearnDtos.add(lessonLearnMapper.toDto(lessonLearn));
            List<LessonLearn> lessonLearns = lessonLearnRepository.findByPid(lessonLearn.getId());
            if(lessonLearns!=null && lessonLearns.size()!=0){
                getDeleteLessonLearns(lessonLearns, lessonLearnDtos);
            }
        }
        return lessonLearnDtos;
    }

    @Override
    public Object buildTree(List<LessonLearnDto> lessonLearnDtos) {
        return null;
    }

    @Override
    public void verification(Set<LessonLearnDto> lessonLearnDtos) {

    }

    private void updateSubCnt(Long lessonLearnId){
        if(lessonLearnId != null){
            int count = lessonLearnRepository.countByPid(lessonLearnId);
            lessonLearnRepository.updateSubCntByID(count,lessonLearnId);
        }
    }


    @Override
    public List<Long> getLessonLearnChildren(Long id, List<LessonLearn> lessonLearnList) {
        List<Long> list = new ArrayList<>();
        lessonLearnList.forEach(lessonLearn -> {
                    if (lessonLearn!=null && lessonLearn.getEnabled()){
                        List<LessonLearn> lessonLearns = lessonLearnRepository.findByPid(lessonLearn.getId());
                        if(lessonLearnList.size() != 0){
                            list.addAll(getLessonLearnChildren(lessonLearn.getId(), lessonLearns));
                        }
                        list.add(lessonLearn.getId());
                    }
                }
        );
        return list;
    }

    private List<LessonLearnDto> deduplication(List<LessonLearnDto> list) {
        List<LessonLearnDto> lessonLearnDtos = new ArrayList<>();
        for (LessonLearnDto lessonLearnDto : list) {
            boolean flag = true;
            for (LessonLearnDto dto : list) {
                if (lessonLearnDto.getPid().equals(dto.getId())) {
                    flag = false;
                    break;
                }
            }
            if (flag){
                lessonLearnDtos.add(lessonLearnDto);
            }
        }
        return lessonLearnDtos;
    }
}
