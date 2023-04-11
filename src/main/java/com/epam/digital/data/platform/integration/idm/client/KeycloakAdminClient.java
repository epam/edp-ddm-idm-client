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

import com.epam.digital.data.platform.integration.idm.model.SearchUserQuery;
import com.epam.digital.data.platform.integration.idm.exception.KeycloakException;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesResponseDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByEqualsAndStartsWithAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.resource.UsersExtendedResource;
import com.google.common.collect.Maps;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cloud.sleuth.annotation.NewSpan;

/**
 * The client for communication with keycloak admin rest endpoints.
 */
@Slf4j
@RequiredArgsConstructor
public class KeycloakAdminClient {

  private final String realm;
  private final String serverUrl;
  private final Keycloak keycloak;


  /**
   * Retrieve keycloak realm resource.
   *
   * @return realm resource
   */
  @NewSpan
  public RealmResource getRealmResource() {
    log.info("Selecting keycloak realm {}", realm);
    var result = wrapKeycloakRequest(() -> keycloak.realm(realm),
        () -> String.format("Couldn't find realm %s", realm));
    log.info("Keycloak realm {} found", realm);

    return result;
  }

  /**
   * Retrieve list of keycloak role representations by realm resource
   *
   * @param realmResource realm resource
   * @return list of role representations
   */
  @NewSpan
  public List<RoleRepresentation> getKeycloakRoles(RealmResource realmResource) {
    log.info("Selecting keycloak roles in realm {}", realm);
    var keycloakRoles = wrapKeycloakRequest(() -> realmResource.roles().list(),
        () -> String.format("Couldn't select roles from realm %s", realm));
    log.info("Founded {} keycloak roles in realm {}", keycloakRoles.size(), realm);
    return keycloakRoles;
  }

  /**
   * Retrieve keycloak user representation by realm resource and username
   *
   * @param realmResource realm resource
   * @param username      user name
   * @return user representation
   */
  @NewSpan
  public List<UserRepresentation> getUsersRepresentationByUsername(RealmResource realmResource,
      String username) {

    log.info("Finding user {} in keycloak realm {}", username, realm);
    var users = wrapKeycloakRequest(() -> realmResource.users().search(username, true),
        () -> String.format("Couldn't find users %s in realm %s", username, realm));
    log.info("Found {} users with username {} in realm {}", users.size(), username, realm);
    return users;
  }

  /**
   * Retrieve keycloak role representation by realm resource and role name
   *
   * @param realmResource realm resource
   * @param role          role name
   * @return role representation
   */
  @NewSpan
  public RoleRepresentation getRoleRepresentation(RealmResource realmResource, String role) {
    log.info("Finding role {} in keycloak realm {}", role, realm);
    var result = wrapKeycloakRequest(() -> realmResource.roles().get(role).toRepresentation(),
        () -> String.format("Couldn't find role %s in realm %s", role, realm));
    log.info("Role {} in realm {} is found", role, realm);
    return result;
  }

  /**
   * Retrieve list of keycloak users by realm resource and role name
   *
   * @param realmResource realm resource
   * @param role          role name
   * @return list of users
   */
  @NewSpan
  public Set<UserRepresentation> getRoleUserMembers(RealmResource realmResource, String role) {
    log.info("Selecting keycloak users with role {} in realm {}", role, realm);
    var roleUserMembers = wrapKeycloakRequest(
        () -> realmResource.roles().get(role).getRoleUserMembers(),
        () -> String.format("Couldn't get keycloak users with role %s in realm %s", role,
            realm));
    log.info("Selected {} users with role {} in realm {}", roleUserMembers.size(), role, realm);
    return roleUserMembers;
  }

  /**
   * Retrieve keycloak role scope resource by realm resource and user id
   *
   * @param realmResource realm resource
   * @param userId        user identifier
   * @return role scope resource
   */
  @NewSpan
  public RoleScopeResource getRoleScopeResource(RealmResource realmResource, String userId) {
    log.info("Finding keycloak role scope resource by userId {} in realm {}", userId, realm);
    var result = wrapKeycloakRequest(() -> realmResource.users().get(userId).roles().realmLevel(),
        () -> String
            .format("Couldn't find keycloak role scope resource by userId %s in realm %s", userId,
                realm));
    log.info("Found role scope resource by userId {} in realm {}", userId, realm);
    return result;
  }

