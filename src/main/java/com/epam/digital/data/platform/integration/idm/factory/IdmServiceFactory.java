/*
 * Copyright 2023 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.integration.idm.factory;

import com.epam.digital.data.platform.integration.idm.client.KeycloakAdminClient;
import com.epam.digital.data.platform.integration.idm.client.PublicKeycloakAuthClient;
import com.epam.digital.data.platform.integration.idm.mapper.IdmUsersMapper;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.integration.idm.service.KeycloakIdmService;
import com.epam.digital.data.platform.integration.idm.service.PublicIdmService;
import com.epam.digital.data.platform.integration.idm.service.PublicKeycloakIdmService;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;

@RequiredArgsConstructor
public class IdmServiceFactory {

  private static final String KEYCLOAK_AUTH_URL_PATTERN = "%s/auth";
  private final String serverUrl;
  private final ApplicationContext applicationContext;
  private final IdmUsersMapper idmUsersMapper;

  public IdmService createIdmService(String realm, String clientId, String clientSecret) {
    var serverAthUrl = String.format(KEYCLOAK_AUTH_URL_PATTERN, this.serverUrl);
    var keycloak = KeycloakBuilder.builder()
        .clientSecret(clientSecret)
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(clientId).realm(realm)
        .serverUrl(serverAthUrl)
        .build();
    var keycloakAdminClient = new KeycloakAdminClient(realm, serverAthUrl, keycloak);
    return new KeycloakIdmService(keycloakAdminClient, idmUsersMapper);
  }

  public PublicIdmService createPublicIdmService() {
    return new PublicKeycloakIdmService(
        new FeignClientBuilder(applicationContext).forType(PublicKeycloakAuthClient.class,
            "public-keycloak-auth-client").url(serverUrl).build());
  }

}
