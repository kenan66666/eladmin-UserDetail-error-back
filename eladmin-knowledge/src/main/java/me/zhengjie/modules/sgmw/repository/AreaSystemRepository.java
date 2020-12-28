package me.zhengjie.modules.sgmw.repository;


import me.zhengjie.modules.sgmw.domain.AreaSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AreaSystemRepository extends JpaRepository<AreaSystem,Long>, JpaSpecificationExecutor<AreaSystem> {
    List<AreaSystem> findBySysNameLike(String sysName);
    AreaSystem findBySysId(String sysId);
}
