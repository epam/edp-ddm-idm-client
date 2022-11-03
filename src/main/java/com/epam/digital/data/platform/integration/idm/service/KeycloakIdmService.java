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

import com.epam.digital.data.platform.integration.idm.client.KeycloakAdminClient;
import com.epam.digital.data.platform.integration.idm.exception.KeycloakException;
import com.epam.digital.data.platform.integration.idm.model.IdmRole;
import com.epam.digital.data.platform.integration.idm.model.IdmUser;
import com.epam.digital.data.platform.integration.idm.model.KeycloakSystemAttribute;
import com.epam.digital.data.platform.integration.idm.model.SearchUserQuery;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByEqualsAndStartsWithAttributesRequestDto;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;

@RequiredArgsConstructor
public class KeycloakIdmService implements IdmService {

  private final KeycloakAdminClient client;

  @Override
  public String getClientAccessToken() {
    return client.getClientAccessToken();
  }

  @Override
  public List<IdmRole> getRoles() {
    var realmResource = client.getRealmResource();
    return client.getKeycloakRoles(realmResource).stream()
        .map(r -> IdmRole.builder().name(r.getName()).build()).collect(Collectors.toList());
  }

  @Override
  public void removeRole(String username, String role) {
    var realmResource = client.getRealmResource();
    var roleRepresentation = client.getRoleRepresentation(realmResource, role);
    var userRepresentation = this.getUserRepresentation(realmResource, username);
    var roleScopeResource = client.getRoleScopeResource(realmResource, userRepresentation.getId());
    client.removeRole(roleScopeResource, roleRepresentation);
  }

  public void addRole(String username, String role) {
    var realmResource = client.getRealmResource();
    var roleRepresentation = client.getRoleRepresentation(realmResource, role);
    var userRepresentation = this.getUserRepresentation(realmResource, username);
    var roleScope = client.getRoleScopeResource(realmResource, userRepresentation.getId());
    client.addRole(roleScope, roleRepresentation);
  }

  @Override
  public List<IdmUser> getRoleUserMembers(String role) {
    var realmResource = client.getRealmResource();
    return mapToIdmUsers(client.getRoleUserMembers(realmResource, role));
  }

  @Override
  public List<IdmUser> searchUsers(SearchUserQuery searchUserQuery) {
    return mapToIdmUsers(client.searchUsersByAttributes(searchUserQuery));
  }

  @Override
  public List<IdmUser> searchUsers(
      SearchUsersByEqualsAndStartsWithAttributesRequestDto searchUserQuery) {
    return mapToIdmUsers(client.searchUsersByAttributes(searchUserQuery));
  }

  @Override
  public List<IdmUser> getUserByUserName(String username) {
    var realmResource = client.getRealmResource();
    return mapToIdmUsers(client.getUsersRepresentationByUsername(realmResource, username));
  }

  @Override
  public void saveUserAttribute(String username, String attribute, List<String> values) {
    var realmResource = client.getRealmResource();
    var user = client.getUsersRepresentationByUsername(realmResource, username);
    client.saveUserAttribute(realmResource, user.get(0).getId(), attribute, values);
  }

  private List<IdmUser> mapToIdmUsers(Collection<UserRepresentation> roleUserMembers) {
    return roleUserMembers.stream()
        .filter(this::hasFullNameAttribute)
        .map(user -> IdmUser.builder().id(user.getId()).userName(user.getUsername()).fullName(
            user.getAttributes().get(KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE)
                .get(KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE_INDEX))
            .attributes(user.getAttributes())
            .build())
        .sorted(Comparator.comparing(IdmUser::getFullName))
        .collect(Collectors.toList());
  }

  /**
   * Used for filtering out service account users
   *
   * @param user - keycloak user representation
   * @return true if keycloak user has fullName attribute and false otherwise
   */
  private boolean hasFullNameAttribute(UserRepresentation user) {
    var attribute = user.getAttributes();
    return Objects.nonNull(attribute) && Objects
        .nonNull(attribute.get(KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE));
  }

  private UserRepresentation getUserRepresentation(RealmResource realmResource, String userName) {
    var users = client.getUsersRepresentationByUsername(realmResource, userName);

    if (users.size() != 1) {
      throw new KeycloakException(
          String.format("Found %d users with name %s, but expect one", users.size(), userName));
    }
    return users.get(0);
  }
}
