package me.zhengjie.modules.sgmw.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.modules.sgmw.service.AreaSystemService;
import me.zhengjie.modules.sgmw.service.dto.AreaSystemDto;
import me.zhengjie.modules.sgmw.service.dto.AreaSystemQueryCriteria;
import me.zhengjie.utils.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @auther: wukenan
 * @date: 2020/11/25
 * @description:
 */

@RestController
@RequiredArgsConstructor
@Api(tags = "Overview：系统概况")
@RequestMapping("/api/overview")
public class SystemListController {
    @Autowired
    private final AreaSystemService areaSystemService;
    private static final String ENTITY_NAME = "AreaSystem";

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('dept:list')")
    public void download(HttpServletResponse response, AreaSystemQueryCriteria criteria) throws Exception {
        areaSystemService.download(areaSystemService.queryAll(criteria, false), response);
    }

    @Log("查询系统清单")
    @ApiOperation("查询系统清单")
    @GetMapping(value = "/systemlist")
    @PreAuthorize("@el.check('user:list','dept:list')")
    public ResponseEntity<Object> query(AreaSystemQueryCriteria criteria) throws Exception {
        List<AreaSystemDto> areaSystemDtos = areaSystemService.queryAll(criteria, true);
        return new ResponseEntity<>(PageUtil.toPage(areaSystemDtos, areaSystemDtos.size()), HttpStatus.OK);
    }
}
