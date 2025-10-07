import { useId } from "react";
import DatePicker from "react-datepicker";
import { ru } from "date-fns/locale";
import "react-datepicker/dist/react-datepicker.css";

type Props = {
    label: string;
    value: Date | null;
    onChange: (d: Date | null) => void;
};

export default function DatePickerField({ label, value, onChange }: Props) {
    const id = useId();
    return (
        <label className="field" htmlFor={id}>
            <span className="field-label">{label}</span>
            <DatePicker
                id={id}
                selected={value}
                onChange={onChange}
                dateFormat="yyyy-MM-dd"
                placeholderText="YYYY-MM-DD"
                className="input"
                locale={ru}
                isClearable
            />
        </label>
    );
}
