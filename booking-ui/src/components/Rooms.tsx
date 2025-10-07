import { useEffect, useState } from "react";
import { getRooms } from "../api";
import type { RoomDto } from "../types";
import { parseServerError } from "../utils/errors";
import { useAlert } from "../Alert";

type Props = {
    selectedRoomId: number | null;
    onSelect: (id: number | null) => void;
};

export default function Rooms({ selectedRoomId, onSelect }: Props) {
    const [rooms, setRooms] = useState<RoomDto[]>([]);
    const [error, setError] = useState<string>("");
    const { show } = useAlert();

    useEffect(() => {
        (async () => {
            try {
                const data = await getRooms();
                setRooms(data);
            } catch (err: any) {
                const msg = parseServerError(err);
                setError(msg);
                show("error", msg);
            }
        })();
    }, []);

    return (
        <div>
            {error && <div className="error">{error}</div>}
            {!error && rooms.length === 0 && <div className="muted">Комнат нет</div>}
            <ul className="list">
                {rooms.map((r) => (
                    <li
                        key={r.id}
                        className={`list-item ${selectedRoomId === r.id ? "active" : ""}`}
                        onClick={() => onSelect(r.id)}
                    >
                        <div className="title">{r.name}</div>
                        <div className="subtitle">
                            {r.openTime?.slice(0, 5)} – {r.closeTime?.slice(0, 5)}
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    );
}
