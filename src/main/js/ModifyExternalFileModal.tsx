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

import React from "react";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { useExternalFileUrl } from "./useExternalFile";
import ExternalFileModal from "./ExternalFileModal";
import { useModifyExternalFile } from "./useModifyExternalFile";
import { useTranslation } from "react-i18next";

const ModifyExternalFileModal: extensionPoints.ModalMenuProps["modalElement"] = ({
  file,
  revision,
  repository,
  close
}) => {
  const [t] = useTranslation("plugins");
  const { data: externalFileUrl, isLoading, error } = useExternalFileUrl(file);
  const { modify, error: modifyError, isLoading: isModifying } = useModifyExternalFile(file);
  return (
    <ExternalFileModal
      submitButtonLabel={t("scm-external-file-plugin.edit.submit")}
      title={t("scm-external-file-plugin.edit.title")}
      repository={repository}
      revision={revision}
      close={close}
      initialUrl={externalFileUrl}
      initialPath={file.path.split(file.name)[0]}
      initialFilename={file.name}
      error={error || modifyError}
      isLoading={isLoading || isModifying}
      onSubmit={form =>
        modify({ url: form.url, commitMessage: form.commitMessage, branch: revision || "" }).then(close)
      }
    />
  );
};

export default ModifyExternalFileModal;
