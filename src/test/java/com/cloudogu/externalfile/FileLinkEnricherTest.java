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

import com.cloudogu.scm.editor.ChangeGuardCheck;
import com.cloudogu.scm.editor.EditorPreconditions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import jakarta.inject.Provider;
import java.net.URI;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileLinkEnricherTest {

  @Mock
  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  private final Repository repository = RepositoryTestData.createHeartOfGold();
  @Mock
  private HalEnricherContext context;
  @Mock
  private HalAppender appender;

  @Mock
  private ChangeGuardCheck changeGuardCheck;

  @Mock
  private EditorPreconditions editorPreconditions;

  @InjectMocks
  private FileLinkEnricher enricher;

  @Test
  void shouldNotEnrichLinkForDirectory() {
    FileObject fileObject = new FileObject();
    fileObject.setDirectory(true);
    fileObject.setName("testfile.URL");
    BrowserResult result = createBrowserResult("42", "master", fileObject);
    setUpHalContext(repository, result, fileObject);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(any(), any());
  }

  @Test
  void shouldNotEnrichLinkForWrongFileExtension() {
    FileObject fileObject = new FileObject();
    fileObject.setDirectory(false);
    fileObject.setPath("testfile.LUR");
    BrowserResult result = createBrowserResult("42", "master", fileObject);
    setUpHalContext(repository, result, fileObject);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(any(), any());
  }

  @Test
  void shouldEnrichLink() {
    FileObject fileObject = new FileObject();
    fileObject.setDirectory(false);
    fileObject.setPath("nested/file/testfile.URL");
    BrowserResult result = createBrowserResult("42", "master", fileObject);
    setUpHalContext(repository, result, fileObject);

    ScmPathInfoStore scmPathInfoStore = mock(ScmPathInfoStore.class);
    when(scmPathInfoStoreProvider.get()).thenReturn(scmPathInfoStore);
    when(scmPathInfoStore.get()).thenReturn(() -> URI.create("/scm"));
    when(changeGuardCheck.isModifiable(any(), any(), any())).thenReturn(emptyList());
    when(editorPreconditions.isEditable(any(), any())).thenReturn(true);

    enricher.enrich(context, appender);

    ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
    verify(appender, times(2)).appendLink(argument.capture(), eq("/scm/v2/external-file/hitchhiker/HeartOfGold/nested%2Ffile%2Ftestfile.URL"));
    List<String> argumentValues = argument.getAllValues();
    Assertions.assertThat(argumentValues).contains("externalFile", "modifyExternalFile");
  }

  private void setUpHalContext(Repository repository, BrowserResult result, FileObject fileObject) {
    doReturn(repository.getNamespaceAndName()).when(context).oneRequireByType(NamespaceAndName.class);
    doReturn(result).when(context).oneRequireByType(BrowserResult.class);
    doReturn(fileObject).when(context).oneRequireByType(FileObject.class);
  }

  private BrowserResult createBrowserResult(String revision, String branchName, FileObject fileObject) {
    return new BrowserResult(revision, branchName, fileObject);
  }
}
