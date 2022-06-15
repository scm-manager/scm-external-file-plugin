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

import { File, Repository } from "@scm-manager/ui-types";
import React, { FC, useCallback, useMemo, useRef } from "react";
import { useTranslation } from "react-i18next";
import {
  Button,
  ButtonGroup,
  CommitAuthor,
  ErrorNotification,
  InputField,
  Modal,
  Textarea,
  validation
} from "@scm-manager/ui-components";
import { useForm, useWatch } from "react-hook-form";

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
  submitButtonLabel
}) => {
  const originalPath = initialPath === "/" ? "/" : "/" + initialPath;
  const [t] = useTranslation("plugins");
  const { register, formState, getValues, control } = useForm<ExternalFileForm>({
    defaultValues: { path: originalPath, filename: initialFilename, commitMessage: "", url: initialUrl },
    mode: "onBlur"
  });
  const [commitMessage, filename, url] = useWatch({ control, name: ["commitMessage", "filename", "url"] });
  const isPathAndFilenameDisabled = useMemo(() => isLoading || !!initialUrl, [isLoading, initialUrl]);
  const submitDisabled = useMemo(() => !commitMessage || !filename || !url, [commitMessage, filename, url]);
  const initialFocusRef = useRef<HTMLInputElement | null>(null);

  const submit = useCallback(() => {
    const { commitMessage, path, url, filename } = getValues();
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
      path: resultingPath
    });
  }, [getValues, submitDisabled, onSubmit]);

  const { ref: pathRef, ...pathRegistration } = register("path", {
    validate: validation.isPathValid
  });

  const { ref: urlRef, ...urlRegistration } = register("url", { validate: validation.isUrlValid });

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
        ref={e => {
          pathRef(e);
          if (!initialUrl) {
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
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
        {...register("filename", { validate: validation.isFilenameValid })}
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
        ref={e => {
          urlRef(e);
          if (initialUrl) {
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
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
      <Button action={close} disabled={isLoading}>
        {t("scm-external-file-plugin.modal.cancel")}
      </Button>
      <Button action={submit} disabled={submitDisabled} loading={isLoading} color="primary">
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
