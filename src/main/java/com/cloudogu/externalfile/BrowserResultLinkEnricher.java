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
import sonia.scm.repository.NamespaceAndName;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

@Extension
@Enrich(BrowserResult.class)
public class BrowserResultLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;
  private final EditorPreconditions preconditions;
  private final ChangeGuardCheck changeGuardCheck;

  @Inject
  public BrowserResultLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStore, EditorPreconditions preconditions, ChangeGuardCheck changeGuardCheck) {
    this.scmPathInfoStore = scmPathInfoStore;
    this.preconditions = preconditions;
    this.changeGuardCheck = changeGuardCheck;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    NamespaceAndName namespaceAndName = context.oneRequireByType(NamespaceAndName.class);
    BrowserResult browserResult = context.oneRequireByType(BrowserResult.class);
    if (isEnrichable(namespaceAndName, browserResult)) {
      appender.appendLink("createExternalFile", createUploadLink(namespaceAndName));
    }
  }

  private boolean isEnrichable(NamespaceAndName namespaceAndName, BrowserResult browserResult) {
    return preconditions.isEditable(namespaceAndName, browserResult)
      && isDirectory(browserResult)
      && changeGuardCheck.canCreateFilesIn(namespaceAndName, browserResult.getRequestedRevision(), browserResult.getFile().getPath()).isEmpty();
  }

  private boolean isDirectory(BrowserResult browserResult) {
    return browserResult.getFile().isDirectory();
  }

  private String createUploadLink(NamespaceAndName namespaceAndName) {
    return new RestAPI(scmPathInfoStore.get().get().getApiRestUri())
      .externalFile()
      .createExternalFile(namespaceAndName.getNamespace(), namespaceAndName.getName())
      .asString();
  }
}
