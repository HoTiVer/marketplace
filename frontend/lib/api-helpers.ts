/* eslint-disable @typescript-eslint/no-explicit-any */

import { UseFormSetError, FieldValues, Path } from "react-hook-form";

export const handleValidationErrors = <T extends FieldValues>(
    error: any,
    setError: UseFormSetError<T>
): boolean => {
    if (error.response?.status === 422 && error.response?.data?.errors) {
        const backendErrors = error.response.data.errors;

        Object.entries(backendErrors).forEach(([field, messages]) => {
            const message = Array.isArray(messages) ? messages[0] : messages;

            setError(field as Path<T>, {
                type: "server",
                message: message as string,
            });
        });
        return true;
    }

    return false;
};