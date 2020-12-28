package me.zhengjie.modules.sgmw.repository;

import me.zhengjie.modules.sgmw.domain.KeycloakUserExtend;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface KeycloakUserExtendRepository extends JpaRepositoryImplementation<KeycloakUserExtend, String> {

    List<KeycloakUserExtend> findAllByUsernameIn(Set<String> usernameSet);

    @Query(value = "SELECT username,display_name FROM keycloak_user_extend WHERE username in (?1)", nativeQuery = true)
    List<Map<String, Object>> findDisplayNameMappingByUsernameIn(Set<String> usernameSet);

    @Modifying
    @Query(value = "INSERT INTO `keycloak_user_extend` ( `username`, `keycloak_user_id`, `email`, `first_name`, `last_name`, `department_name`, `title`, `display_name`, `full_name`, `home_telephone`, `mobile_telephone` )\n" +
            "VALUES\n" +
            "\t( ?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11) \n" +
            "\tON DUPLICATE KEY UPDATE  `keycloak_user_id` = ?2, `email` = ?3, `first_name` = ?4, `last_name` = ?5, `department_name` = ?6, `title` = ?7, `display_name` = ?8, `full_name` = ?9, `home_telephone` = ?10, `mobile_telephone` = ?11", nativeQuery = true)
    void insertOrUpdate(String username, String keycloakUserId, String email, String firstName, String lastName, String departmentName, String title, String displayName, String fullName, String homeTelephone, String mobileTelephone);

}
