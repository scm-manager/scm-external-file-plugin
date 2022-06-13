/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cloudogu.externalfile;

import com.cloudogu.jaxrstie.GenerateLinkBuilder;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.io.IOException;

import static com.cloudogu.externalfile.ExternalFileResource.PATH;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

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
