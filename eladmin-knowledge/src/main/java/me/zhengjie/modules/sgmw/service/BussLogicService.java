package me.zhengjie.modules.sgmw.service;

import me.zhengjie.modules.sgmw.domain.BussLogic;
import me.zhengjie.modules.sgmw.service.dto.BussLogicDto;
import me.zhengjie.modules.sgmw.service.dto.BussLogicQueryCriteria;
import org.mapstruct.Mapper;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface BussLogicService {
    /**
     * create by: wukenan
     * description: 查询所有满足查询条件的NewEmpTrnDto对象
     * create time: 2020/8/28 2:28 下午
     * @Param: null
     * @return
     */
    List<BussLogicDto> queryAll(BussLogicQueryCriteria criteria, Boolean isQuery) throws Exception;

    /**
     * create by: wukenan
     * description: 根据id查询NewEmpTrnDto对象
     * create time: 2020/8/28 2:30 下午
     * @Param: null
     * @return
     */
    BussLogicDto findById(Long id);

    List<BussLogic> findByPid(Long pid);

    List<BussLogicDto> getSuperior(BussLogicDto bussLogicDto, List<BussLogic> bussLogics);

    void create(BussLogic resources, MultipartFile multipartFile);

    void update(BussLogic resources, MultipartFile multipartFile);

    void update(BussLogic resources);

    void delete(Set<BussLogicDto> bussLogicDtos);

    Set<BussLogicDto> getDeleteBussLogics(List<BussLogic> bussLogicList, Set<BussLogicDto> bussLogicDtos);

    Object buildTree(List<BussLogicDto> bussLogicDtos);

    /**
     * 验证是否被角色或用户关联
     * @param /
     */
    void verification(Set<BussLogicDto> bussLogicDtos);

    List<Long> getBussLogicChildren(Long id,List<BussLogic> bussLogicList);

}
