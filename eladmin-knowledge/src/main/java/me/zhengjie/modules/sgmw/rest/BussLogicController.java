package me.zhengjie.modules.sgmw.rest;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.base.BaseEntity;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.modules.sgmw.domain.BussLogic;
import me.zhengjie.modules.sgmw.service.BussLogicService;
import me.zhengjie.modules.sgmw.service.dto.BussLogicDto;
import me.zhengjie.modules.sgmw.service.dto.BussLogicQueryCriteria;
import me.zhengjie.utils.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * @auther: wukenan
 * @date: 2020/10/12
 * @description:
 */

@RestController
@RequiredArgsConstructor
@Api(tags = "知识管理-业务逻辑与流程")
@RequestMapping("/api/busslogic")
public class BussLogicController {

    @Autowired
    private BussLogicService bussLogicService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ENTITY_NAME = "busslogic";

    @Log("查询业务逻辑和流程")
    @ApiOperation("查询业务逻辑和流程")
    @GetMapping
    @PreAuthorize("@el.check('knowledge:list')")
    public ResponseEntity<Object> query(BussLogicQueryCriteria criteria) throws Exception{
        List<BussLogicDto> bussLogicDtos = bussLogicService.queryAll(criteria,true);
        Map<String, Object> map = PageUtil.toPage(bussLogicDtos, bussLogicDtos.size());
        String s = objectMapper.writeValueAsString(map);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @Log
    @ApiOperation("查询上级课程，用于编辑时，拿到自己的父级，以及自己父级的同级，并循环直到次顶级，以及顶层的所有课程")
    @PostMapping("/superior")
    @PreAuthorize("@el.check('user:list','knowledge:list')")
    public ResponseEntity<Object> getSuperior(@RequestBody List<Long> ids){
        Set<BussLogicDto> bussLogicDtos = new LinkedHashSet<>();
        for(Long id : ids){
            BussLogicDto bussLogicDto = bussLogicService.findById(id);
            List<BussLogicDto> bussLogics = bussLogicService.getSuperior(bussLogicDto, new ArrayList<>());
            bussLogicDtos.addAll(bussLogics);
        }
        return new ResponseEntity<>(bussLogicService.buildTree(new ArrayList<>(bussLogicDtos)),HttpStatus.OK);
    }

    @Log("新增业务逻辑与流程资料")
    @ApiOperation("新增业务逻辑与流程资料")
    @PostMapping("/file")
    @PreAuthorize("@el.check('knowledge:add')")
    public ResponseEntity<Object> create(@RequestParam String name, @RequestParam Long pid, @RequestParam Boolean enabled, @RequestParam(value = "file") MultipartFile multipartFile){
        BussLogic bussLogic = new BussLogic();
        if (bussLogic.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        bussLogic.setName(name);
        bussLogic.setPid(pid);
        bussLogic.setEnabled(enabled);
        bussLogicService.create(bussLogic, multipartFile);
        return new ResponseEntity<>("hello",HttpStatus.CREATED);
    }

    @Log("修改新员工培训资料")
    @ApiOperation("修改新员工培训资料")
    @PutMapping("/file")
    @PreAuthorize("@el.check('knowledge:edit')")
    public ResponseEntity<Object> update(@RequestParam Long id, @RequestParam String name, @RequestParam Long pid, @RequestParam Boolean enabled, @RequestParam(value = "file") MultipartFile multipartFile){
        BussLogic bussLogic = new BussLogic();
        bussLogic.setId(id);
        bussLogic.setName(name);
        bussLogic.setPid(pid);
        bussLogic.setEnabled(enabled);
        bussLogicService.update(bussLogic, multipartFile);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("修改新员工培训资料")
    @ApiOperation("修改新员工培训资料@Validated(NewEmpTrn.Update.class) @RequestBody NewEmpTrn resources")
    @PutMapping
    @PreAuthorize("@el.check('knowledge:edit')")
    public ResponseEntity<Object> update(@Validated(BussLogic.Update.class) @RequestBody BussLogic resources){
        bussLogicService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除部门")
    @ApiOperation("删除部门")
    @DeleteMapping
    @PreAuthorize("@el.check('knowledge:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        Set<BussLogicDto> bussLogicDtos = new HashSet<>();
        for (Long id : ids) {
            List<BussLogic> bussLogicList = bussLogicService.findByPid(id);
            bussLogicDtos.add(bussLogicService.findById(id));
            if(CollectionUtil.isNotEmpty(bussLogicList)){
                bussLogicDtos = bussLogicService.getDeleteBussLogics(bussLogicList,bussLogicDtos);
            }
        }
        // 验证是否被角色或用户关联
        //newEmpTrnService.verification(newEmpTrnDtos);
        bussLogicService.delete(bussLogicDtos);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
