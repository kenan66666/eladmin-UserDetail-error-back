package me.zhengjie.modules.sgmw.repository;

import me.zhengjie.modules.sgmw.domain.LessonLearn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LessonLearnRepository extends JpaRepository<LessonLearn,Long>, JpaSpecificationExecutor<LessonLearn> {
    List<LessonLearn> findByPid(Long ID);

    List<LessonLearn> findByPidIsNull();

    int countByPid(Long ID);

    @Modifying
    @Query(value = "update knlge_lesson_learn set sub_count = ?1 where id = ?2 ",nativeQuery = true)
    void updateSubCntByID(Integer count,Long id);

}
