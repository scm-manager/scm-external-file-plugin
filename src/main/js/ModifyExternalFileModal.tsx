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
  const { modify, error: modifyError, isLoading: isModifying } = useModifyExternalFile(repository, file);
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
