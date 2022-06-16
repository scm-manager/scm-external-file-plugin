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
