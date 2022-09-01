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
package com.epam.digital.data.platform.integration.idm.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchUserQuery {

  private Map<String, String> attributes;

  public static class SearchUserQueryBuilder {

    public SearchUserQueryBuilder drfo(String drfo) {
      if (attributes == null) {
        attributes = new HashMap<>();
      }
      attributes.put(KeycloakSystemAttribute.DRFO, drfo);
      return this;
    }

    public SearchUserQueryBuilder edrpou(String edrpou) {
      if (attributes == null) {
        attributes = new HashMap<>();
      }
      attributes.put(KeycloakSystemAttribute.EDRPOU, edrpou);
      return this;
    }

    public SearchUserQueryBuilder attributes(Map<String, String> attributes) {
      if (this.attributes == null) {
        this.attributes = new HashMap<>();
      }
      this.attributes.putAll(attributes);
      return this;
    }

  }

}
