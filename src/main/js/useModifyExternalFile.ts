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

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@scm-manager/ui-components";
import { File, Link } from "@scm-manager/ui-types";

export type ModifyExternalFileRequest = {
  commitMessage: string;
  url: string;
  branch: string;
};

export const useModifyExternalFile = (file: File) => {
  const queryClient = useQueryClient();
  const link = (file._links.modifyExternalFile as Link).href;

  const { mutateAsync, isPending, error } = useMutation<unknown, Error, ModifyExternalFileRequest>({
    mutationFn: (request) => apiClient.put(link, request, "application/vnd.scmm-externalFile+json;v=2"),
    onSuccess: () => {
      return queryClient.invalidateQueries({ queryKey: ["externalFileUrl", link] });
    },
  });

  return {
    modify: (request: ModifyExternalFileRequest) => mutateAsync(request),
    isLoading: isPending,
    error,
  };
};
