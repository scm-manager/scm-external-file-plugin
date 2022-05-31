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
import com.cloudogu.scm.editor.EditorPreconditions;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import javax.inject.Provider;

import java.net.URI;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrowserResultLinkEnricherTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();
  @Mock
  private Provider<ScmPathInfoStore> scmPathInfoStore;
  @Mock
  private EditorPreconditions preconditions;
  @Mock
  private ChangeGuardCheck changeGuardCheck;
  @Mock
  private HalAppender appender;
  @Mock
  private HalEnricherContext context;
  @InjectMocks
  private BrowserResultLinkEnricher enricher;

  @BeforeEach
  void initEnricher() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/scm"));
    lenient().when(scmPathInfoStore.get()).thenReturn(pathInfoStore);
  }

  @Test
  void shouldNotEnrichLinkIfNotEditable() {
    BrowserResult browserResult = createBrowserResult(new FileObject());
    when(preconditions.isEditable(repository.getNamespaceAndName(), browserResult)).thenReturn(false);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(any(), any());
  }

  @Test
  void shouldNotEnrichLinkIfBlockedByChangeGuard() {
    FileObject fileObject = new FileObject();
    fileObject.setDirectory(true);
    BrowserResult browserResult = createBrowserResult(fileObject);
    when(preconditions.isEditable(repository.getNamespaceAndName(), browserResult)).thenReturn(true);
    when(changeGuardCheck.canCreateFilesIn(eq(repository.getNamespaceAndName()), any(), any())).thenReturn(ImmutableSet.of(new ChangeObstacle() {
      @Override
      public String getMessage() {
        return null;
      }

      @Override
      public String getKey() {
        return null;
      }
    }));

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(any(), any());
  }

  @Test
  void shouldNotEnrichLinkIfNotDirectory() {
    FileObject fileObject = new FileObject();
    fileObject.setDirectory(false);
    BrowserResult browserResult = createBrowserResult(fileObject);
    when(preconditions.isEditable(repository.getNamespaceAndName(), browserResult)).thenReturn(true);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(any(), any());
  }

  @Test
  void shouldEnrichLink() {
    FileObject fileObject = new FileObject();
    fileObject.setDirectory(true);
    BrowserResult browserResult = createBrowserResult(fileObject);
    when(preconditions.isEditable(repository.getNamespaceAndName(), browserResult)).thenReturn(true);
    when(changeGuardCheck.canCreateFilesIn(eq(repository.getNamespaceAndName()), any(), any())).thenReturn(Collections.emptySet());
    enricher.enrich(context, appender);

    verify(appender).appendLink("createExternalFile", "/scm/v2/external-file/hitchhiker/42Puzzle");
  }

  private BrowserResult createBrowserResult(FileObject fileObject) {
    BrowserResult browserResult = mock(BrowserResult.class);
    when(context.oneRequireByType(BrowserResult.class)).thenReturn(browserResult);
    when(context.oneRequireByType(NamespaceAndName.class)).thenReturn(repository.getNamespaceAndName());
    lenient().when(browserResult.getFile()).thenReturn(fileObject);
    return browserResult;
  }

}
