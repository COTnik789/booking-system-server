import { format, parseISO } from "date-fns";
import { ru } from "date-fns/locale";

export function formatDateTime(isoString?: string | null): string {
    if (!isoString) return "";
    try {
        const date = parseISO(isoString);
        return format(date, "dd.MM.yyyy HH:mm", { locale: ru });
    } catch {
        return isoString;
    }
}

