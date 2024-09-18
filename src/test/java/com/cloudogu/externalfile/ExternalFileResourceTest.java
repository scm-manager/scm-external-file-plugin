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

import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.RestDispatcher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class ExternalFileResourceTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private ExternalFileService service;

  @InjectMocks
  private ExternalFileResource resource;

  RestDispatcher dispatcher;
  MockHttpResponse response = new MockHttpResponse();

  @BeforeEach
  void initResource() {
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
    repository.setId("1");
    when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(repository);
  }

  @Test
  @SubjectAware(permissions = "repository:modify:1")
  void shouldCreateExternalFile() throws URISyntaxException, IOException {
    MockHttpRequest request =
      MockHttpRequest
        .post("/" + ExternalFileResource.PATH + "/" + repository.getNamespaceAndName().toString())
        .contentType(ExternalFileResource.MEDIA_TYPE)
        .content("{\"url\": \"https://test.url/\", \"path\": \"my-first-external-file\", \"commitMessage\": \"Create new file link\", \"branch\": \"main\"}".getBytes(StandardCharsets.UTF_8));

    dispatcher.invoke(request, response);

    verify(service).create(eq(repository), any(CreateExternalFileDto.class));
  }

  @Test
  @SubjectAware(permissions = "repository:modify:1")
  void shouldModifyExternalFile() throws URISyntaxException, IOException {
    MockHttpRequest request =
      MockHttpRequest
        .put("/" + ExternalFileResource.PATH + "/" + repository.getNamespaceAndName().toString() + "/my-first-external-file")
        .contentType(ExternalFileResource.MEDIA_TYPE)
        .content("{\"url\": \"https://test.url/\", \"commitMessage\": \"Update existing file link\", \"branch\": \"main\"}".getBytes(StandardCharsets.UTF_8));

    dispatcher.invoke(request, response);

    verify(service).modify(repository, "main", "my-first-external-file", "https://test.url/", "Update existing file link");
  }
}
