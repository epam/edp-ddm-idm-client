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

package com.epam.digital.data.platform.integration.idm.service;

import com.epam.digital.data.platform.integration.idm.model.IdmRole;
import com.epam.digital.data.platform.integration.idm.model.IdmUser;
import com.epam.digital.data.platform.integration.idm.model.IdmUsersResponse;
import com.epam.digital.data.platform.integration.idm.model.SearchUserQuery;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByEqualsAndStartsWithAttributesRequestDto;
import java.util.List;
import org.keycloak.representations.idm.RoleRepresentation;

public interface IdmService {

  String getClientAccessToken();

  List<IdmRole> getRoles();

  List<RoleRepresentation> getRoleRepresentations();

  void removeRole(String username, String role);

  void removeRoles(String username, List<RoleRepresentation> roles);

  void addRole(String username, String role);

  void addRoles(String username, List<RoleRepresentation> roles);

  List<IdmUser> getRoleUserMembers(String role);

  /**
   * @deprecated use {@link IdmService#searchUsers(SearchUsersByAttributesRequestDto)} instead
   */
  @Deprecated(forRemoval = true)
  List<IdmUser> searchUsers(SearchUserQuery searchUserQuery);

  /**
   * @deprecated use {@link IdmService#searchUsers(SearchUsersByAttributesRequestDto)} instead
   */
  @Deprecated(forRemoval = true)
  List<IdmUser> searchUsers(SearchUsersByEqualsAndStartsWithAttributesRequestDto searchUserQuery);

  /**
   * Search users by attributes matching.
   *
   * @param requestDto dto that contains map of required matches for user attributes to return the
   *                   user
   * @return list of found users with token for the next page of users
   */
  IdmUsersResponse searchUsers(SearchUsersByAttributesRequestDto requestDto);

  List<IdmUser> getUserByUserName(String username);

  void saveUserAttribute(String username, String attribute, List<String> values);
}
