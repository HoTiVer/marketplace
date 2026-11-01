import type { ReactNode, InputHTMLAttributes } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
    label: string;
    icon?: ReactNode;
    error?: string;
}

export function SmartInput({ label, icon, error, ...props }: InputProps) {
    return (
        <div className="space-y-2 w-full">
            <label className="text-xs font-bold text-gray-400 uppercase tracking-widest ml-1">
                {label}
            </label>

            <div className="relative group">
                {icon && (
                    <div className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-blue-600 transition-colors">
                        {icon}
                    </div>
                )}
                <input
                    {...props}
                    className={`w-full bg-gray-50 border rounded-2xl py-4 ${icon ? 'pl-14' : 'px-6'} pr-6 text-sm font-bold text-gray-900 outline-none transition-all
                    ${error ? 'border-red-500 ring-4 ring-red-500/5' : 'border-gray-100 focus:bg-white focus:border-blue-600 focus:shadow-lg focus:shadow-blue-50'}`}
                />
            </div>
            {error && (
                <p className="text-[10px] font-bold text-red-500 uppercase px-2">
                    {error}
                </p>
            )}
        </div>
    );
}