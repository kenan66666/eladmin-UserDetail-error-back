package me.zhengjie.modules.sgmw.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.modules.sgmw.domain.OverviewMain;
import me.zhengjie.modules.sgmw.service.OverviewMainService;
import me.zhengjie.modules.sgmw.service.dto.OverviewMainDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * @auther: wukenan
 * @date: 2020/11/23
 * @description:
 */

@RestController
@RequiredArgsConstructor
@Api(tags = "Overview：main")
@RequestMapping("/api/overviewmain")
public class OverviewMainController {

    private final OverviewMainService overviewMainService;
    private static final String ENTITY_NAME = "overviewmain";

//    @Log("查询系统概况主要信息")
//    @ApiOperation("查询系统概况主要信息")
//    @GetMapping
//    @PreAuthorize("@el.check('user:list','dept:list')")
//    public ResponseEntity<Object> queryBySysName(String sysName) {
//         OverviewMainDto overviewMainDto = overviewMainService.findBySysName(sysName);
//        // OverviewMainDto overviewMainDto = overviewMainService.findBySysId(sysId);
//        return new ResponseEntity<>(overviewMainDto, HttpStatus.OK);
//    }

    @Log("查询系统概况主要信息")
    @ApiOperation("查询系统概况主要信息")
    @GetMapping
    @PreAuthorize("@el.check('user:list','dept:list')")
    public ResponseEntity<Object> queryBySysId(String sysId) {
        OverviewMainDto overviewMainDto = overviewMainService.findBySysId(sysId);
        return new ResponseEntity<>(overviewMainDto, HttpStatus.OK);
    }

//    @Log("查询系统概况主要信息")
//    @ApiOperation("查询系统概况主要信息")
//    @GetMapping
//    @PreAuthorize("@el.check('user:list','dept:list')")
//    public ResponseEntity<Object> queryById(Long id) {
//        OverviewMainDto overviewMainDto = overviewMainService.findById(id);
//        return new ResponseEntity<>(overviewMainDto, HttpStatus.OK);
//    }

    @Log("新增系统主要概况")
    @ApiOperation("新增系统主要概况")
    @PostMapping
    @PreAuthorize("@el.check('dept:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody OverviewMain resources){
        overviewMainService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改系统主要概况")
    @ApiOperation("修改系统主要概况")
    @PutMapping
    @PreAuthorize("@el.check('dept:edit')")
    public ResponseEntity<Object> update(@Validated(OverviewMain.Update.class) @RequestBody OverviewMain resources){
        overviewMainService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
