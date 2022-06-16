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

import com.cloudogu.scm.editor.ChangeGuardCheck;
import com.cloudogu.scm.editor.ChangeObstacle;
import io.micrometer.core.instrument.util.IOUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.ModifyCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class ExternalFileServiceTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService repositoryService;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ModifyCommandBuilder modifyCommandBuilder;
  @Mock
  private ModifyCommandBuilder.WithOverwriteFlagContentLoader contentLoader;

  @Mock
  private ChangeGuardCheck changeGuardCheck;

  @InjectMocks
  private ExternalFileService service;

  @BeforeEach
  void initService() {
    lenient().when(serviceFactory.create(repository)).thenReturn(repositoryService);
    lenient().when(repositoryService.getModifyCommand()).thenReturn(modifyCommandBuilder);
    repository.setId("1");
  }

  @Test
  @SubjectAware(value = "trillian")
  void shouldThrowAuthorizationException() {
    CreateExternalFileDto dto = new CreateExternalFileDto("https://test.url/", "my-first-link", "", "Add new link");

    assertThrows(UnauthorizedException.class, () -> service.create(repository, dto));
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "repository:modify:1")
  void shouldCreateExternalFile() throws IOException {
    CreateExternalFileDto dto = new CreateExternalFileDto("https://test.url/", "my-first-link", "", "Add new link");

    service.create(repository, dto);

    verify(modifyCommandBuilder).createFile("my-first-link.URL");
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "repository:modify:1")
  void shouldModifyExternalFile() throws IOException {
    ModifyExternalFileDto dto = new ModifyExternalFileDto("https://test.url/", null, "Modify existing link");

    service.modify(repository, dto.getBranch(), "my-first-link", dto.getUrl(), dto.getCommitMessage());

    verify(modifyCommandBuilder).modifyFile("my-first-link.URL");
  }

  @Test
  @SubjectAware(value = "trillian")
  void shouldNotAllowToModifyWithoutPermissions() {
    ModifyExternalFileDto dto = new ModifyExternalFileDto("https://test.url/", null, "Modify existing link");

    assertThrows(UnauthorizedException.class, () -> service.modify(repository, dto.getBranch(), "my-first-link", dto.getUrl(), dto.getCommitMessage()));
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "repository:modify:1")
  void shouldNotAllowToModifyIfThereIsAChangeObstacle() {
    when(changeGuardCheck.isModifiable(repository.getNamespaceAndName(), null, "my-first-link")).thenReturn(List.of(new ChangeObstacle() {
      @Override
      public String getMessage() {
        return "NOPE";
      }

      @Override
      public String getKey() {
        return "MyObstacle";
      }
    }));
    ModifyExternalFileDto dto = new ModifyExternalFileDto("https://test.url/", null, "Modify existing link");

    String branch = dto.getBranch();
    String url = dto.getUrl();
    String commitMessage = dto.getCommitMessage();
    assertThrows(ChangeNotAllowedException.class, () -> service.modify(repository, branch, "my-first-link", url, commitMessage));
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "repository:modify:1")
  void shouldChangeSuffixWithLowerCaseLettersToUpperCase() throws IOException {
    CreateExternalFileDto dto = new CreateExternalFileDto("https://test.url/", "my-first-link.uRl", "", "Add new link");

    service.create(repository, dto);

    verify(modifyCommandBuilder).createFile("my-first-link.URL");
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "repository:modify:1")
  void shouldCreateExternalFileWithNestedPath() throws IOException {
    CreateExternalFileDto dto = new CreateExternalFileDto("https://test.url/", "nested/dir/my-first-link", "", "Add new link");

    service.create(repository, dto);

    verify(modifyCommandBuilder).createFile("nested/dir/my-first-link.URL");
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "repository:modify:1")
  void shouldCreateExternalFileWithFileTemplate() throws IOException {
    CreateExternalFileDto dto = new CreateExternalFileDto("https://test.url/", "my-first-link.txt", "", "Add new link");
    ArgumentCaptor<ByteArrayInputStream> bais = ArgumentCaptor.forClass(ByteArrayInputStream.class);
    when(modifyCommandBuilder.createFile(any())).thenReturn(contentLoader);
    when(contentLoader.withData(bais.capture())).thenReturn(modifyCommandBuilder);

    service.create(repository, dto);

    assertThat(IOUtils.toString(bais.getValue(), StandardCharsets.UTF_8)).isEqualTo("[InternetShortcut]\nURL=https://test.url/");
  }
}
