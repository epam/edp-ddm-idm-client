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

package com.epam.digital.data.platform.integration.idm.resource;

import com.epam.digital.data.platform.integration.idm.model.SearchUserQuery;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.keycloak.representations.idm.UserRepresentation;

@Path("/realms/{realm}/users/search")
@Consumes(MediaType.APPLICATION_JSON)
public interface UsersExtendedResource {

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  List<UserRepresentation> searchUsersByAttributes(@PathParam("realm") String realm,
      SearchUserQuery searchUserRequestDto);

}