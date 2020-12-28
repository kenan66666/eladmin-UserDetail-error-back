package me.zhengjie.modules.sgmw.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.modules.sgmw.domain.BussLogic;
import me.zhengjie.modules.sgmw.domain.NewEmpTrn;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.modules.sgmw.repository.NewEmpTrnRepository;
//import me.zhengjie.modules.system.repository.RoleRepository;
//import me.zhengjie.modules.system.repository.UserRepository;
import me.zhengjie.modules.sgmw.service.NewEmpTrnService;
import me.zhengjie.modules.sgmw.service.dto.NewEmpTrnDto;
import me.zhengjie.modules.sgmw.service.dto.NewEmpTrnQueryCriteria;
import me.zhengjie.modules.sgmw.service.mapstruct.NewEmpTrnMapper;
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


@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "newEmpTrn")
public class NewEmpTrnServiceImpl implements NewEmpTrnService {

    public final NewEmpTrnRepository newEmpTrnRepository;
    public final NewEmpTrnMapper newEmpTrnMapper;
//    public final UserRepository userRepository;
    public final RedisUtils redisUtils;
//    public final RoleRepository roleRepository;
    private final FileProperties properties;

    @Override
    public List<NewEmpTrnDto> queryAll(NewEmpTrnQueryCriteria criteria, Boolean isQuery) throws Exception {
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
        List<NewEmpTrn> data = newEmpTrnRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), sort);
        List<NewEmpTrnDto> list = newEmpTrnMapper.toDto(data);
        // 如果为空，就代表为自定义权限或者本级权限，就需要去重，不理解可以注释掉，看查询结果
        if(StringUtils.isAllBlank(dataScopeType)){
            return deduplication(list);
        };
        return list;
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    public NewEmpTrnDto findById(Long id) {
        NewEmpTrn newEmpTrn = newEmpTrnRepository.findById(id).orElseGet(NewEmpTrn::new);
        ValidationUtil.isNull(newEmpTrn.getId(),"NewEmpTrn","id",id);
        return newEmpTrnMapper.toDto(newEmpTrn);
    }

    @Override
    @Cacheable(key = "'pid:' + #p0")
    public List<NewEmpTrn> findByPid(Long pid) {
        return newEmpTrnRepository.findByPid(pid);
    }

    @Override
    public List<NewEmpTrnDto> getSuperior(NewEmpTrnDto newEmpTrnDto, List<NewEmpTrn> newEmpTrns) {
        if(newEmpTrnDto.getPid() == null){
            newEmpTrns.addAll(newEmpTrnRepository.findByPidIsNull());
            return newEmpTrnMapper.toDto(newEmpTrns);
        }
        newEmpTrns.addAll(newEmpTrnRepository.findByPid(newEmpTrnDto.getPid()));
        return getSuperior(findById(newEmpTrnDto.getPid()), newEmpTrns);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(NewEmpTrn resources, MultipartFile multipartFile) {
        if (multipartFile == null){
            newEmpTrnRepository.save(resources);
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
                newEmpTrnRepository.save(resources);
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
    public void update(NewEmpTrn resources) {
        Long oldPid = findById(resources.getId()).getPid();
        Long newPid = resources.getPid();
        if(resources.getPid() != null && resources.getId().equals(resources.getPid())) {
            throw new BadRequestException("上级不能为自己");
        }
        NewEmpTrn newEmpTrn = newEmpTrnRepository.findById(resources.getId()).orElseGet(NewEmpTrn::new);
        ValidationUtil.isNull( newEmpTrn.getId(),"NewEmpTrn","id",resources.getId());
        resources.setId(newEmpTrn.getId());
        newEmpTrnRepository.save(resources);
        // 更新父节点中子节点数目
        updateSubCnt(oldPid);
        updateSubCnt(newPid);
        // 清理缓存
        // delCaches(resources.getId(), oldPid, newPid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(NewEmpTrn resources, MultipartFile multipartFile){
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
            NewEmpTrn newEmpTrn = newEmpTrnRepository.findById(resources.getId()).orElseGet(NewEmpTrn::new);
            ValidationUtil.isNull( newEmpTrn.getId(),"NewEmpTrn","id",resources.getId());
            resources.setId(newEmpTrn.getId());
            String docName = StringUtils.isBlank(resources.getDocName()) ? FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename()) : resources.getDocName();
            resources.setDocName(docName);
            resources.setDocRealName(file.getName());
            resources.setDocSuffix(suffix);
            resources.setDocDir(file.getPath());
            resources.setDocType(type);
            resources.setDocSize(FileUtil.getSize(multipartFile.getSize()));
            newEmpTrnRepository.save(resources);
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
    public void delete(Set<NewEmpTrnDto> newEmpTrnDtos) {
        for(NewEmpTrnDto newEmpTrnDto:newEmpTrnDtos){
            // delCaches(deptDto.getId(), deptDto.getPid(), null);
            newEmpTrnRepository.deleteById(newEmpTrnDto.getId());
            updateSubCnt(newEmpTrnDto.getPid());
        }
    }

    @Override
    public Set<NewEmpTrnDto> getDeleteNewEmpTrns(List<NewEmpTrn> menuList, Set<NewEmpTrnDto> newEmpTrnDtos) {
        for (NewEmpTrn newEmpTrn : menuList) {
            newEmpTrnDtos.add(newEmpTrnMapper.toDto(newEmpTrn));
            List<NewEmpTrn> newEmpTrns = newEmpTrnRepository.findByPid(newEmpTrn.getId());
            if(newEmpTrns!=null && newEmpTrns.size()!=0){
                getDeleteNewEmpTrns(newEmpTrns, newEmpTrnDtos);
            }
        }
        return newEmpTrnDtos;
    }


    @Override
    public Object buildTree(List<NewEmpTrnDto> newEmpTrnDtos) {
        Set<NewEmpTrnDto> trees = new LinkedHashSet<>();
        Set<NewEmpTrnDto> newEmpTrns= new LinkedHashSet<>();
        List<String> newEmpTrnNames = newEmpTrnDtos.stream().map(NewEmpTrnDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (NewEmpTrnDto newEmpTrnDTO : newEmpTrnDtos) {
            isChild = false;
            if (newEmpTrnDTO.getPid() == null) {
                trees.add(newEmpTrnDTO);
            }
            for (NewEmpTrnDto it : newEmpTrnDtos) {
                if (it.getPid() != null && newEmpTrnDTO.getId().equals(it.getPid())) {
                    isChild = true;
                    if (newEmpTrnDTO.getChildren() == null) {
                        newEmpTrnDTO.setChildren(new ArrayList<>());
                    }
                    newEmpTrnDTO.getChildren().add(it);
                }
            }
            if(isChild) {
                newEmpTrns.add(newEmpTrnDTO);
            } else if(newEmpTrnDTO.getPid() != null &&  !newEmpTrnNames.contains(findById(newEmpTrnDTO.getPid()).getName())) {
                newEmpTrns.add(newEmpTrnDTO);
            }
        }

        if (CollectionUtil.isEmpty(trees)) {
            trees = newEmpTrns;
        }
        Map<String,Object> map = new HashMap<>(2);
        map.put("totalElements",newEmpTrnDtos.size());
        map.put("content",CollectionUtil.isEmpty(trees)? newEmpTrnDtos :trees);
        return map;
    }

    @Override
    public void verification(Set<NewEmpTrnDto> newEmpTrnDtos) {
        Set<Long> newEmpTrnIds = newEmpTrnDtos.stream().map(NewEmpTrnDto::getId).collect(Collectors.toSet());
//        if(userRepository.countByDepts(newEmpTrnIds) > 0){
//            throw new BadRequestException("所选部门存在用户关联，请解除后再试！");
//        }
//        if(roleRepository.countByDepts(newEmpTrnIds) > 0){
//            throw new BadRequestException("所选部门存在角色关联，请解除后再试！");
//        }
    }

    @Override
    public List<Long> getNewEmpTrnChildren(Long id, List<NewEmpTrn> newEmpTrnList) {
        List<Long> list = new ArrayList<>();
        newEmpTrnList.forEach(newEmpTrn -> {
                    if (newEmpTrn!=null && newEmpTrn.getEnabled()){
                        List<NewEmpTrn> newEmpTrns = newEmpTrnRepository.findByPid(newEmpTrn.getId());
                        if(newEmpTrnList.size() != 0){
                            list.addAll(getNewEmpTrnChildren(newEmpTrn.getId(), newEmpTrns));
                        }
                        list.add(newEmpTrn.getId());
                    }
                }
        );
        return list;
    }


    private List<NewEmpTrnDto> deduplication(List<NewEmpTrnDto> list) {
        List<NewEmpTrnDto> newEmpTrnDtos = new ArrayList<>();
        for (NewEmpTrnDto newEmpTrnDto : list) {
            boolean flag = true;
            for (NewEmpTrnDto dto : list) {
                if (newEmpTrnDto.getPid().equals(dto.getId())) {
                    flag = false;
                    break;
                }
            }
            if (flag){
                newEmpTrnDtos.add(newEmpTrnDto);
            }
        }
        return newEmpTrnDtos;
    }

    private void updateSubCnt(Long newEmpTrnId){
        if(newEmpTrnId != null){
            int count = newEmpTrnRepository.countByPid(newEmpTrnId);
            newEmpTrnRepository.updateSubCntByID(count,newEmpTrnId);
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
