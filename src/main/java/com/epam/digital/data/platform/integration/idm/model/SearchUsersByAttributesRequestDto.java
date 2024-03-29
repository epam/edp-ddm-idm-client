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

package com.epam.digital.data.platform.integration.idm.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * Representation of search users by attributes request body.
 */
@Builder
@Getter
public class SearchUsersByAttributesRequestDto {

  private Map<String, List<String>> attributesEquals;
  private Map<String, List<String>> attributesStartsWith;
  private Map<String, List<String>> attributesThatAreStartFor;
  private Pagination pagination;

  @Builder
  @Getter
  public static class Pagination {

    private Integer limit;
    private Integer continueToken;
  }
}
