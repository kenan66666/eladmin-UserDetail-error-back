package me.zhengjie.modules.system.repository;

import me.zhengjie.modules.system.domain.ShortMessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
* @author Kermit
* @date 2020-01-13
*/
public interface ShortMessageLogRepository extends JpaRepository<ShortMessageLog, Integer>, JpaSpecificationExecutor<ShortMessageLog> {
}