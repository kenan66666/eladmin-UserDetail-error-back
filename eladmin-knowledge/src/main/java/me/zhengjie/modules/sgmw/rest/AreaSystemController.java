package me.zhengjie.modules.sgmw.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jdk.nashorn.internal.objects.annotations.Getter;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.modules.sgmw.domain.AreaSystem;
import me.zhengjie.modules.sgmw.service.AreaSystemService;
import me.zhengjie.modules.sgmw.service.dto.AreaSystemDto;
import me.zhengjie.modules.sgmw.service.dto.AreaSystemQueryCriteria;
import me.zhengjie.modules.sgmw.service.dto.LessonLearnDto;
import me.zhengjie.modules.sgmw.service.dto.LessonLearnQueryCriteria;
import me.zhengjie.utils.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @auther: wukenan
 * @date: 2020/11/12
 * @description:
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "知识管理-系统目录")
@RequestMapping("/api/areasystem")
public class AreaSystemController {
    @Autowired
    private AreaSystemService areaSystemService;

    private static final String ENTITY_NAME = "areasystem";

    @Log("查询服务目录")
    @ApiOperation("查询服务目录")
    @GetMapping
    @PreAuthorize("@el.check('knowledge:list')")
    public ResponseEntity<Object> query(AreaSystemQueryCriteria criteria) throws Exception{
        List<AreaSystemDto> areaSystemDtos = areaSystemService.queryAll(criteria,true);
        Map<String, Object> map = PageUtil.toPage(areaSystemDtos, areaSystemDtos.size());
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @Log
    @ApiOperation("模糊查询系统名称与系统编号")
    @GetMapping("/sysnameid")
    @PreAuthorize("@el.check('user:list','knowledge:list')")
    public ResponseEntity<Object> getSysnameId(String sysName){
        return new ResponseEntity<>(areaSystemService.findBySysName(sysName), HttpStatus.OK);
    }

    @Log
    @ApiOperation("模糊查询系统名称与系统编号")
    @GetMapping("/findbysysid")
    @PreAuthorize("@el.check('user:list','knowledge:list')")
    public ResponseEntity<Object> getSystemNameArea(String sysId){
        return new ResponseEntity<>(areaSystemService.findBySysId(sysId), HttpStatus.OK);
    }

    @Log
    @ApiOperation("模糊查询系统名称与系统编号")
    @GetMapping("/systemall")
    @PreAuthorize("@el.check('user:list','knowledge:list')")
    public ResponseEntity<Object> getSystemAll(){
        return new ResponseEntity<>(areaSystemService.findAll(), HttpStatus.OK);
    }

}
