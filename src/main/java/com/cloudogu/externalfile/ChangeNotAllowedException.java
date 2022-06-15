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

import com.cloudogu.scm.editor.ChangeObstacle;
import sonia.scm.ContextEntry;
import sonia.scm.ExceptionWithContext;
import sonia.scm.repository.NamespaceAndName;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class ChangeNotAllowedException extends ExceptionWithContext {

  private final Collection<ChangeObstacle> obstacles;

  public ChangeNotAllowedException(NamespaceAndName namespaceAndName, String branch, String path, Collection<ChangeObstacle> obstacles) {
    super(createContext(namespaceAndName, branch, path), buildMessage(obstacles));
    this.obstacles = obstacles;
  }

  private static List<ContextEntry> createContext(NamespaceAndName namespaceAndName, String branch, String path) {
    ContextEntry.ContextBuilder contextBuilder = new ContextEntry.ContextBuilder();
    if (branch != null) {
      contextBuilder
        .in("Branch", branch);
    }
    return contextBuilder
      .in(namespaceAndName).build();
  }

  Collection<ChangeObstacle> getObstacles() {
    return obstacles;
  }

  private static String buildMessage(Collection<ChangeObstacle> obstacles) {
    return obstacles.stream().map(ChangeObstacle::getMessage).collect(Collectors.joining(",\n", "Change was prevented by other plugins:\n", ""));
  }

  @Override
  public String getCode() {
    return "AuRneG3vO1";
  }
}