  /**
   * Remove role from keycloak user
   *
   * @param roleScopeResource role scope resource
   * @param roles             role representations to remove
   */
  @NewSpan
  public void removeRoles(RoleScopeResource roleScopeResource, List<RoleRepresentation> roles) {
    log.info("Removing roles {} from user", roles);
    wrapKeycloakVoidRequest(() -> roleScopeResource.remove(roles),
        () -> String.format("Couldn't remove roles %s from user", roles));
    log.info("Roles {} removed from user", roles);
  }

  /**
   * Add role to keycloak user
   *
   * @param roleScopeResource role scope resource
   * @param roles             role representations to add
   */
  @NewSpan
  public void addRoles(RoleScopeResource roleScopeResource, List<RoleRepresentation> roles) {
    log.info("Adding roles {} to user", roles);
    wrapKeycloakVoidRequest(() -> roleScopeResource.add(roles),
        () -> String.format("Couldn't add roles %s to user", roles));
    log.info("Roles {} added to user", roles);
  }

  /**
   * @return current service account access token
   *
   * @throws KeycloakException in case of any error
   */
  @NewSpan
  public String getClientAccessToken() {
    return wrapKeycloakRequest(() -> keycloak.tokenManager().getAccessTokenString(),
        () -> String.format("Couldn't get access token, realm %s", realm));
  }

  /**
   * Retrieve users with certain custom attributes
   *
   * @param searchRequest search request with required attributes map
   * @return users that have specified attributes
   *
   * @see SearchUserQuery
   * @deprecated use
   * {@link KeycloakAdminClient#searchUsersByAttributes(SearchUsersByAttributesRequestDto)} instead
   */
  @NewSpan
  @Deprecated(forRemoval = true)
  public List<UserRepresentation> searchUsersByAttributes(SearchUserQuery searchRequest) {
    return wrapKeycloakRequest(() -> keycloak.proxy(UsersExtendedResource.class, URI.create(
                serverUrl))
            .searchUsersByAttributes(realm, searchRequest),
        () -> String.format("Couldn't find users by attributes in realm %s", realm));
  }

  /**
   * Retrieve users with certain custom attributes
   *
   * @param searchRequestDto search request with required attributes map
   * @return users that have specified attributes
   *
   * @see SearchUsersByEqualsAndStartsWithAttributesRequestDto
   * @deprecated use
   * {@link KeycloakAdminClient#searchUsersByAttributes(SearchUsersByAttributesRequestDto)} instead
   */
  @NewSpan
  @Deprecated(forRemoval = true)
  public List<UserRepresentation> searchUsersByAttributes(
      SearchUsersByEqualsAndStartsWithAttributesRequestDto searchRequestDto) {
    return
        wrapKeycloakRequest(() -> keycloak.proxy(UsersExtendedResource.class, URI.create(serverUrl))
                .searchUsersByAttributes(realm, searchRequestDto),
            () -> String.format("Couldn't find users by attributes in realm %s", realm));
  }

  /**
   * Retrieve users with certain custom attributes
   *
   * @param requestDto search request with required attributes map
   * @return users that have specified attributes
   *
   * @see SearchUsersByAttributesRequestDto
   */
  @NewSpan
  public SearchUsersByAttributesResponseDto searchUsersByAttributes(
      SearchUsersByAttributesRequestDto requestDto) {
    return
        wrapKeycloakRequest(() -> keycloak.proxy(UsersExtendedResource.class, URI.create(serverUrl))
                .searchUsersByAttributes(realm, requestDto),
            () -> String.format("Couldn't find users by attributes in realm %s", realm));
  }

  @NewSpan
  public void saveUserAttribute(RealmResource realmResource, String userId, String attributeName,
      List<String> values) {
    log.info("Saving user attribute {} in realm {} ", attributeName, realm);
    var userResource = realmResource.users().get(userId);
    var userRepresentation = userResource.toRepresentation();
    if (Objects.isNull(userRepresentation.getAttributes())) {
      userRepresentation.setAttributes(Maps.newHashMap());
    }
    userRepresentation.getAttributes().put(attributeName, values);
    userResource.update(userRepresentation);
    log.info("User attribute {} is saved in realm {}", attributeName, realm);
  }

  private <T> T wrapKeycloakRequest(Supplier<T> supplier, Supplier<String> failMessageSupplier) {
    try {
      return supplier.get();
    } catch (RuntimeException exception) {
      throw new KeycloakException(failMessageSupplier.get(), exception);
    }
  }

  private void wrapKeycloakVoidRequest(Runnable runnable, Supplier<String> failMessageSupplier) {
    try {
      runnable.run();
    } catch (RuntimeException exception) {
      throw new KeycloakException(failMessageSupplier.get(), exception);
    }
  }
}
