import { useEffect, useState } from "react";
import Header from "./components/Header";
import Rooms from "./components/Rooms";
import NewRoomForm from "./components/NewRoomForm";
import Bookings from "./components/Bookings";
import NewBookingForm from "./components/NewBookingForm";
import Availability from "./components/Availability";
import { AlertProvider } from "./Alert";

export default function App() {
    const [user, setUser] = useState<string>(() => localStorage.getItem("xUser") ?? "");
    const [selectedRoomId, setSelectedRoomId] = useState<number | null>(null);
    const [refreshKey, setRefreshKey] = useState(0);

    useEffect(() => {
        localStorage.setItem("xUser", user);
    }, [user]);

    const reloadAll = () => setRefreshKey((k) => k + 1);

    return (
        <AlertProvider>
            <div className="container">
                <Header user={user} onUserChange={setUser} />

                <div className="grid">
                    <div className="card pad">
                        <h2>Помещения</h2>
                        <Rooms
                            key={`rooms-${refreshKey}`}
                            selectedRoomId={selectedRoomId}
                            onSelect={setSelectedRoomId}
                        />
                        <div className="mt">
                            <NewRoomForm onCreated={reloadAll} />
                        </div>
                    </div>

                    <div className="card pad">
                        <h2>Брони</h2>
                        <Bookings
                            key={`bookings-${selectedRoomId}-${refreshKey}`}
                            roomId={selectedRoomId}
                            onDeleted={reloadAll}
                        />
                        <div className="mt">
                            <NewBookingForm selectedRoomId={selectedRoomId} onCreated={reloadAll} />
                        </div>
                    </div>
                </div>

                <div className="card pad mt">
                    <h2>Свободные интервалы</h2>
                    <Availability
                        key={`availability-${selectedRoomId}-${refreshKey}`}
                        roomId={selectedRoomId}
                    />
                </div>
            </div>
        </AlertProvider>
    );
}
