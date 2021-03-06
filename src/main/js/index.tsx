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

import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import ExternalFileRenderer from "./ExternalFileRenderer";
import CreateExternalFileButton from "./CreateExternalFileButton";
import ModifyExternalFileModal from "./ModifyExternalFileModal";

binder.bind<extensionPoints.RepositorySourcesView>("repos.sources.view", ExternalFileRenderer, {
  predicate: props => props.file._links.externalFile
});

binder.bind<extensionPoints.ReposSourcesActionbar>("repos.sources.actionbar", CreateExternalFileButton, {
  predicate: props => props.sources && "createExternalFile" in props.sources._links,
  priority: 100
});

binder.bind<extensionPoints.FileViewActionBarOverflowMenu>(
  "repos.sources.content.actionbar.menu",
  {
    icon: "edit",
    label: "scm-external-file-plugin.edit.buttonLabel",
    category: "Editor",
    modalElement: ModifyExternalFileModal
  },
  {
    predicate: props => props.file && "modifyExternalFile" in props.file._links
  }
);
