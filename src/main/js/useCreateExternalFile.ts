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

import { useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@scm-manager/ui-components";
import { File, Link, Repository } from "@scm-manager/ui-types";

export type CreateExternalFileRequest = {
  commitMessage: string;
  url: string;
  branch: string;
  path: string;
};

export const useCreateExternalFile = (repository: Repository, sources: File) => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const { mutate, isPending, error } = useMutation<unknown, Error, CreateExternalFileRequest>({
    mutationFn: (request) =>
      apiClient
        .post((sources._links.createExternalFile as Link).href, request, "application/vnd.scmm-externalFile+json;v=2")
        .then(() => {
          let path = request.path;
          if (path.startsWith("/")) {
            path = path.substring(1);
          }
          if (!path.endsWith(".URL")) {
            path += ".URL";
          }
          navigate(`/repo/${repository.namespace}/${repository.name}/code/sources/${request.branch}/${path}`);
        }),
    onSuccess: () => {
      return queryClient.invalidateQueries({
        queryKey: ["repository", repository.namespace, repository.name, "sources"],
      });
    },
  });

  return {
    create: (request: CreateExternalFileRequest) => mutate(request),
    isLoading: isPending,
    error,
  };
};
