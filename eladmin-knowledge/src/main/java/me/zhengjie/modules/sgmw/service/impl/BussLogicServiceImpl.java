package me.zhengjie.modules.sgmw.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.LocalStorage;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.modules.sgmw.domain.BussLogic;
import me.zhengjie.modules.sgmw.repository.BussLogicRepository;
import me.zhengjie.modules.sgmw.service.BussLogicService;
import me.zhengjie.modules.sgmw.service.dto.BussLogicDto;
import me.zhengjie.modules.sgmw.service.dto.BussLogicQueryCriteria;
import me.zhengjie.modules.sgmw.service.mapstruct.BussLogicMapper;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * @auther: wukenan
 * @date: 2020/10/13
 * @description:
 */

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "bussLogic")
public class BussLogicServiceImpl implements BussLogicService {

    public final BussLogicRepository bussLogicRepository;
    public final BussLogicMapper bussLogicMapper;
    public RedisUtils redisUtils;
    private final FileProperties properties;


    @Override
    public List<BussLogicDto> queryAll(BussLogicQueryCriteria criteria, Boolean isQuery) throws Exception {
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
        List<BussLogic> data = bussLogicRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), sort);
        List<BussLogicDto> list = bussLogicMapper.toDto(data);
        // 如果为空，就代表为自定义权限或者本级权限，就需要去重，不理解可以注释掉，看查询结果
        if(StringUtils.isAllBlank(dataScopeType)){
            return deduplication(list);
        };
        return list;
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    public BussLogicDto findById(Long id) {
        BussLogic bussLogic = bussLogicRepository.findById(id).orElseGet(BussLogic::new);
        ValidationUtil.isNull(bussLogic.getId(),"BussLogic","id",id);
        return bussLogicMapper.toDto(bussLogic);
    }

    @Override
    @Cacheable(key = "'pid:' + #p0")
    public List<BussLogic> findByPid(Long pid) {
        return bussLogicRepository.findByPid(pid);
    }

    @Override
    public List<BussLogicDto> getSuperior(BussLogicDto bussLogicDto, List<BussLogic> bussLogics) {
        // 当循环到最顶层，已经没有父级的时候，在bussLogics里边，已经保存了所有的bussLogics对象，然后做一次Dto的Mapper，就成功了。
        if(bussLogicDto.getPid() == null){
            bussLogics.addAll(bussLogicRepository.findByPidIsNull());
            return bussLogicMapper.toDto(bussLogics);
        }
        bussLogics.addAll(bussLogicRepository.findByPid(bussLogicDto.getPid()));
        return getSuperior(findById(bussLogicDto.getPid()), bussLogics);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(BussLogic resources, MultipartFile multipartFile) {
        if (multipartFile == null){
            bussLogicRepository.save(resources);
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
                bussLogicRepository.save(resources);
                resources.setSubCount(0);
                // redisUtils.del("bussLogic::pid:" + (resources.getPid() == null ? 0 : resources.getPid()));
                updateSubCnt(resources.getPid());
            } catch (Exception e) {
                FileUtil.del(file);
                throw e;
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(BussLogic resources) {
        bussLogicRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(BussLogic resources, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type +  File.separator);
        if(ObjectUtil.isNull(file)){
            throw new BadRequestException("上传失败");
        }
        try {
            Long oldPid = findById(resources.getId()).getPid();
            Long newPid = resources.getPid();
            if(resources.getPid() != null && resources.getId().equals(resources.getPid())) {
                throw new BadRequestException("上级不能为自己");
            }
            BussLogic bussLogic = bussLogicRepository.findById(resources.getId()).orElseGet(BussLogic::new);
            ValidationUtil.isNull( bussLogic.getId(),"BussLogic","id",resources.getId());
            resources.setId(bussLogic.getId());
            String docName = StringUtils.isBlank(resources.getDocName()) ? FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename()) : resources.getDocName();
            resources.setDocName(docName);
            resources.setDocRealName(file.getName());
            resources.setDocSuffix(suffix);
            resources.setDocDir(file.getPath());
            resources.setDocType(type);
            resources.setDocSize(FileUtil.getSize(multipartFile.getSize()));
            bussLogicRepository.save(resources);
            // 更新父节点中子节点数目
            updateSubCnt(oldPid);
            updateSubCnt(newPid);
            // 清理缓存
            // delCaches(resources.getId(), oldPid, newPid);
        }catch (Exception e){
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<BussLogicDto> bussLogicDtos) {
        for(BussLogicDto bussLogicDto:bussLogicDtos){
            // delCaches(deptDto.getId(), deptDto.getPid(), null);
            bussLogicRepository.deleteById(bussLogicDto.getId());
            updateSubCnt(bussLogicDto.getPid());
        }
    }

    @Override
    public Set<BussLogicDto> getDeleteBussLogics(List<BussLogic> menuList, Set<BussLogicDto> bussLogicDtos) {
        for (BussLogic bussLogic : menuList) {
            bussLogicDtos.add(bussLogicMapper.toDto(bussLogic));
            List<BussLogic> bussLogics = bussLogicRepository.findByPid(bussLogic.getId());
            if(bussLogics!=null && bussLogics.size()!=0){
                getDeleteBussLogics(bussLogics, bussLogicDtos);
            }
        }
        return bussLogicDtos;
    }

    @Override
    public Object buildTree(List<BussLogicDto> bussLogicDtos) {
        Set<BussLogicDto> trees = new LinkedHashSet<>();
        Set<BussLogicDto> bussLogics= new LinkedHashSet<>();
        List<String> bussLogicNames = bussLogicDtos.stream().map(BussLogicDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (BussLogicDto bussLogicDTO : bussLogicDtos) {
            isChild = false;
            if (bussLogicDTO.getPid() == null) {
                trees.add(bussLogicDTO);
            }
            for (BussLogicDto it : bussLogicDtos) {
                if (it.getPid() != null && bussLogicDTO.getId().equals(it.getPid())) {
                    isChild = true;
                    if (bussLogicDTO.getChildren() == null) {
                        bussLogicDTO.setChildren(new ArrayList<>());
                    }
                    bussLogicDTO.getChildren().add(it);
                }
            }
            if(isChild) {
                bussLogics.add(bussLogicDTO);
            } else if(bussLogicDTO.getPid() != null &&  !bussLogicNames.contains(findById(bussLogicDTO.getPid()).getName())) {
                bussLogics.add(bussLogicDTO);
            }
        }

        if (CollectionUtil.isEmpty(trees)) {
            trees = bussLogics;
        }
        Map<String,Object> map = new HashMap<>(2);
        map.put("totalElements",bussLogicDtos.size());
        map.put("content",CollectionUtil.isEmpty(trees)? bussLogicDtos :trees);
        return map;
    }

    @Override
    public void verification(Set<BussLogicDto> bussLogicDtos) {
        Set<Long> bussLogicIds = bussLogicDtos.stream().map(BussLogicDto::getId).collect(Collectors.toSet());
//        if(userRepository.countByDepts(newEmpTrnIds) > 0){
//            throw new BadRequestException("所选部门存在用户关联，请解除后再试！");
//        }
//        if(roleRepository.countByDepts(newEmpTrnIds) > 0){
//            throw new BadRequestException("所选部门存在角色关联，请解除后再试！");
//        }
    }

    @Override
    public List<Long> getBussLogicChildren(Long id, List<BussLogic> bussLogicList) {
        List<Long> list = new ArrayList<>();
        bussLogicList.forEach(bussLogic -> {
                    if (bussLogic!=null && bussLogic.getEnabled()){
                        List<BussLogic> bussLogics = bussLogicRepository.findByPid(bussLogic.getId());
                        if(bussLogicList.size() != 0){
                            list.addAll(getBussLogicChildren(bussLogic.getId(), bussLogics));
                        }
                        list.add(bussLogic.getId());
                    }
                }
        );
        return list;
    }

    private List<BussLogicDto> deduplication(List<BussLogicDto> list) {
        List<BussLogicDto> bussLogicDtos = new ArrayList<>();
        for (BussLogicDto bussLogicDto : list) {
            boolean flag = true;
            for (BussLogicDto dto : list) {
                if (bussLogicDto.getPid().equals(dto.getId())) {
                    flag = false;
                    break;
                }
            }
            if (flag){
                bussLogicDtos.add(bussLogicDto);
            }
        }
        return bussLogicDtos;
    }

    private void updateSubCnt(Long bussLogicId){
        if(bussLogicId != null){
            int count = bussLogicRepository.countByPid(bussLogicId);
            bussLogicRepository.updateSubCntByID(count,bussLogicId);
        }
    }

    /**
     * 清理缓存
     * @param id /
     * @param oldPid /
     * @param newPid /
     */
    /*public void delCaches(Long id, Long oldPid, Long newPid){
        List<User> users = userRepository.findByDeptRoleId(id);
        // 删除数据权限
        redisUtils.delByKeys("data::user:",users.stream().map(User::getId).collect(Collectors.toSet()));
        redisUtils.del("dept::id:" + id);
        redisUtils.del("dept::pid:" + (oldPid == null ? 0 : oldPid));
        redisUtils.del("dept::pid:" + (newPid == null ? 0 : newPid));
}*/

}
