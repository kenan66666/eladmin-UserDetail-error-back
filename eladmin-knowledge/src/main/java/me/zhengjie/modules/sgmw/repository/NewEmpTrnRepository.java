package me.zhengjie.modules.sgmw.repository;

import me.zhengjie.modules.sgmw.domain.NewEmpTrn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface NewEmpTrnRepository extends JpaRepository<NewEmpTrn,Long>, JpaSpecificationExecutor<NewEmpTrn> {

    /**
     * create by: wukenan
     * description: TODO
     * create time: 2020/8/26 10:33 上午
      * @Param: null
     * @return 
     */
    List<NewEmpTrn> findByPid(Long ID);

    /**
     * create by: wukenan
     * description: TODO
     * create time: 2020/8/26 9:41 上午
      * @Param: null
     * @return 
     */
    List<NewEmpTrn> findByPidIsNull();

    /**
     * create by: wukenan
     * description: 判断是否存在子知识点
     * create time: 2020/8/26 11:10 上午
      * @Param: null
     * @return
     */
    int countByPid(Long pid);

    /**
     * create by: wukenan
     * description: 根据相应的知识组(父级知识点)编号，改变子其内部知识点（子级知识点）的数目。
     * create time: 2020/8/26 11:17 上午
      * @Param: null
     * @return
     */
    @Modifying
    @Query(value = "update knlge_new set sub_count = ?1 where id = ?2 ",nativeQuery = true)
    void updateSubCntByID(Integer count,Long id);
}
