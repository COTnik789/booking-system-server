import { createContext, useContext, useState } from "react";

type AlertType = "success" | "error" | "info";
type Alert = { type: AlertType; message: string } | null;

const AlertContext = createContext<{
    alert: Alert;
    show: (type: AlertType, message: string) => void;
    hide: () => void;
}>({
    alert: null,
    show: () => {},
    hide: () => {},
});

export function AlertProvider({ children }: { children: React.ReactNode }) {
    const [alert, setAlert] = useState<Alert>(null);

    function show(type: AlertType, message: string) {
        setAlert({ type, message });
        setTimeout(() => setAlert(null), 3000);
    }

    function hide() {
        setAlert(null);
    }

    return (
        <AlertContext.Provider value={{ alert, show, hide }}>
            {alert && (
                <div className={`alert ${alert.type}`}>
                    <span>{alert.message}</span>
                </div>
            )}
            {children}
        </AlertContext.Provider>
    );
}

export function useAlert() {
    return useContext(AlertContext);
}
