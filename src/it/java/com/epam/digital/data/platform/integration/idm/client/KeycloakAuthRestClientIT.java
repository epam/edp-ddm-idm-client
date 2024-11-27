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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.epam.digital.data.platform.integration.idm.config.WireMockConfig;
import com.epam.digital.data.platform.integration.idm.model.KeycloakSystemAttribute;
import com.epam.digital.data.platform.integration.idm.model.SearchUserQuery;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesRequestDto.Pagination;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByRoleAndAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.integration.idm.service.PublicIdmService;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {WireMockConfig.class, TestIdmConfig.class})
@EnableFeignClients
class KeycloakAuthRestClientIT {

  @Autowired
  protected WireMockServer keycloakMockServer;
  @Autowired
  private PublicIdmService publicIdmService;
  @Autowired
  private IdmService idmService;

  private final String realm = "testRealm";

  @BeforeEach
  public void mockKeycloak() {
    keycloakMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/auth/realms/" + realm))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(jsonToStr("/json/keycloakResponse.json")))));

    keycloakMockServer.addStubMapping(
        stubFor(post("/auth/realms/" + realm + "/protocol/openid-connect/token")
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(jsonToStr("/json/keycloakTokenResponse.json")))));

    keycloakMockServer.addStubMapping(
        stubFor(
            post(urlPathEqualTo("/auth/realms/" + realm + "/users/search")).withRequestBody(
                    equalToJson("{\"attributes\" : {\"drfo\" : \"123\"}}"))
                .willReturn(aResponse().withStatus(200)
                    .withHeader("Content-type", "application/json")
                    .withBody(jsonToStr("/json/keycloakUserSearchResponse.json")))));
    keycloakMockServer.addStubMapping(
        stubFor(
            get(urlPathEqualTo("/auth/admin/realms/" + realm + "/users"))
                .withQueryParam("username", equalTo("username"))
                .willReturn(aResponse().withStatus(200)
                    .withHeader("Content-type", "application/json")
                    .withBody(jsonToStr("/json/keycloakUserByUsernameResponse.json")))));

  }

  @Test
  void testGetRealmRepresentation() {
    var result = publicIdmService.getRealm(realm);
    assertThat(result).isNotNull();
    assertThat(result.getPublicKey()).isNotNull();
  }

  @Test
  void testSearchUser() {
    var users = idmService.searchUsers(SearchUserQuery.builder().drfo("123").build());
    assertThat(users.size()).isEqualTo(1);
    assertThat(users.get(0).getUserName()).isEqualTo("123");
  }

  @Test
  void testSearchUsersByAttributes() {
    keycloakMockServer.addStubMapping(stubFor(
        post(urlPathEqualTo("/auth/realms/" + realm + "/users/v2/search-by-attributes"))
            .withRequestBody(
                equalToJson(jsonToStr("/json/keycloakSearchUserByAttributesRequest.json")))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(jsonToStr("/json/keycloakSearchUserByAttributesResponse.json")))));

    var searchRequestDto = SearchUsersByAttributesRequestDto.builder()
        .attributesStartsWith(Map.of("hierarchy", List.of("100")))
        .pagination(Pagination.builder().continueToken(1022).limit(2).build())
        .build();

    var actualIdmUserResponse = idmService.searchUsers(searchRequestDto);

    Assertions.assertThat(actualIdmUserResponse)
        .hasFieldOrProperty("users")
        .hasFieldOrProperty("pagination");
    Assertions.assertThat(actualIdmUserResponse.getUsers())
        .hasSize(2)
        .element(0)
        .hasFieldOrPropertyWithValue("id", "someNextId")
        .hasFieldOrPropertyWithValue("userName", "jane_doe")
        .hasFieldOrPropertyWithValue("fullName", "Jane Doe")
        .hasFieldOrPropertyWithValue("attributes", Map.of(
            KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE, List.of("Jane Doe"),
            KeycloakSystemAttribute.DRFO, List.of("1234567890"),
            "hierarchy", List.of("100.200", "101")));
    Assertions.assertThat(actualIdmUserResponse.getUsers())
        .hasSize(2)
        .element(1)
        .hasFieldOrPropertyWithValue("id", "someId")
        .hasFieldOrPropertyWithValue("userName", "john_doe")
        .hasFieldOrPropertyWithValue("fullName", "John Doe")
        .hasFieldOrPropertyWithValue("attributes", Map.of(
            KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE, List.of("John Doe"),
            KeycloakSystemAttribute.DRFO, List.of("1234567891"),
            "hierarchy", List.of("100")));
    Assertions.assertThat(actualIdmUserResponse.getPagination())
        .hasFieldOrPropertyWithValue("continueToken", 1025);
  }

  @Test
  void testSearchUsersByRoleAndAttributes() {
    keycloakMockServer.addStubMapping(stubFor(
        post(urlPathEqualTo("/auth/realms/" + realm + "/users/search-by-role-and-attributes"))
            .withRequestBody(
                equalToJson(jsonToStr("/json/keycloakSearchUserByRoleAndAttributesRequest.json")))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(jsonToStr("/json/keycloakSearchUserByRoleAndAttributesResponse.json")))));

    var searchRequestDto = SearchUsersByRoleAndAttributesRequestDto.builder()
        .attributesStartsWith(Map.of("hierarchy", List.of("100")))
        .pagination(SearchUsersByRoleAndAttributesRequestDto.OffsetPagination.builder().offset(0).limit(2).build())
        .build();

    var actualIdmUserResponse = idmService.searchUsersByRoleAndAttributes(searchRequestDto);

    Assertions.assertThat(actualIdmUserResponse)
        .hasFieldOrProperty("users");
    Assertions.assertThat(actualIdmUserResponse.getUsers())
        .hasSize(2)
        .element(0)
        .hasFieldOrPropertyWithValue("id", "someNextId")
        .hasFieldOrPropertyWithValue("userName", "jane_doe")
        .hasFieldOrPropertyWithValue("fullName", "Jane Doe")
        .hasFieldOrPropertyWithValue("attributes", Map.of(
            KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE, List.of("Jane Doe"),
            KeycloakSystemAttribute.DRFO, List.of("1234567890"),
            "hierarchy", List.of("100.200", "101")));
    Assertions.assertThat(actualIdmUserResponse.getUsers())
        .element(1)
        .hasFieldOrPropertyWithValue("id", "someId")
        .hasFieldOrPropertyWithValue("userName", "john_doe")
        .hasFieldOrPropertyWithValue("fullName", "John Doe")
        .hasFieldOrPropertyWithValue("attributes", Map.of(
            KeycloakSystemAttribute.FULL_NAME_ATTRIBUTE, List.of("John Doe"),
            KeycloakSystemAttribute.DRFO, List.of("1234567891"),
            "hierarchy", List.of("100")));
  }

  @Test
  void testGetUserByUsername() {
    var username = "username";
    var result = idmService.getUserByUserName(username);
    assertThat(result.size()).isOne();
    assertThat(result.get(0).getId()).isEqualTo("testId");
  }

  @SneakyThrows
  public static String jsonToStr(String content) {
    return Files.readString(
        Paths.get(KeycloakAuthRestClientIT.class.getResource(content).toURI()),
        StandardCharsets.UTF_8);
  }
}