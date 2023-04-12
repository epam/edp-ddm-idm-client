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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.integration.idm.client.KeycloakAdminClient;
import com.epam.digital.data.platform.integration.idm.mapper.IdmUsersMapper;
import com.epam.digital.data.platform.integration.idm.model.KeycloakSystemAttribute;
import com.epam.digital.data.platform.integration.idm.model.SearchUserQuery;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesResponseDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesResponseDto.Pagination;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.bytebuddy.utility.RandomString;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KeycloakIdmServiceTest {

  public static final String TEST_ROLE = "testRole";
  public static final String TEST_USERNAME = "testUsername";
  public static final String TEST_USER_ID = "testUserId";
  @Mock
  private KeycloakAdminClient client;
  private KeycloakIdmService service;
  @Mock
  private RoleScopeResource roleScopeResource;
  @Mock
  private RoleRepresentation roleRepresentation;
  @Mock
  private UserRepresentation userRepresentation;
  @Mock
  private RealmResource realmResource;
  @Spy
  private IdmUsersMapper idmUsersMapper = Mappers.getMapper(IdmUsersMapper.class);

  @BeforeEach
  void setUp() {
    service = new KeycloakIdmService(client, idmUsersMapper);
  }

  @Test
  void getRoles() {
    when(client.getRealmResource()).thenReturn(realmResource);
    when(client.getKeycloakRoles(realmResource)).thenReturn(
        Collections.singletonList(roleRepresentation));
    when(roleRepresentation.getName()).thenReturn(TEST_ROLE);

    var roles = service.getRoles();
    assertThat(roles).hasSize(1);
    assertThat(roles.get(0).getName()).isEqualTo(TEST_ROLE);
  }

  @Test
  void getRoleRepresentations() {
    when(client.getRealmResource()).thenReturn(realmResource);
    when(client.getKeycloakRoles(realmResource)).thenReturn(
        Collections.singletonList(roleRepresentation));
    when(roleRepresentation.getName()).thenReturn(TEST_ROLE);

    var roles = service.getRoleRepresentations();
    assertThat(roles).hasSize(1);
    assertThat(roles.get(0).getName()).isEqualTo(TEST_ROLE);
  }

  @Test
  void removeRole() {
    var usersList = Collections.singletonList(userRepresentation);

    when(client.getRealmResource()).thenReturn(realmResource);
    when(userRepresentation.getId()).thenReturn(TEST_USER_ID);
    when(client.getRoleRepresentation(realmResource, TEST_ROLE)).thenReturn(roleRepresentation);
    when(client.getUsersRepresentationByUsername(realmResource, TEST_USERNAME)).thenReturn(
        usersList);
    when(client.getRoleScopeResource(realmResource, TEST_USER_ID)).thenReturn(roleScopeResource);

    service.removeRole(TEST_USERNAME, TEST_ROLE);
    verify(client).removeRoles(roleScopeResource, List.of(roleRepresentation));
  }

  @Test
  void removeRoles() {
    var usersList = Collections.singletonList(userRepresentation);
    var roles = List.of(roleRepresentation);

    when(client.getRealmResource()).thenReturn(realmResource);
    when(userRepresentation.getId()).thenReturn(TEST_USER_ID);
    when(client.getUsersRepresentationByUsername(realmResource, TEST_USERNAME)).thenReturn(
        usersList);
    when(client.getRoleScopeResource(realmResource, TEST_USER_ID)).thenReturn(roleScopeResource);

    service.removeRoles(TEST_USERNAME, roles);
    verify(client).removeRoles(roleScopeResource, roles);
    verify(client, times(0)).getRoleRepresentation(any(), any());
  }

  @Test
  void addRole() {
    var usersList = Collections.singletonList(userRepresentation);

    when(client.getRealmResource()).thenReturn(realmResource);
    when(userRepresentation.getId()).thenReturn(TEST_USER_ID);
    when(client.getRoleRepresentation(realmResource, TEST_ROLE)).thenReturn(roleRepresentation);
    when(client.getUsersRepresentationByUsername(realmResource, TEST_USERNAME)).thenReturn(
        usersList);
    when(client.getRoleScopeResource(realmResource, TEST_USER_ID)).thenReturn(roleScopeResource);

    service.addRole(TEST_USERNAME, TEST_ROLE);
    verify(client).addRoles(roleScopeResource, List.of(roleRepresentation));
  }

  @Test
  void addRoles() {
    var usersList = Collections.singletonList(userRepresentation);
    var roles = List.of(roleRepresentation);

    when(client.getRealmResource()).thenReturn(realmResource);
    when(userRepresentation.getId()).thenReturn(TEST_USER_ID);
    when(client.getUsersRepresentationByUsername(realmResource, TEST_USERNAME)).thenReturn(
        usersList);
    when(client.getRoleScopeResource(realmResource, TEST_USER_ID)).thenReturn(roleScopeResource);

    service.addRoles(TEST_USERNAME, roles);
    verify(client).addRoles(roleScopeResource, roles);
    verify(client, times(0)).getRoleRepresentation(any(), any());
  }

  @Test
  void getRoleUserMembers() {
    when(client.getRealmResource()).thenReturn(realmResource);
    when(client.getRoleUserMembers(realmResource, TEST_ROLE)).thenReturn(
        Set.of(userRepresentation));
    when(userRepresentation.getUsername()).thenReturn(TEST_USERNAME);
    when(userRepresentation.getAttributes()).thenReturn(Map.of("fullName", List.of("fullName")));

    var roleUserMembers = service.getRoleUserMembers(TEST_ROLE);
    assertThat(roleUserMembers).hasSize(1);
    assertThat(roleUserMembers.get(0).getUserName()).isEqualTo(TEST_USERNAME);
  }

  @Test
  void searchUsers() {
    var edr123 = "123";
    var fullName = "fullName";
    var query = SearchUserQuery.builder().edrpou(edr123).build();
    var attributes = new HashMap<String, List<String>>();
    attributes.put(fullName, List.of(fullName));
    attributes.put("edrpou", List.of(edr123));

    when(client.searchUsersByAttributes(query)).thenReturn(List.of(userRepresentation));
    when(userRepresentation.getUsername()).thenReturn(TEST_USERNAME);
    when(userRepresentation.getAttributes()).thenReturn(attributes);

    var users = service.searchUsers(query);
    assertThat(users).hasSize(1);
    assertThat(users.get(0).getUserName()).isEqualTo(TEST_USERNAME);
  }

  @Test
  void searchUsersByAttributes() {
    var searchRequestDto = SearchUsersByAttributesRequestDto.builder().build();
    var user = new UserRepresentation();
    user.setId("someId");
    user.setUsername("john_doe");
    user.setAttributes(Map.of(
        KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE, List.of("John Doe"),
        "attribute1", List.of("value1", "value2")
    ));
    var userWithoutFullName = new UserRepresentation();
    var searchResponseDto = new SearchUsersByAttributesResponseDto();
    searchResponseDto.setUsers(List.of(user, userWithoutFullName));
    var pagination = new Pagination();
    pagination.setContinueToken(1);
    searchResponseDto.setPagination(pagination);

    Mockito.doReturn(searchResponseDto).when(client).searchUsersByAttributes(searchRequestDto);

    var actualIdmUserResponse = service.searchUsers(searchRequestDto);

    Assertions.assertThat(actualIdmUserResponse)
        .hasFieldOrProperty("users")
        .hasFieldOrProperty("pagination");
    Assertions.assertThat(actualIdmUserResponse.getUsers())
        .hasSize(1)
        .element(0)
        .hasFieldOrPropertyWithValue("id", "someId")
        .hasFieldOrPropertyWithValue("userName", "john_doe")
        .hasFieldOrPropertyWithValue("fullName", "John Doe")
        .hasFieldOrPropertyWithValue("attributes", Map.of(
            KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE, List.of("John Doe"),
            "attribute1", List.of("value1", "value2")));
    Assertions.assertThat(actualIdmUserResponse.getPagination())
        .hasFieldOrPropertyWithValue("continueToken", 1);
  }


  @Test
  void getClientAccessToken() {
    var token = "token";

    when(client.getClientAccessToken()).thenReturn(token);

    var clientAccessToken = service.getClientAccessToken();
    verify(client, times(1)).getClientAccessToken();
    assertThat(clientAccessToken).isEqualTo(token);
  }

  @Test
  void testSaveAttribute() {
    var userId = "userId";
    var attributeValue = List.of("UA003");
    when(client.getRealmResource()).thenReturn(realmResource);
    when(client.getUsersRepresentationByUsername(realmResource, TEST_USERNAME)).thenReturn(
        List.of(userRepresentation));
    when(userRepresentation.getId()).thenReturn(userId);
    service.saveUserAttribute(TEST_USERNAME, KeycloakSystemAttribute.KATOTTG, attributeValue);
    verify(client, times(1)).saveUserAttribute(realmResource, userId,
        KeycloakSystemAttribute.KATOTTG, attributeValue);
  }

  @Test
  void getUserRole() {
    var username = RandomString.make();
    var usersList = Collections.singletonList(userRepresentation);

    when(client.getRealmResource()).thenReturn(realmResource);
    when(client.getUsersRepresentationByUsername(realmResource, username)).thenReturn(usersList);
    when(client.getRoleScopeResource(realmResource, userRepresentation.getId()))
        .thenReturn(roleScopeResource);
    when(roleScopeResource.listAll()).thenReturn(List.of(roleRepresentation));

    var userRoles = service.getUserRoles(username);
    assertThat(userRoles).isNotNull();
    assertThat(userRoles).isNotEmpty();
    assertThat(roleRepresentation).isEqualTo(userRoles.get(0));
    verify(roleScopeResource).listAll();
  }

  @Test
  void getUserRepresentationByUserName() {
    when(client.getRealmResource()).thenReturn(realmResource);
    when(client.getUsersRepresentationByUsername(realmResource, TEST_USERNAME)).thenReturn(
        List.of(userRepresentation));

    var user = service.getUserRepresentationByUserName(TEST_USERNAME);
    AssertionsForClassTypes.assertThat(user).isEqualTo(userRepresentation);
  }

  @Test
  void updateUserRepresentation() {
    when(client.getRealmResource()).thenReturn(realmResource);

    service.updateUserRepresentation(userRepresentation);
    verify(client).updateUserRepresentation(realmResource, userRepresentation);
  }

  @Test
  void createUserRepresentation() {
    var roles = List.of(roleRepresentation);
    var usersList = Collections.singletonList(userRepresentation);
    when(client.getRealmResource()).thenReturn(realmResource);
    when(userRepresentation.getUsername()).thenReturn(TEST_USERNAME);
    when(userRepresentation.getId()).thenReturn(TEST_USER_ID);
    when(client.getUsersRepresentationByUsername(realmResource, TEST_USERNAME)).thenReturn(
        usersList);
    when(client.getRoleScopeResource(realmResource, TEST_USER_ID)).thenReturn(roleScopeResource);

    service.createUserRepresentation(userRepresentation, roles);

    verify(client).createUserRepresentation(realmResource, userRepresentation);
    verify(client).addRoles(roleScopeResource, roles);
  }
}
