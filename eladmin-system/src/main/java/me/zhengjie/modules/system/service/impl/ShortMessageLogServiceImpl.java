package me.zhengjie.modules.system.service.impl;

import me.zhengjie.modules.system.domain.ShortMessageLog;
import me.zhengjie.modules.system.repository.ShortMessageLogRepository;
import me.zhengjie.modules.system.service.ShortMessageLogService;
import me.zhengjie.modules.system.service.dto.ShortMessageLogDto;
import me.zhengjie.modules.system.service.dto.ShortMessageLogQueryCriteria;
import me.zhengjie.modules.system.service.mapstruct.ShortMessageLogMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
* @author Kermit
* @date 2020-01-13
*/
@Service
//@CacheConfig(cacheNames = "shortMessageLog")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class ShortMessageLogServiceImpl implements ShortMessageLogService {

    private final ShortMessageLogRepository shortMessageLogRepository;

    private final ShortMessageLogMapper shortMessageLogMapper;

    public ShortMessageLogServiceImpl(ShortMessageLogRepository shortMessageLogRepository, ShortMessageLogMapper shortMessageLogMapper) {
        this.shortMessageLogRepository = shortMessageLogRepository;
        this.shortMessageLogMapper = shortMessageLogMapper;
    }

    @Override
    //@Cacheable
    public Map<String,Object> queryAll(ShortMessageLogQueryCriteria criteria, Pageable pageable){
        Page<ShortMessageLog> page = shortMessageLogRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(shortMessageLogMapper::toDto));
    }

    @Override
    //@Cacheable
    public List<ShortMessageLogDto> queryAll(ShortMessageLogQueryCriteria criteria){
        return shortMessageLogMapper.toDto(shortMessageLogRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    @Override
    //@Cacheable(key = "#p0")
    public ShortMessageLogDto findById(Integer id) {
        ShortMessageLog shortMessageLog = shortMessageLogRepository.findById(id).orElseGet(ShortMessageLog::new);
        ValidationUtil.isNull(shortMessageLog.getId(),"ShortMessageLog","id",id);
        return shortMessageLogMapper.toDto(shortMessageLog);
    }

    @Override
    //@CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public ShortMessageLogDto create(ShortMessageLog resources) {
        return shortMessageLogMapper.toDto(shortMessageLogRepository.save(resources));
    }

    @Override
    //@CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void update(ShortMessageLog resources) {
        ShortMessageLog shortMessageLog = shortMessageLogRepository.findById(resources.getId()).orElseGet(ShortMessageLog::new);
        ValidationUtil.isNull( shortMessageLog.getId(),"ShortMessageLog","id",resources.getId());
        shortMessageLog.copy(resources);
        shortMessageLogRepository.save(shortMessageLog);
    }

    @Override
    //@CacheEvict(allEntries = true)
    public void deleteAll(Integer[] ids) {
        for (Integer id : ids) {
            shortMessageLogRepository.deleteById(id);
        }
    }

    @Override
    public void download(List<ShortMessageLogDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ShortMessageLogDto shortMessageLog : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("手机号", shortMessageLog.getPhone());
            map.put("内容", shortMessageLog.getContent());
            map.put("发送时间", shortMessageLog.getSendTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}