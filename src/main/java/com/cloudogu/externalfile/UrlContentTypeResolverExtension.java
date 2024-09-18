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

import sonia.scm.io.ContentTypeResolverExtension;
import sonia.scm.plugin.Extension;

import java.util.Optional;

@Extension
public class UrlContentTypeResolverExtension implements ContentTypeResolverExtension {

  private static final String CUSTOM_CONTENT_TYPE = "scm/external-file-url";

  @Override
  public Optional<String> resolve(String path, byte[] contentPrefix) {
    if (path.endsWith(".URL")) {
      return Optional.of(CUSTOM_CONTENT_TYPE);
    }
    return Optional.empty();
  }
}
