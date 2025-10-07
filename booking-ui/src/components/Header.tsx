import { useEffect, useId, useState } from "react";

type Props = {
    user: string;
    onUserChange: (v: string) => void;
};

export default function Header({ user, onUserChange }: Props) {
    const id = useId();
    const [value, setValue] = useState(user);

    useEffect(() => setValue(user), [user]);

    return (
        <header className="header">
            <div className="brand">Booking System</div>

            <div className="userbox">
                <label className="user-label" htmlFor={id}>
                    Пользователь
                </label>
                <input
                    id={id}
                    className="input user-input"
                    type="text"
                    placeholder="Введите имя (X-User)"
                    value={value}
                    onChange={(e) => {
                        const v = e.target.value.trim();
                        setValue(v);
                        onUserChange(v);
                        localStorage.setItem("xUser", v);
                    }}
                />
            </div>
        </header>
    );
}
