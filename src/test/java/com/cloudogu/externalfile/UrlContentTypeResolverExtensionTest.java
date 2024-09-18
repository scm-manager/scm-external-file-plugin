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

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UrlContentTypeResolverExtensionTest {

  UrlContentTypeResolverExtension extension = new UrlContentTypeResolverExtension();

  @Test
  void shouldReturnEmptyOptional() {
    Optional<String> contentType = extension.resolve("README.md", new byte[]{});

    assertThat(contentType).isNotPresent();
  }

  @Test
  void shouldReturnCustomContentType() {
    Optional<String> contentType = extension.resolve("README.URL", new byte[]{});

    assertThat(contentType)
      .isPresent()
      .contains("scm/external-file-url");
  }
}
