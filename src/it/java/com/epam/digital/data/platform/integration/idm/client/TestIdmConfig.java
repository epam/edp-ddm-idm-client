/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.integration.idm.client;


import com.epam.digital.data.platform.integration.idm.config.IdmClientServiceConfig;
import com.epam.digital.data.platform.integration.idm.factory.IdmServiceFactory;
import com.epam.digital.data.platform.integration.idm.model.KeycloakClientProperties;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.integration.idm.service.PublicIdmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(IdmClientServiceConfig.class)
public class TestIdmConfig {

  @Autowired
  public IdmServiceFactory idmServiceFactory;

  @Bean
  public PublicIdmService publicIdmService() {
    return idmServiceFactory.createPublicIdmService();
  }

  @Bean
  @ConfigurationProperties(prefix = "keycloak.test")
  public KeycloakClientProperties keycloakClientProperties() {
    return new KeycloakClientProperties();
  }

  @Bean
  public IdmService idmService(KeycloakClientProperties keycloakClientProperties) {
    return idmServiceFactory.createIdmService(keycloakClientProperties().getRealm(),
        keycloakClientProperties.getClientId(), keycloakClientProperties().getClientSecret());
  }

}
