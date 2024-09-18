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
