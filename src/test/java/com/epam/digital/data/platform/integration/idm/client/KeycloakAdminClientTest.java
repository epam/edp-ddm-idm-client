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

package com.epam.digital.data.platform.integration.idm.client;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.integration.idm.exception.KeycloakException;
import com.epam.digital.data.platform.integration.idm.model.KeycloakSystemAttribute;
import com.epam.digital.data.platform.integration.idm.model.SearchUserQuery;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesResponseDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesResponseDto.Pagination;
import com.epam.digital.data.platform.integration.idm.resource.UsersExtendedResource;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KeycloakAdminClientTest {

  private final String realm = "testRealm";
  private final String username = "username";
  private final String role = "role";

  @Mock
  private Keycloak keycloak;
  @Mock
  private RealmResource realmResource;
  @Mock
  private RolesResource rolesResource;
  @Mock
  private UsersResource usersResource;
  @Mock
  private RoleResource roleResource;
  @Mock
  private UserResource userResource;
  @Mock
  private RoleMappingResource roleMappingResource;
  @Mock
  private RoleScopeResource roleScopeResource;
  @Mock
  private TokenManager tokenManager;
  @Mock
  private Response createUserResponse;

  private KeycloakAdminClient client;

  @BeforeEach
  public void init() {
    client = new KeycloakAdminClient(realm, "testUrl", keycloak);
  }

  @Test
  void testGetRealmResource() {
    when(keycloak.realm(realm)).thenReturn(realmResource);

    var result = client.getRealmResource();
    assertThat(result).isEqualTo(realmResource);
  }

  @Test
  void testGetKeycloakRoles() {
    var roles = List.of(new RoleRepresentation(role, null, true));

    when(realmResource.roles()).thenReturn(rolesResource);
    when(rolesResource.list()).thenReturn(roles);

    var result = client.getKeycloakRoles(realmResource);
    assertThat(result.size()).isOne();
    assertThat(result.get(0)).isEqualTo(roles.get(0));
  }

  @Test
  void testGetUsersRepresentationByUsername() {
    var user = new UserRepresentation();
    user.setUsername(username);
    var users = List.of(user);

    when(realmResource.users()).thenReturn(usersResource);
    when(usersResource.search(username, true)).thenReturn(users);

    var result = client.getUsersRepresentationByUsername(realmResource, username);
    assertThat(result.size()).isOne();
    assertThat(result.get(0)).isEqualTo(user);
  }

  @Test
  void testGetRoleRepresentation() {
    var roleRep = new RoleRepresentation(role, null, true);

    when(realmResource.roles()).thenReturn(rolesResource);
    when(rolesResource.get(role)).thenReturn(roleResource);
    when(roleResource.toRepresentation()).thenReturn(roleRep);

    var result = client.getRoleRepresentation(realmResource, role);
    assertThat(result).isEqualTo(roleRep);
  }

  @Test
  void testGetRoleUserMembers() {
    var userRep = new UserRepresentation();
    userRep.setUsername(username);

    when(realmResource.roles()).thenReturn(rolesResource);
    when(rolesResource.get(role)).thenReturn(roleResource);
    when(roleResource.getRoleUserMembers()).thenReturn(Set.of(userRep));

    var result = client.getRoleUserMembers(realmResource, role);
    assertThat(result.size()).isOne();
    assertThat(result.iterator().next()).isEqualTo(userRep);
  }

  @Test
  void testGetRoleScopeResource() {
    var userId = "userId";

    when(realmResource.users()).thenReturn(usersResource);
    when(usersResource.get(userId)).thenReturn(userResource);
    when(userResource.roles()).thenReturn(roleMappingResource);
    when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

    var result = client.getRoleScopeResource(realmResource, userId);
    assertThat(result).isEqualTo(roleScopeResource);
  }

  @Test
  void testRemoveRole() {
    var roleRepresentation = new RoleRepresentation(role, null, true);
    var roleRepresentationList = List.of(roleRepresentation);

    client.removeRoles(roleScopeResource, roleRepresentationList);
    verify(roleScopeResource).remove(roleRepresentationList);
  }

  @Test
  void testAddRole() {
    var roleRepresentation = new RoleRepresentation(role, null, true);
    var roleRepresentationList = List.of(roleRepresentation);

    client.addRoles(roleScopeResource, roleRepresentationList);
    verify(roleScopeResource).add(roleRepresentationList);
  }

  @Test
  void testGetAccessTokenString() {
    var token = "token";

    when(keycloak.tokenManager()).thenReturn(tokenManager);
    when(tokenManager.getAccessTokenString()).thenReturn(token);

    var result = client.getClientAccessToken();
    assertThat(result).isEqualTo(token);
  }

  @Test
  void testRealmNotFound() {
    when(keycloak.realm(realm)).thenThrow(RuntimeException.class);

    var exception = assertThrows(KeycloakException.class,
        () -> client.getRealmResource());
    assertThat(exception.getMessage()).isEqualTo(String.format("Couldn't find realm %s", realm));
  }

  @Test
  void testRoleNotAdded() {
    var roleRepresentation = new RoleRepresentation(role, null, true);

    doThrow(new RuntimeException()).when(roleScopeResource).add(List.of(roleRepresentation));

    var exception = assertThrows(KeycloakException.class,
        () -> client.addRoles(roleScopeResource, List.of(roleRepresentation)));
    assertThat(exception.getMessage()).isEqualTo(
        String.format("Couldn't add roles [%s] to user", role));
  }

  @Test
  void testSearchUsersException() {
    var exception = assertThrows(KeycloakException.class,
        () -> client.searchUsersByAttributes(SearchUserQuery.builder().build()));
    assertThat(exception.getMessage()).isEqualTo(
        String.format("Couldn't find users by attributes in realm %s", realm));
  }

  @Test
  void testSearchUsers() {
    var userRepresentations = Collections.singletonList(mock(UserRepresentation.class));
    var searchRequest = SearchUserQuery.builder().build();
    var resource = mock(UsersExtendedResource.class);

    when(keycloak.proxy(UsersExtendedResource.class, URI.create("testUrl"))).thenReturn(resource);
    when(resource.searchUsersByAttributes(realm, searchRequest)).thenReturn(userRepresentations);
    var actual = client.searchUsersByAttributes(searchRequest);
    assertThat(actual.size()).isOne();
  }

  @Test
  void testSearchUsersByAttributes() {
    var searchRequest = SearchUsersByAttributesRequestDto.builder().build();
    var userRepresentations = Collections.singletonList(mock(UserRepresentation.class));
    var searchResponse = new SearchUsersByAttributesResponseDto();
    searchResponse.setUsers(userRepresentations);
    var pagination = new Pagination();
    pagination.setContinueToken(1);
    searchResponse.setPagination(pagination);

    var resource = mock(UsersExtendedResource.class);
    Mockito.doReturn(resource).when(keycloak)
        .proxy(UsersExtendedResource.class, URI.create("testUrl"));
    Mockito.doReturn(searchResponse).when(resource).searchUsersByAttributes(realm, searchRequest);

    var actual = client.searchUsersByAttributes(searchRequest);

    assertThat(actual).isSameAs(searchResponse);
  }

  @Test
  void testSearchUsersByAttributes_exception() {
    var searchRequest = SearchUsersByAttributesRequestDto.builder().build();

    var resource = mock(UsersExtendedResource.class);
    Mockito.doReturn(resource).when(keycloak)
        .proxy(UsersExtendedResource.class, URI.create("testUrl"));
    Mockito.doThrow(RuntimeException.class).when(resource)
        .searchUsersByAttributes(realm, searchRequest);

    assertThatThrownBy(() -> client.searchUsersByAttributes(searchRequest))
        .isInstanceOf(KeycloakException.class)
        .hasMessage("Couldn't find users by attributes in realm %s", realm);
  }

  @Test
  void testSaveUserAttribute() {
    var userId = "userId";
    var attributeValue = List.of("UA003");
    var user = new UserRepresentation();
    user.setUsername(username);
    when(realmResource.users()).thenReturn(usersResource);
    when(usersResource.get(userId)).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(user);

    client.saveUserAttribute(realmResource, userId, KeycloakSystemAttribute.KATOTTG,
        attributeValue);

    user.setAttributes(Map.of(KeycloakSystemAttribute.KATOTTG, attributeValue));
    verify(userResource).update(user);
  }

  @Test
  void testUpdateUserRepresentation() {
    var userId = "userId";
    var user = new UserRepresentation();
    user.setUsername(username);
    user.setId(userId);
    when(realmResource.users()).thenReturn(usersResource);
    when(usersResource.get(userId)).thenReturn(userResource);

    client.updateUserRepresentation(realmResource, user);
    verify(userResource).update(user);
  }

  @Test
  void testCreateUserRepresentation() {
    var user = new UserRepresentation();
    user.setUsername(username);
    when(realmResource.users()).thenReturn(usersResource);
    when(usersResource.create(user)).thenReturn(createUserResponse);
    when(createUserResponse.getStatus()).thenReturn(201);

    client.createUserRepresentation(realmResource, user);
    verify(usersResource).create(user);
    verify(createUserResponse).getStatus();
  }

  @Test
  void testCreateUserRepresentationWithDuplicatedUserName() {
    var user = new UserRepresentation();
    user.setUsername(username);
    when(realmResource.users()).thenReturn(usersResource);
    when(usersResource.create(user)).thenReturn(createUserResponse);
    when(createUserResponse.getStatus()).thenReturn(409);

    var exception = assertThrows(KeycloakException.class,
        () -> client.createUserRepresentation(realmResource, user));
    assertThat(exception.getMessage()).isEqualTo(
        String.format("Couldn't create user with username: %s", username));
  }
}