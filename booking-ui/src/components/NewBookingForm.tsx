import { useEffect, useMemo, useState } from "react";
import { createBooking, getRooms } from "../api";
import type { RoomDto } from "../types";
import DateTimePicker from "./DateTimePicker";
import { useAlert } from "../Alert";
import { parseServerError } from "../utils/errors";

type Props = {
    selectedRoomId: number | null;
    onCreated: () => void;
};

function toDateInputValue(d: Date) {
    const pad = (n: number) => n.toString().padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(
        d.getHours()
    )}:${pad(d.getMinutes())}`;
}

export default function NewBookingForm({ selectedRoomId, onCreated }: Props) {
    const { show } = useAlert();
    const [rooms, setRooms] = useState<RoomDto[]>([]);
    const [roomId, setRoomId] = useState<number | "">("");
    const [start, setStart] = useState<string>("");
    const [end, setEnd] = useState<string>("");
    const [error, setError] = useState<string>("");

    useEffect(() => {
        (async () => {
            try {
                const list = await getRooms();
                setRooms(list);
            } catch {}
        })();
    }, []);

    useEffect(() => {
        if (selectedRoomId != null) setRoomId(selectedRoomId);
    }, [selectedRoomId]);

    useEffect(() => {
        const now = new Date();
        now.setMinutes(0, 0, 0);
        now.setHours(now.getHours() + 1);
        const oneHourLater = new Date(now.getTime() + 60 * 60 * 1000);
        setStart(toDateInputValue(now));
        setEnd(toDateInputValue(oneHourLater));
    }, []);

    const canSubmit = useMemo(
        () => roomId !== "" && !!start && !!end,
        [roomId, start, end]
    );

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError("");
        const user = localStorage.getItem("xUser")?.trim();

        if (!user) {
            const msg = "Пожалуйста, укажите имя пользователя (вверху экрана)";
            setError(msg);
            show("error", msg);
            return;
        }

        try {
            await createBooking({
                roomId: Number(roomId),
                startTime: start.length === 16 ? `${start}:00` : start,
                endTime: end.length === 16 ? `${end}:00` : end,
            });
            show("success", "Бронирование успешно создано");
            onCreated();
        } catch (err: any) {
            const msg = parseServerError(err);
            setError(msg);
            show("error", msg);
        }
    }

    return (
        <form className="form" onSubmit={onSubmit}>
            {error && <div className="error">{error}</div>}

            <div className="field">
                <label className="field-label" htmlFor="roomId">Помещение</label>
                <select
                    id="roomId"
                    className="select"
                    value={roomId}
                    onChange={(e) =>
                        setRoomId(e.target.value === "" ? "" : Number(e.target.value))
                    }
                >
                    <option value="" disabled>Выберите помещение</option>
                    {rooms.map((r) => (
                        <option key={r.id} value={r.id}>
                            {r.name} ({r.openTime}–{r.closeTime})
                        </option>
                    ))}
                </select>
            </div>

            <DateTimePicker label="Начало" value={start} onChange={setStart} />
            <DateTimePicker label="Окончание" value={end} onChange={setEnd} />

            {!localStorage.getItem("xUser")?.trim() && (
                <div className="warning">Введите имя пользователя выше, чтобы бронировать комнаты</div>
            )}
            <div className="form-actions">
                <button
                    className="btn"
                    type="button"
                    onClick={() => {
                        setRoomId(selectedRoomId ?? "");
                        setError("");
                    }}
                >
                    Сбросить
                </button>
                <button className="btn primary" type="submit" disabled={!canSubmit}>
                    Забронировать
                </button>
            </div>
        </form>
    );
}
