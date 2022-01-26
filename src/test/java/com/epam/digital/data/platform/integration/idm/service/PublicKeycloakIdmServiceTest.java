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

package com.epam.digital.data.platform.integration.idm.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.integration.idm.client.PublicKeycloakAuthClient;
import java.security.PublicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.PublishedRealmRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublicKeycloakIdmServiceTest {

  @Mock
  private PublicKeycloakAuthClient publicKeycloakAuthClient;

  private PublicKeycloakIdmService idmService;
  private final String realmName = "realmTest";

  @BeforeEach
  public void init() {
    idmService = new PublicKeycloakIdmService(publicKeycloakAuthClient);
  }

  @Test
  void getRealmRepresentation() {
    var realmRepresentation = mock(PublishedRealmRepresentation.class);
    var key = mock(PublicKey.class);
    when(realmRepresentation.getPublicKey()).thenReturn(key);
    when(publicKeycloakAuthClient.getRealmRepresentation(realmName)).thenReturn(
        realmRepresentation);
    var realm = idmService.getRealm(realmName);
    assertThat(realm.getPublicKey()).isEqualTo(key);
  }

}