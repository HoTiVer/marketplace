"use client";

import { useState, useEffect, useRef } from "react";
import { ChevronDown } from "lucide-react";

const COUNTRIES = [
    { code: '+380', flag: '🇺🇦', name: 'UA' },
    { code: '+48', flag: '🇵🇱', name: 'PL' },
    { code: '+1', flag: '🇺🇸', name: 'US' },
    { code: '+49', flag: '🇩🇪', name: 'DE' },
];

interface PhoneInputProps {
    value: string;
    onChange: (val: string) => void;
    error?: string;
}

export function PhoneInput({ value, onChange, error }: PhoneInputProps) {
    const [isOpen, setIsOpen] = useState(false);
    const [selectedCountry, setSelectedCountry] = useState(COUNTRIES[0]);
    const dropdownRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const val = e.target.value;
        if (/^[0-9+]*$/.test(val) || val === "") {
            onChange(val);
        }
    };

    const selectCountry = (country: typeof COUNTRIES[0]) => {
        setSelectedCountry(country);
        onChange(country.code);
        setIsOpen(false);
    };

    return (
        <div className="space-y-2 w-full antialiased" ref={dropdownRef}>
            <label className="text-xs font-black text-black uppercase tracking-widest ml-1">
                Contact Phone
            </label>
            <div className="relative">
                <div className="absolute left-3 top-1/2 -translate-y-1/2 z-20 flex items-center">
                    <button
                        type="button"
                        onClick={() => setIsOpen(!isOpen)}
                        className="flex items-center gap-1.5 px-3 py-2 bg-white rounded-xl border border-gray-300 hover:border-black transition-all text-sm font-black text-black shadow-sm active:scale-95"
                    >
                        <span className="leading-none">{selectedCountry.flag}</span>
                        <ChevronDown
                            size={14}
                            className={`transition-transform duration-200 text-black ${isOpen ? 'rotate-180' : ''}`}
                            strokeWidth={3}
                        />
                    </button>

                    {/* Выпадающее меню */}
                    {isOpen && (
                        <div className="absolute top-full left-0 mt-2 bg-white border-2 border-black rounded-2xl shadow-[0_10px_40px_rgba(0,0,0,0.15)] p-1.5 w-40 animate-in fade-in zoom-in-95 duration-150 z-50">
                            {COUNTRIES.map((c) => (
                                <button
                                    key={c.code}
                                    type="button"
                                    onClick={() => selectCountry(c)}
                                    className="flex items-center justify-between w-full p-3 hover:bg-gray-100 rounded-xl transition-colors group"
                                >
                                    <span className="text-lg">{c.flag}</span>
                                    <span className="text-xs font-black text-black group-hover:text-blue-600 transition-colors">
                                        {c.code}
                                    </span>
                                </button>
                            ))}
                        </div>
                    )}
                </div>

                <input
                    type="text"
                    value={value}
                    onChange={handleInputChange}
                    maxLength={18}
                    className={`w-full bg-white border-2 rounded-2xl py-4 pl-28 pr-6 text-base font-black text-black outline-none transition-all placeholder:text-gray-300
                    ${error
                        ? 'border-red-500 ring-4 ring-red-500/5'
                        : 'border-gray-200 focus:border-black focus:shadow-[0_0_0_4px_rgba(0,0,0,0.03)]'
                    }`}
                    placeholder="+380..."
                    style={{ fontVariantNumeric: 'tabular-nums' }}
                />
            </div>
            {error && (
                <p className="text-[10px] font-black text-red-600 uppercase px-2 tracking-widest">
                    {error}
                </p>
            )}
        </div>
    );
}