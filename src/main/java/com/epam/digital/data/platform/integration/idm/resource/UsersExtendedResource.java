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

package com.epam.digital.data.platform.integration.idm.resource;

import com.epam.digital.data.platform.integration.idm.model.SearchUserQuery;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByAttributesResponseDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByEqualsAndStartsWithAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByRoleAndAttributesRequestDto;
import com.epam.digital.data.platform.integration.idm.model.SearchUsersByRoleAndAttributesResponseDto;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.keycloak.representations.idm.UserRepresentation;

@Path("/realms/{realm}/users")
@Consumes(MediaType.APPLICATION_JSON)
public interface UsersExtendedResource {

  /**
   * @deprecated use
   * {@link UsersExtendedResource#searchUsersByAttributes(String,
   * SearchUsersByAttributesRequestDto)} instead
   */
  @POST
  @Path("/search")
  @Consumes(MediaType.APPLICATION_JSON)
  @Deprecated(forRemoval = true)
  List<UserRepresentation> searchUsersByAttributes(@PathParam("realm") String realm,
      SearchUserQuery searchUserRequestDto);

  /**
   * @deprecated use
   * {@link UsersExtendedResource#searchUsersByAttributes(String,
   * SearchUsersByAttributesRequestDto)} instead
   */
  @POST
  @Path("/search-by-attributes")
  @Consumes(MediaType.APPLICATION_JSON)
  @Deprecated(forRemoval = true)
  List<UserRepresentation> searchUsersByAttributes(@PathParam("realm") String realm,
      SearchUsersByEqualsAndStartsWithAttributesRequestDto searchUserRequestDto);

  @POST
  @Path("/v2/search-by-attributes")
  @Consumes(MediaType.APPLICATION_JSON)
  SearchUsersByAttributesResponseDto searchUsersByAttributes(@PathParam("realm") String realm,
      SearchUsersByAttributesRequestDto requestDto);

  @POST
  @Path("/search-by-role-and-attributes")
  @Consumes(MediaType.APPLICATION_JSON)
  SearchUsersByRoleAndAttributesResponseDto searchUsersByRoleAndAttributes(@PathParam("realm") String realm,
      SearchUsersByRoleAndAttributesRequestDto requestDto);
}