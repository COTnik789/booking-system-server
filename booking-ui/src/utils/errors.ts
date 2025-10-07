export function parseServerError(err: any): string {
    if (!err) return "Неизвестная ошибка";

    if (err.response?.data?.message) {
        return err.response.data.message;
    }

    if (typeof err.response?.data === "object") {
        const data = err.response.data;
        const parts: string[] = [];
        for (const key in data) {
            if (typeof data[key] === "string") {
                parts.push(`${key}: ${data[key]}`);
            }
        }
        if (parts.length > 0) return parts.join("; ");
    }

    if (typeof err.response?.data === "string") {
        return err.response.data;
    }

    return err.message || "Произошла ошибка при выполнении запроса";
}
