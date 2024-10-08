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
import com.cloudogu.scm.editor.ChangeObstacle;
import com.google.common.base.Strings;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.ModifyCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;

class ExternalFileService {

  private final RepositoryServiceFactory serviceFactory;

  private final ChangeGuardCheck changeGuardCheck;
  private static final String FILE_TEMPLATE = "[InternetShortcut]\nURL=%s";

  @Inject
  public ExternalFileService(RepositoryServiceFactory serviceFactory, ChangeGuardCheck changeGuardCheck) {
    this.serviceFactory = serviceFactory;
    this.changeGuardCheck = changeGuardCheck;
  }

  public void create(Repository repository, CreateExternalFileDto dto) throws IOException {
    RepositoryPermissions.modify(repository).check();
    try (RepositoryService service = serviceFactory.create(repository)) {
      String filePath = resolveFilePath(dto.getPath());
      ModifyCommandBuilder modifyCommandBuilder = service.getModifyCommand()
        .createFile(filePath)
        .withData(new ByteArrayInputStream((String.format(FILE_TEMPLATE, dto.getUrl())).getBytes(UTF_8)))
        .setCommitMessage(dto.getCommitMessage());
      if (!Strings.isNullOrEmpty(dto.getBranch())) {
        modifyCommandBuilder.setBranch(dto.getBranch());
      }
        modifyCommandBuilder.execute();
    }
  }

  public void modify(Repository repository, @Nullable String branch, String path, String url, String commitMessage) throws IOException {
    RepositoryPermissions.modify(repository).check();

    Collection<ChangeObstacle> obstacles = changeGuardCheck.isModifiable(repository.getNamespaceAndName(), branch, path);
    if (!obstacles.isEmpty()) {
      throw new ChangeNotAllowedException(repository.getNamespaceAndName(), branch, obstacles);
    }

    try (RepositoryService service = serviceFactory.create(repository)) {
      ModifyCommandBuilder modifyCommandBuilder = service.getModifyCommand()
        .modifyFile(resolveFilePath(path))
        .withData(new ByteArrayInputStream((String.format(FILE_TEMPLATE, url)).getBytes(UTF_8)))
        .setCommitMessage(commitMessage);
      if (!Strings.isNullOrEmpty(branch)) {
        modifyCommandBuilder.setBranch(branch);
      }
      modifyCommandBuilder.execute();
    }
  }

  public String get(Repository repository, String path) throws IOException {
    RepositoryPermissions.read(repository).check();
    try (RepositoryService service = serviceFactory.create(repository)) {
      String content = service.getCatCommand().getContent(path);
      String url = Arrays.stream(content.split("\n"))
        .filter(line -> line.startsWith("URL="))
        .findFirst()
        .orElseThrow(() -> new MissingUrlException(ContextEntry.ContextBuilder.entity(repository).build(), "Could not find target in url file"));
      return url.substring("URL=".length());
    }
  }

  private String resolveFilePath(String path) {
    String filePath = "";
    if (!Strings.isNullOrEmpty(path)) {
      filePath = path;
    }
    if (!filePath.endsWith(".URL")) {
      if (filePath.toLowerCase(Locale.ROOT).endsWith(".url")) {
        filePath = filePath.substring(0, filePath.length() - ".url".length());
      }
      filePath += ".URL";
    }

    return filePath;
  }
}
