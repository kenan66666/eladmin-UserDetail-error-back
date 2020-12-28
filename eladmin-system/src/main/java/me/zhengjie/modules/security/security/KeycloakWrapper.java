package me.zhengjie.modules.security.security;

import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Keycloak的包装操作
 *
 * @author wbq
 */
@Component
public class KeycloakWrapper {

    private final Keycloak keycloak;
    private final KeycloakSpringBootProperties keycloakSpringBootProperties;

    // 需要配置如下
    // keycloak:
    //   admin:
    //    client-id: admin-cli
    //    realm: master
    //    username: admin
    //    password: Sgmw@5050

    public KeycloakWrapper(
            KeycloakSpringBootProperties keycloakSpringBootProperties,
            @Value("${keycloak-admin.client-id:admin-cli}") String clientId,
            @Value("${keycloak-admin.realm:master}") String realm,
            @Value("${keycloak-admin.username:admin}") String username,
            @Value("${keycloak-admin.password:Sgmw@5050}") String password
    ) {
        this.keycloakSpringBootProperties = keycloakSpringBootProperties;
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakSpringBootProperties.getAuthServerUrl())
                .clientId(clientId)
                .realm(realm)
                .username(username)
                .password(password)
                .build();
    }

    public Keycloak getKeycloak() {
        return keycloak;
    }

    public KeycloakSpringBootProperties getKeycloakSpringBootProperties() {
        return keycloakSpringBootProperties;
    }

    public String getRealm() {
        return getKeycloakSpringBootProperties().getRealm();
    }


}
