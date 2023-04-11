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

package com.epam.digital.data.platform.integration.idm.mapper;

import com.epam.digital.data.platform.integration.idm.model.IdmUser;
import com.epam.digital.data.platform.integration.idm.model.IdmUsersResponse;
import com.epam.digital.data.platform.integration.idm.model.KeycloakSystemAttribute;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesResponseDto;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.lang.NonNull;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface IdmUsersMapper {

  @Mapping(source = "username", target = "userName")
  @Mapping(source = "attributes", target = "fullName", qualifiedByName = "getFullName")
  IdmUser toIdmUser(UserRepresentation userRepresentation);

  @Named("getFullName")
  default String getFullName(Map<String, List<String>> attributes) {
    return attributes.get(KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE)
        .get(KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE_INDEX);
  }

  @Named("mapToIdmUsersSortedByFullName")
  default List<IdmUser> mapToIdmUsersSortedByFullName(
      Collection<UserRepresentation> roleUserMembers) {
    return roleUserMembers.stream()
        .filter(this::hasFullNameAttribute)
        .map(this::toIdmUser)
        .sorted(Comparator.comparing(IdmUser::getFullName))
        .collect(Collectors.toList());
  }

  @Mapping(source = "users", target = "users", qualifiedByName = "mapToIdmUsersSortedByFullName")
  IdmUsersResponse toIdmUsersResponse(SearchUsersByAttributesResponseDto responseDto);

  private boolean hasFullNameAttribute(@NonNull UserRepresentation user) {
    var attribute = user.getAttributes();
    return Objects.nonNull(attribute) && Objects
        .nonNull(attribute.get(KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE));
  }
}
