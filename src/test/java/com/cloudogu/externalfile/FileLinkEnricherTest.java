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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

  @InjectMocks
  private FileLinkEnricher enricher;

  @Test
  void shouldNotEnrichLinkForDirectory() {
    FileObject fileObject = new FileObject();
    fileObject.setDirectory(true);
    fileObject.setName("testfile.URL");
    BrowserResult result = createBrowserResult("42", "master", fileObject);
    setUpHalContext(repository, result);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(any(), any());
  }

  @Test
  void shouldNotEnrichLinkForWrongFileExtension() {
    FileObject fileObject = new FileObject();
    fileObject.setDirectory(false);
    fileObject.setPath("testfile.LUR");
    BrowserResult result = createBrowserResult("42", "master", fileObject);
    setUpHalContext(repository, result);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(any(), any());
  }

  @Test
  void shouldEnrichLink() {
    ScmPathInfoStore scmPathInfoStore = mock(ScmPathInfoStore.class);
    when(scmPathInfoStoreProvider.get()).thenReturn(scmPathInfoStore);
    when(scmPathInfoStore.get()).thenReturn(() -> URI.create("/scm"));

    FileObject fileObject = new FileObject();
    fileObject.setDirectory(false);
    fileObject.setPath("nested/file/testfile.URL");
    BrowserResult result = createBrowserResult("42", "master", fileObject);
    setUpHalContext(repository, result);

    enricher.enrich(context, appender);

    verify(appender).appendLink("externalFile", "/scm/v2/external-file/hitchhiker/HeartOfGold/nested%2Ffile%2Ftestfile.URL");
  }

  private void setUpHalContext(Repository repository, BrowserResult result) {
    doReturn(repository.getNamespaceAndName()).when(context).oneRequireByType(NamespaceAndName.class);
    doReturn(result).when(context).oneRequireByType(BrowserResult.class);
  }

  private BrowserResult createBrowserResult(String revision, String branchName, FileObject fileObject) {
    return new BrowserResult(revision, branchName, fileObject);
  }
}
