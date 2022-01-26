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

import com.epam.digital.data.platform.integration.idm.model.PublishedIdmRealm;
import com.epam.digital.data.platform.integration.idm.client.PublicKeycloakAuthClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublicKeycloakIdmService implements PublicIdmService {

  private final PublicKeycloakAuthClient client;

  @Override
  public PublishedIdmRealm getRealm(String realm) {
    return PublishedIdmRealm.builder()
        .publicKey(client.getRealmRepresentation(realm).getPublicKey()).build();
  }
}
