package info.pich.chatbottest.configuration;

import lombok.Data;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Groups.CAMUNDA_ADMIN;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;

@ConfigurationProperties("camunda.bpm.user")
@Data
public class CamundaUserConfiguration extends AbstractCamundaConfiguration {
    private String name;
    private String password;

    public void postProcessEngineBuild(ProcessEngine processEngine) {
        final IdentityService identityService = processEngine.getIdentityService();
        final AuthorizationService authorizationService = processEngine.getAuthorizationService();

        if(name != null) {
            User singleResult = identityService.createUserQuery().userId(name).singleResult();
            if (singleResult != null) {
                return;
            }

            logger.info("Generating user data");

            User user = identityService.newUser(name);
            user.setFirstName(name);
            user.setLastName(name);
            user.setPassword(password);
            user.setEmail(name + "@localhost");
            identityService.saveUser(user);

            // create group
            if(identityService.createGroupQuery().groupId(CAMUNDA_ADMIN).count() == 0) {
                Group camundaAdminGroup = identityService.newGroup(CAMUNDA_ADMIN);
                camundaAdminGroup.setName("camunda BPM Administrators");
                camundaAdminGroup.setType(Groups.GROUP_TYPE_SYSTEM);
                identityService.saveGroup(camundaAdminGroup);
            }

            // create ADMIN authorizations on all built-in resources
            for (Resource resource : Resources.values()) {
                if(authorizationService.createAuthorizationQuery().groupIdIn(CAMUNDA_ADMIN).resourceType(resource).resourceId(ANY).count() == 0) {
                    AuthorizationEntity userAdminAuth = new AuthorizationEntity(AUTH_TYPE_GRANT);
                    userAdminAuth.setGroupId(CAMUNDA_ADMIN);
                    userAdminAuth.setResource(resource);
                    userAdminAuth.setResourceId(ANY);
                    userAdminAuth.addPermission(ALL);
                    authorizationService.saveAuthorization(userAdminAuth);
                }
            }

            identityService.createMembership(name, "camunda-admin");
        }
    }
}
