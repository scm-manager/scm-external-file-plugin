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
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

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
