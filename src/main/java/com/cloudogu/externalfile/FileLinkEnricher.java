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
import com.cloudogu.scm.editor.EditorPreconditions;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

import javax.inject.Inject;
import javax.inject.Provider;

@Extension
@Enrich(FileObject.class)
public class FileLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;
  private final ChangeGuardCheck changeGuardCheck;

  private final EditorPreconditions editorPreconditions;

  @Inject
  public FileLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStore, ChangeGuardCheck changeGuardCheck, EditorPreconditions editorPreconditions) {
    this.scmPathInfoStore = scmPathInfoStore;
    this.changeGuardCheck = changeGuardCheck;
    this.editorPreconditions = editorPreconditions;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    NamespaceAndName namespaceAndName = context.oneRequireByType(NamespaceAndName.class);
    BrowserResult browserResult = context.oneRequireByType(BrowserResult.class);
    FileObject fileObject = context.oneRequireByType(FileObject.class);

    if (shouldEnrichLink(browserResult)) {
      RestAPI.ExternalFileLinks externalFileLinks = new RestAPI(scmPathInfoStore.get().get().getApiRestUri()).externalFile();
      String link = externalFileLinks.getExternalFile(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getFile().getPath()).asString();
      appender.appendLink("externalFile", link);
      if (
        editorPreconditions.isEditable(namespaceAndName, browserResult) &&
          changeGuardCheck.isModifiable(namespaceAndName, browserResult.getRequestedRevision(), fileObject.getPath()).isEmpty()
      ) {
        appender.appendLink("modifyExternalFile", link);
      }
    }
  }

  private boolean shouldEnrichLink(BrowserResult browserResult) {
    return !browserResult.getFile().isDirectory() && browserResult.getFile().getPath().endsWith(".URL");
  }
}
