package me.zhengjie.modules.sgmw.rest;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.modules.sgmw.domain.LessonLearn;
import me.zhengjie.modules.sgmw.service.LessonLearnService;
import me.zhengjie.modules.sgmw.service.dto.LessonLearnDto;
import me.zhengjie.modules.sgmw.service.dto.LessonLearnQueryCriteria;
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
 * @date: 2020/11/6
 * @description:
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "知识管理-业务逻辑与流程")
@RequestMapping("/api/lessonlearn")
public class LessonLearnController {

    @Autowired
    private LessonLearnService lessonLearnService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ENTITY_NAME = "lessonlearn";

    @Log("查询经验教训")
    @ApiOperation("查询经验教训")
    @GetMapping
    @PreAuthorize("@el.check('knowledge:list')")
    public ResponseEntity<Object> query(LessonLearnQueryCriteria criteria) throws Exception{
        List<LessonLearnDto> lessonLearnDtos = lessonLearnService.queryAll(criteria,true);
        Map<String, Object> map = PageUtil.toPage(lessonLearnDtos, lessonLearnDtos.size());
        String s = objectMapper.writeValueAsString(map);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @Log
    @ApiOperation("查询上级课程，用于编辑时，拿到自己的父级，以及自己父级的同级，并循环直到次顶级，以及顶层的所有课程")
    @PostMapping("/superior")
    @PreAuthorize("@el.check('user:list','knowledge:list')")
    public ResponseEntity<Object> getSuperior(@RequestBody List<Long> ids){
        Set<LessonLearnDto> lessonLearnDtos = new LinkedHashSet<>();
        for(Long id : ids){
            LessonLearnDto lessonLearnDto = lessonLearnService.findById(id);
            List<LessonLearnDto> lessonLearns = lessonLearnService.getSuperior(lessonLearnDto, new ArrayList<>());
            lessonLearnDtos.addAll(lessonLearns);
        }
        return new ResponseEntity<>(lessonLearnService.buildTree(new ArrayList<>(lessonLearnDtos)),HttpStatus.OK);
    }

    @Log("新增经验教训资料")
    @ApiOperation("新增经验教训资料")
    @PostMapping("/file")
    @PreAuthorize("@el.check('knowledge:add')")
    public ResponseEntity<Object> create(@RequestParam String sysId, @RequestParam String sysName, @RequestParam String name, @RequestParam String appear, @RequestParam String rootCause, @RequestParam String solution ,@RequestParam Boolean enabled, @RequestParam(value = "file") MultipartFile multipartFile){
        LessonLearn lessonLearn = new LessonLearn();
        if (lessonLearn.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        lessonLearn.setSysId(sysId);
        lessonLearn.setSysName(sysName);
        lessonLearn.setName(name);
        lessonLearn.setAppear(appear);
        lessonLearn.setRootCause(rootCause);
        lessonLearn.setSolution(solution);
        lessonLearn.setEnabled(enabled);
        lessonLearnService.create(lessonLearn, multipartFile);
        return new ResponseEntity<>("hello",HttpStatus.CREATED);
    }

    @Log("新增经验教训资料")
    @ApiOperation("新增经验教训资料")
    @PostMapping
    @PreAuthorize("@el.check('knowledge:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody LessonLearn resources){
        lessonLearnService.create(resources);
        return new ResponseEntity<>("hello", HttpStatus.CREATED);
    }

    @Log("修改新员工培训资料")
    @ApiOperation("修改新员工培训资料")
    @PutMapping("/file")
    @PreAuthorize("@el.check('knowledge:edit')")
    public ResponseEntity<Object> update(@RequestParam Long id, @RequestParam String sysId, @RequestParam String sysName, @RequestParam String name, @RequestParam String appear, @RequestParam String rootCause, @RequestParam String solution ,@RequestParam Boolean enabled, @RequestParam(value = "file") MultipartFile multipartFile){
        LessonLearn lessonLearn = new LessonLearn();
        lessonLearn.setId(id);
        lessonLearn.setSysId(sysId);
        lessonLearn.setName(sysName);
        lessonLearn.setName(name);
        lessonLearn.setAppear(appear);
        lessonLearn.setRootCause(rootCause);
        lessonLearn.setSolution(solution);
        lessonLearn.setEnabled(enabled);
        lessonLearnService.update(lessonLearn, multipartFile);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("修改新员工培训资料")
    @ApiOperation("修改新员工培训资料@Validated(NewEmpTrn.Update.class) @RequestBody NewEmpTrn resources")
    @PutMapping
    @PreAuthorize("@el.check('knowledge:edit')")
    public ResponseEntity<Object> update(@Validated(LessonLearn.Update.class) @RequestBody LessonLearn resources){
        lessonLearnService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Log("删除部门")
    @ApiOperation("删除部门")
    @DeleteMapping
    @PreAuthorize("@el.check('knowledge:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        Set<LessonLearnDto> lessonLearnDtos = new HashSet<>();
        for (Long id : ids) {
            List<LessonLearn> lessonLearnList = lessonLearnService.findByPid(id);
            lessonLearnDtos.add(lessonLearnService.findById(id));
            if(CollectionUtil.isNotEmpty(lessonLearnList)){
                lessonLearnDtos = lessonLearnService.getDeleteLessonLearns(lessonLearnList,lessonLearnDtos);
            }
        }
        // 验证是否被角色或用户关联
        //newEmpTrnService.verification(newEmpTrnDtos);
        lessonLearnService.delete(lessonLearnDtos);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
