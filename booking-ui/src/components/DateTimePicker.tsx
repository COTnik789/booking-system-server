import React from "react";

type Props = {
    label: string;
    value: string;
    onChange: (v: string) => void;
};

export default function DateTimePicker({ label, value, onChange }: Props) {
    // при изменении поля просто возвращаем строку без преобразований
    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = e.target.value;
        onChange(newValue);
    };

    return (
        <div className="field">
            <label className="field-label">{label}</label>
            <input
                type="datetime-local"
                className="input"
                value={value}
                onChange={handleChange}
                step={60} // шаг 1 минута
            />
        </div>
    );
}
