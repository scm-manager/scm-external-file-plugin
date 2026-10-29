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

import React, { FC, useCallback, useMemo, useRef } from "react";
import { useTranslation } from "react-i18next";
import { useForm, useWatch } from "react-hook-form";
import { ButtonGroup, CommitAuthor, InputField, Modal, validation } from "@scm-manager/ui-components";
import { Button, ErrorNotification, Textarea } from "@scm-manager/ui-core";
import { File, Repository } from "@scm-manager/ui-types";

export type ExternalFileForm = {
  commitMessage: string;
  url: string;
  path: string;
  filename: string;
};

export type FormSubmitValue = {
  commitMessage: string;
  url: string;
  path: string;
};

type Props = {
  repository: Repository;
  revision?: string;
  close: () => void;
  file?: File;
  initialUrl?: string;
  initialPath?: string;
  initialFilename?: string;
  onSubmit: (form: FormSubmitValue) => void;
  isLoading?: boolean;
  error?: Error | null;
  title: string;
  submitButtonLabel: string;
};

const ExternalFileModal: FC<Props> = ({
  close,
  initialFilename,
  isLoading,
  error,
  initialUrl,
  onSubmit,
  initialPath,
  title,
  submitButtonLabel,
}) => {
  const originalPath = initialPath === "/" ? "/" : "/" + initialPath;
  const [t] = useTranslation("plugins");
  const { register, formState, control } = useForm<ExternalFileForm>({
    defaultValues: { path: originalPath, filename: initialFilename, commitMessage: "", url: initialUrl },
    mode: "onChange",
  });
  const [commitMessage, filename, url, path] = useWatch({
    control,
    name: ["commitMessage", "filename", "url", "path"],
  });
  const isPathAndFilenameDisabled = useMemo(() => isLoading || !!initialUrl, [isLoading, initialUrl]);
  const submitDisabled = useMemo(
    () => !commitMessage || !filename || !url || !!formState.errors.url || !!formState.errors.path,
    [commitMessage, filename, url, formState.errors.url, formState.errors.path],
  );
  const initialFocusRef = useRef<HTMLInputElement | null>(null);

  const submit = useCallback(() => {
    if (submitDisabled) {
      return;
    }

    let resultingPath = path;
    if (resultingPath.length !== 0 && !resultingPath.endsWith("/")) {
      resultingPath = resultingPath + "/";
    }
    resultingPath = resultingPath + filename;
    onSubmit({
      commitMessage,
      url,
      path: resultingPath,
    });
  }, [submitDisabled, path, filename, onSubmit, commitMessage, url]);

  const { ref: pathRef, ...pathRegistration } = register("path", {
    validate: validation.isPathValid,
  });

  const { ref: urlRef, ...urlRegistration } = register("url", { validate: validation.isUrlValid });

  const externalFilenameRegex = /\.url$/i;
  const isExternalFilenameValid = (filename: string) => {
    return validation.isFilenameValid(filename) && !externalFilenameRegex.test(filename);
  };

  const body = (
    <>
      {error ? <ErrorNotification error={error} /> : null}
      <InputField
        label={t("scm-external-file-plugin.create.path")}
        readOnly={isPathAndFilenameDisabled}
        validationError={!!formState.errors.path}
        errorMessage={t("scm-external-file-plugin.create.invalidPath")}
        helpText={t("scm-external-file-plugin.create.pathHelpText")}
        onReturnPressed={submit}
        className="mb-4"
        ref={(e) => {
          pathRef(e);
          if (!initialUrl) {
            initialFocusRef.current = e;
          }
        }}
        {...pathRegistration}
      />
      <InputField
        label={t("scm-external-file-plugin.create.filename")}
        validationError={!!formState.errors.filename}
        errorMessage={t("scm-external-file-plugin.create.invalidFilename")}
        helpText={t("scm-external-file-plugin.create.filenameHelpText")}
        readOnly={isPathAndFilenameDisabled}
        onReturnPressed={submit}
        {...register("filename", { validate: isExternalFilenameValid })}
      />
      <hr />
      <InputField
        label={t("scm-external-file-plugin.create.url")}
        value={url}
        validationError={!!formState.errors.url}
        errorMessage={t("scm-external-file-plugin.create.invalidUrl")}
        helpText={t("scm-external-file-plugin.create.urlHelpText")}
        disabled={isLoading}
        onReturnPressed={submit}
        ref={(e) => {
          urlRef(e);
          if (initialUrl) {
            initialFocusRef.current = e;
          }
        }}
        {...urlRegistration}
      />
      <div className="mb-2 mt-5">
        <CommitAuthor />
      </div>
      <Textarea
        placeholder={t("scm-external-file-plugin.create.commitPlaceholder")}
        value={commitMessage}
        disabled={isLoading}
        onSubmit={submit}
        {...register("commitMessage")}
      />
    </>
  );

  const footer = (
    <ButtonGroup>
      <Button onClick={close} disabled={isLoading}>
        {t("scm-external-file-plugin.modal.cancel")}
      </Button>
      <Button
        onClick={submit}
        disabled={submitDisabled || !!formState.errors.filename}
        isLoading={isLoading}
        variant="primary"
      >
        {submitButtonLabel}
      </Button>
    </ButtonGroup>
  );

  return (
    <Modal
      body={body}
      footer={footer}
      title={title}
      closeFunction={close}
      active={true}
      initialFocusRef={initialFocusRef}
    />
  );
};

export default ExternalFileModal;
