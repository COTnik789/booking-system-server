export type RoomDto = {
    id: number;
    name: string;
    openTime: string;  // HH:mm:ss
    closeTime: string; // HH:mm:ss
};

export type CreateRoomPayload = {
    name: string;
    openTime: string;   // HH:mm:ss
    closeTime: string;  // HH:mm:ss
};

export type BookingDto = {
    id: number;
    roomId: number;
    userName: string;
    startTime: string; // yyyy-MM-dd'T'HH:mm:ss
    endTime: string;   // yyyy-MM-dd'T'HH:mm:ss
};

export type CreateBookingPayload = {
    roomId: number;
    startTime: string; // yyyy-MM-dd'T'HH:mm:ss
    endTime: string;   // yyyy-MM-dd'T'HH:mm:ss
};

export type AvailabilityDto = {
    start: string;        // yyyy-MM-dd'T'HH:mm:ss | дата дня
    end?: string | null;  // для неполных интервалов
    fullDayFree: boolean; // когда свободен весь день
};
