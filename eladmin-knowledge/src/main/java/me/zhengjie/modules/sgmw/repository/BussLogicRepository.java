package me.zhengjie.modules.sgmw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import me.zhengjie.modules.sgmw.domain.BussLogic;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BussLogicRepository extends JpaRepository<BussLogic,Long>, JpaSpecificationExecutor<BussLogic> {
    List<BussLogic> findByPid(Long ID);

    List<BussLogic> findByPidIsNull();

    int countByPid(Long ID);

    @Modifying
    @Query(value = "update knlge_busslogic set sub_count = ?1 where id = ?2 ",nativeQuery = true)
    void updateSubCntByID(Integer count,Long id);
}
