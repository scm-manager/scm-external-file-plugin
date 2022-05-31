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
}
