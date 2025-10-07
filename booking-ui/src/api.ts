import axios from "axios";

// создаём клиент с базовым URL из .env
const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
});

// добавляем X-User в каждый запрос
api.interceptors.request.use((config) => {
    if (typeof window !== "undefined") {
        const user = localStorage.getItem("xUser")?.trim();
        if (user) {
            // кодируем, чтобы избежать non-ASCII ошибок
            const safeUser = encodeURIComponent(user);
            config.headers["X-User"] = safeUser;
        }
    }
    return config;
});

const toLocalISOString = (value: string) => {
    const d = new Date(value);
    // убираем миллисекунды и применяем локальную зону
    return new Date(d.getTime() - d.getTimezoneOffset() * 60000)
        .toISOString()
        .slice(0, 19);
};


export const getRooms = async () => (await api.get("/api/rooms")).data;

export const createRoom = async (room: {
    name: string;
    openTime: string;
    closeTime: string;
}) => (await api.post("/api/rooms", room)).data;

// бронирования
export const getBookings = async (roomId?: number) =>
    (await api.get(`/api/bookings${roomId ? `/room/${roomId}` : ""}`)).data;

export const createBooking = async (booking: {
    roomId: number;
    startTime: string;
    endTime: string;
}) => {
    const user = localStorage.getItem("xUser")?.trim();
    return await api.post("/api/bookings", {
        roomId: booking.roomId,
        startTime: toLocalISOString(booking.startTime),
        endTime: toLocalISOString(booking.endTime),
        userName: user || "",
    });
};

export const deleteBooking = async (id: number) =>
    (await api.delete(`/api/bookings/${id}`)).data;

// доступность
export const getAvailability = async (
    roomId: number,
    from: string,
    to: string
) =>
    (await api.get(`/api/rooms/${roomId}/availability`, {
        params: { from, to },
    })).data;

export default api;
