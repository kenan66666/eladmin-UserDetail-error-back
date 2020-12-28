package me.zhengjie.modules.sgmw.repository;

import me.zhengjie.modules.sgmw.domain.OverviewMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OverviewMainRepository extends JpaRepository<OverviewMain,Long>, JpaSpecificationExecutor<OverviewMain> {
    int countById(Long ID);

    OverviewMain findBySysName(String sysName);
    OverviewMain findBySysId(String sysId);

}
