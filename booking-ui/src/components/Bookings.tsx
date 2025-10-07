import { useEffect, useState } from "react";
import { deleteBooking, getBookings } from "../api";
import type { BookingDto } from "../types";
import { useAlert } from "../Alert";
import { formatDateTime } from "../utils/formatDate";
import { parseServerError } from "../utils/errors";

type Props = {
    roomId: number | null;
    onDeleted: () => void;
};

export default function Bookings({ roomId, onDeleted }: Props) {
    const [items, setItems] = useState<BookingDto[]>([]);
    const [error, setError] = useState<string>("");
    const { show } = useAlert();

    useEffect(() => {
        setError("");
        if (!roomId) {
            setItems([]);
            return;
        }
        (async () => {
            try {
                const data = await getBookings(roomId);
                setItems(data);
            } catch (err: any) {
                const msg = parseServerError(err);
                setError(msg);
                show("error", msg);
            }
        })();
    }, [roomId]);

    async function remove(id: number) {
        setError("");
        try {
            await deleteBooking(id);
            setItems((prev) => prev.filter((b) => b.id !== id));
            onDeleted();
            show("success", "Бронь удалена");
        } catch (err: any) {
            const msg = parseServerError(err);
            setError(msg);
            show("error", msg);
        }
    }

    if (!roomId) return <div className="muted">Выберите помещение</div>;

    return (
        <div>
            {error && <div className="error">{error}</div>}
            {items.length === 0 && <div className="muted">Нет броней</div>}
            <ul className="list">
                {items.map((b) => (
                    <li key={b.id} className="list-item">
                        <div className="title">
                            {formatDateTime(b.startTime)} — {formatDateTime(b.endTime)}
                        </div>
                        <div className="subtitle">Пользователь: {b.userName ?? "—"}</div>
                        <button className="btn danger small" onClick={() => remove(b.id)}>
                            Удалить
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    );
}
