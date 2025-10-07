import { useState } from "react";
import { createRoom } from "../api";
import { useAlert } from "../Alert";
import { parseServerError } from "../utils/errors";

type Props = {
    onCreated: () => void;
};

export default function NewRoomForm({ onCreated }: Props) {
    const { show } = useAlert();
    const [name, setName] = useState("");
    const [openTime, setOpenTime] = useState("08:00");
    const [closeTime, setCloseTime] = useState("20:00");
    const [error, setError] = useState("");

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError("");
        try {
            await createRoom({
                name: name.trim(),
                openTime: openTime.length === 5 ? `${openTime}:00` : openTime,
                closeTime: closeTime.length === 5 ? `${closeTime}:00` : closeTime,
            });
            setName("");
            setOpenTime("08:00");
            setCloseTime("20:00");
            onCreated();
            show("success", "Помещение создано");
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
                <label className="field-label" htmlFor="room-name">Название</label>
                <input
                    id="room-name"
                    className="input"
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="Переговорка №1"
                />
            </div>
            <div className="field">
                <label className="field-label" htmlFor="room-open">Открывается</label>
                <input
                    id="room-open"
                    className="input"
                    type="time"
                    value={openTime}
                    onChange={(e) => setOpenTime(e.target.value)}
                    step={60}
                />
            </div>
            <div className="field">
                <label className="field-label" htmlFor="room-close">Закрывается</label>
                <input
                    id="room-close"
                    className="input"
                    type="time"
                    value={closeTime}
                    onChange={(e) => setCloseTime(e.target.value)}
                    step={60}
                />
            </div>
            <div className="form-actions">
                <button
                    className="btn"
                    type="button"
                    onClick={() => {
                        setName("");
                        setOpenTime("08:00");
                        setCloseTime("20:00");
                        setError("");
                    }}
                >
                    Сбросить
                </button>
                <button className="btn primary" type="submit" disabled={!name.trim()}>
                    Создать
                </button>
            </div>
        </form>
    );
}
