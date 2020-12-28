package me.zhengjie.modules.sgmw.service;

import me.zhengjie.modules.sgmw.domain.NewEmpTrn;
import me.zhengjie.modules.sgmw.service.dto.NewEmpTrnDto;
import me.zhengjie.modules.sgmw.service.dto.NewEmpTrnQueryCriteria;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface NewEmpTrnService {
    /**
     * create by: wukenan
     * description: 查询所有满足查询条件的NewEmpTrnDto对象
     * create time: 2020/8/28 2:28 下午
      * @Param: null
     * @return
     */
    List<NewEmpTrnDto> queryAll(NewEmpTrnQueryCriteria criteria, Boolean isQuery) throws Exception;

    /**
     * create by: wukenan
     * description: 根据id查询NewEmpTrnDto对象
     * create time: 2020/8/28 2:30 下午
      * @Param: null
     * @return
     */
    NewEmpTrnDto findById(Long id);

    List<NewEmpTrn> findByPid(Long pid);

    List<NewEmpTrnDto> getSuperior(NewEmpTrnDto newEmpTrnDto,List<NewEmpTrn> newEmpTrns);

    void create(NewEmpTrn resources, MultipartFile multipartFile);

    void update(NewEmpTrn resources, MultipartFile multipartFile);

    void update(NewEmpTrn resources);

    void delete(Set<NewEmpTrnDto> newEmpTrnDtos);

    Set<NewEmpTrnDto> getDeleteNewEmpTrns(List<NewEmpTrn> newEmpTrnList, Set<NewEmpTrnDto> newEmpTrnDtos);

    Object buildTree(List<NewEmpTrnDto> newEmpTrnDtos);

    /**
     * 验证是否被角色或用户关联
     * @param deptDtos /
     */
    void verification(Set<NewEmpTrnDto> newEmpTrnDtos);

    List<Long> getNewEmpTrnChildren(Long id,List<NewEmpTrn> newEmpTrnList);
}
