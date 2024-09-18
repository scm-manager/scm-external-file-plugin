/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.externalfile;

import com.cloudogu.jaxrstie.GenerateLinkBuilder;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

import static com.cloudogu.externalfile.ExternalFileResource.PATH;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@GenerateLinkBuilder(className = "RestAPI")
@Path(PATH)
public class ExternalFileResource {

  static final String PATH = "v2/external-file";
  static final String MEDIA_TYPE = VndMediaType.PREFIX + "externalFile" + VndMediaType.SUFFIX;
  private final RepositoryManager repositoryManager;
  private final ExternalFileService service;

  @Inject
  ExternalFileResource(RepositoryManager repositoryManager, ExternalFileService service) {
    this.repositoryManager = repositoryManager;
    this.service = service;
  }

  @GET
  @Produces(TEXT_PLAIN)
  @Path("{namespace}/{name}/{path: .*}")
  public Response getExternalFile(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("path") String path) throws IOException {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    String url = service.get(repository, path);
    return Response.ok(url).build();
  }

  @POST
  @Consumes(MEDIA_TYPE)
  @Path("{namespace}/{name}")
  public Response createExternalFile(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid CreateExternalFileDto dto) throws IOException {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    service.create(repository, dto);
    return Response.noContent().build();
  }

  @PUT
  @Consumes(MEDIA_TYPE)
  @Path("{namespace}/{name}/{path: .*}")
  public Response modifyExternalFile(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("path") String path, @Valid ModifyExternalFileDto dto) throws IOException {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    service.modify(repository, dto.getBranch(), path, dto.getUrl(), dto.getCommitMessage());
    return Response.noContent().build();
  }
}
