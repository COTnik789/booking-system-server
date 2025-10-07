import { useEffect, useState } from "react";
import { getAvailability } from "../api";
import type { AvailabilityDto } from "../types";
import { format } from "date-fns";
import { formatDateTime } from "../utils/formatDate";
import { parseServerError } from "../utils/errors";

type Props = { roomId: number | null };

export default function Availability({ roomId }: Props) {
    const [from, setFrom] = useState<string>(() => format(new Date(), "yyyy-MM-dd"));
    const [to, setTo] = useState<string>(() => format(new Date(), "yyyy-MM-dd"));
    const [items, setItems] = useState<AvailabilityDto[]>([]);
    const [error, setError] = useState<string>("");

    async function load() {
        if (!roomId) {
            setItems([]);
            return;
        }
        setError("");
        try {
            const data = await getAvailability(roomId, from, to);
            setItems(data);
        } catch (err: any) {
            const msg = parseServerError(err);
            setError(msg);
        }
    }

    useEffect(() => {
        load();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [roomId]);

    return (
        <div className="availability">
            <div className="filters">
                <div className="field inline">
                    <label>С</label>
                    <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
                </div>
                <div className="field inline">
                    <label>По</label>
                    <input type="date" value={to} onChange={(e) => setTo(e.target.value)} />
                </div>
                <button className="btn" onClick={load} disabled={!roomId}>
                    Обновить
                </button>
            </div>

            {error && <div className="error">{error}</div>}
            {!error && items.length === 0 && <div className="muted">Нет интервалов</div>}

            <ul className="intervals">
                {items.map((i, idx) => (
                    <li key={idx} className="interval">
                        {i.fullDayFree
                            ? `${formatDateTime(i.start).slice(0, 10)} свободно весь день`
                            : `${formatDateTime(i.start)} — ${formatDateTime(i.end ?? undefined)}`}
                    </li>
                ))}
            </ul>
        </div>
    );
}
