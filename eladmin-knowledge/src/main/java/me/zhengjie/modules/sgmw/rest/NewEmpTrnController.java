package me.zhengjie.modules.sgmw.rest;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.modules.sgmw.domain.NewEmpTrn;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.modules.sgmw.service.dto.NewEmpTrnDto;
import me.zhengjie.modules.sgmw.service.NewEmpTrnService;
import me.zhengjie.modules.sgmw.service.dto.NewEmpTrnQueryCriteria;
import me.zhengjie.utils.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequiredArgsConstructor
@Api(tags = "知识管理-新员工培训")
@RequestMapping("/api/newemptrn")
public class NewEmpTrnController {

    @Autowired
    private NewEmpTrnService newEmpTrnService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ENTITY_NAME = "newemptrn";

    @Log("查询部门")
    @ApiOperation("查询部门")
    @GetMapping
    @PreAuthorize("@el.check('knowledge:list')")
    public ResponseEntity<Object> query(NewEmpTrnQueryCriteria criteria) throws Exception{
        List<NewEmpTrnDto> newEmpTrnDtos = newEmpTrnService.queryAll(criteria,true);
        Map<String, Object> map = PageUtil.toPage(newEmpTrnDtos, newEmpTrnDtos.size());
        String s = objectMapper.writeValueAsString(map);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @Log
    @ApiOperation("查询上级课程，用于编辑时，拿到自己的父级，以及自己父级的同级，并循环直到次顶级，以及顶层的所有课程")
    @PostMapping("/superior")
    @PreAuthorize("@el.check('user:list','dept:list')")
    public ResponseEntity<Object> getSuperior(@RequestBody List<Long> ids){
        Set<NewEmpTrnDto> newEmpTrnDtos = new LinkedHashSet<>();
        for(Long id : ids){
            NewEmpTrnDto newEmpTrnDto = newEmpTrnService.findById(id);
            List<NewEmpTrnDto> newEmpTrns = newEmpTrnService.getSuperior(newEmpTrnDto, new ArrayList<>());
            newEmpTrnDtos.addAll(newEmpTrns);
        }
        Object o = newEmpTrnService.buildTree(new ArrayList<>(newEmpTrnDtos));
        return new ResponseEntity<>(o,HttpStatus.OK);
    }

    @Log("新增新员工培训资料")
    @ApiOperation("新增新员工培训资料")
    @PostMapping("/file")
    @PreAuthorize("@el.check('knowledge:add')")
    public ResponseEntity<Object> create(@RequestParam String name, @RequestParam Long pid, @RequestParam Boolean enabled, @RequestParam(value = "file") MultipartFile multipartFile){
        NewEmpTrn newEmpTrn = new NewEmpTrn();
        if (newEmpTrn.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        newEmpTrn.setName(name);
        newEmpTrn.setPid(pid);
        newEmpTrn.setEnabled(enabled);
        newEmpTrnService.create(newEmpTrn,multipartFile);
        return new ResponseEntity<>("hello", HttpStatus.CREATED);
    }

    @Log("修改新员工培训资料")
    @ApiOperation("修改新员工培训资料")
    @PutMapping("/file")
    @PreAuthorize("@el.check('knowledge:edit')")
    public ResponseEntity<Object> update(@RequestParam Long id, @RequestParam String name, @RequestParam Long pid, @RequestParam Boolean enabled, @RequestParam(value = "file") MultipartFile multipartFile){
        NewEmpTrn newEmpTrn = new NewEmpTrn();
        newEmpTrn.setId(id);
        newEmpTrn.setName(name);
        newEmpTrn.setPid(pid);
        newEmpTrn.setEnabled(enabled);
        newEmpTrnService.update(newEmpTrn, multipartFile);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("修改新员工培训资料")
    @ApiOperation("修改新员工培训资料")
    @PutMapping
    @PreAuthorize("@el.check('knowledge:edit')")
    public ResponseEntity<Object> update(@Validated(NewEmpTrn.Update.class) @RequestBody NewEmpTrn resources){
        newEmpTrnService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除部门")
    @ApiOperation("删除部门")
    @DeleteMapping
    @PreAuthorize("@el.check('knowledge:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        Set<NewEmpTrnDto> newEmpTrnDtos = new HashSet<>();
        for (Long id : ids) {
            List<NewEmpTrn> newEmpTrnList = newEmpTrnService.findByPid(id);
            newEmpTrnDtos.add(newEmpTrnService.findById(id));
            if(CollectionUtil.isNotEmpty(newEmpTrnList)){
                newEmpTrnDtos = newEmpTrnService.getDeleteNewEmpTrns(newEmpTrnList,newEmpTrnDtos);
            }
        }
        // 验证是否被角色或用户关联
        //newEmpTrnService.verification(newEmpTrnDtos);
        newEmpTrnService.delete(newEmpTrnDtos);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
