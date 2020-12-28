package me.zhengjie.modules.system.service;

import me.zhengjie.modules.system.domain.ShortMessageLog;
import me.zhengjie.modules.system.service.dto.ShortMessageLogDto;
import me.zhengjie.modules.system.service.dto.ShortMessageLogQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
* @author Kermit
* @date 2020-01-13
*/
public interface ShortMessageLogService {

    /**
    * 查询数据分页
    * @param criteria 条件
    * @param pageable 分页参数
    * @return Map<String,Object>
    */
    Map<String,Object> queryAll(ShortMessageLogQueryCriteria criteria, Pageable pageable);

    /**
    * 查询所有数据不分页
    * @param criteria 条件参数
    * @return List<ShortMessageLogDto>
    */
    List<ShortMessageLogDto> queryAll(ShortMessageLogQueryCriteria criteria);

    /**
     * 根据ID查询
     * @param id ID
     * @return ShortMessageLogDto
     */
    ShortMessageLogDto findById(Integer id);

    /**
    * 创建
    * @param resources /
    * @return ShortMessageLogDto
    */
    ShortMessageLogDto create(ShortMessageLog resources);

    /**
    * 编辑
    * @param resources /
    */
    void update(ShortMessageLog resources);

    /**
    * 多选删除
    * @param ids /
    */
    void deleteAll(Integer[] ids);

    /**
    * 导出数据
    * @param all 待导出的数据
    * @param response /
    * @throws IOException /
    */
    void download(List<ShortMessageLogDto> all, HttpServletResponse response) throws IOException;
}