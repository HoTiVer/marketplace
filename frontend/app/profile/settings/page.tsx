'use client';

import Link from 'next/link';
import {
    Lock, Mail, ShieldCheck,
    Trash2, ChevronRight, UserCircle,
    ArrowLeft
} from 'lucide-react';

interface SettingsCardProps {
    title: string;
    description: string;
    icon: React.ReactNode;
    href: string;
    variant?: 'default' | 'danger';
}

const SettingsCard = ({ title, description, icon, href, variant = 'default' }: SettingsCardProps) => (
    <Link href={href} className="group block">
        <div className={`p-5 rounded-xl border-2 transition-all duration-200 flex items-center justify-between 
            ${variant === 'danger'
            ? 'bg-white border-red-100 hover:border-red-600'
            : 'bg-white border-gray-100 hover:border-black'}`}>

            <div className="flex items-center gap-5">
                <div className={`w-12 h-12 rounded-lg flex items-center justify-center transition-colors
                    ${variant === 'danger'
                    ? 'bg-red-50 text-red-600'
                    : 'bg-gray-50 text-black group-hover:bg-black group-hover:text-white'}`}>
                    {icon}
                </div>
                <div>
                    <h3 className={`text-sm font-black uppercase tracking-tight ${variant === 'danger' ? 'text-red-600' : 'text-black'}`}>
                        {title}
                    </h3>
                    <p className="text-[9px] font-bold text-gray-400 uppercase tracking-[0.1em] mt-0.5">
                        {description}
                    </p>
                </div>
            </div>

            <ChevronRight size={18} className={`${variant === 'danger' ? 'text-red-200' : 'text-gray-300'} group-hover:text-black group-hover:translate-x-1 transition-all`} />
        </div>
    </Link>
);

export default function SettingsHubPage() {
    return (
        <main className="bg-white min-h-screen pt-20 p-4 md:p-8 selection:bg-black selection:text-white">
            <div className="max-w-2xl mx-auto space-y-10">
                <header className="border-b-2 border-black pb-8">
                    <Link href="/profile" className="inline-flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-gray-400 hover:text-black transition-colors mb-6">
                        <ArrowLeft size={12} /> Return to Dashboard
                    </Link>
                    <h1 className="text-6xl font-black tracking-[ -0.05em] text-black uppercase leading-none">
                        Settings<span className="text-gray-200">.</span>
                    </h1>
                </header>

                <div className="grid grid-cols-1 gap-3">
                    <p className="text-[10px] font-black text-gray-300 uppercase tracking-[0.3em] mb-2">Account Configuration</p>

                    <SettingsCard
                        title="Profile Identity"
                        description="Public information and metadata"
                        icon={<UserCircle size={20} strokeWidth={2.5} />}
                        href="/settings/profile"
                    />

                    <SettingsCard
                        title="Access Credentials"
                        description="Key-based security management"
                        icon={<Lock size={20} strokeWidth={2.5} />}
                        href="/settings/password"
                    />

                    <SettingsCard
                        title="System Email"
                        description="Primary communication channel"
                        icon={<Mail size={20} strokeWidth={2.5} />}
                        href="/settings/email"
                    />

                    <SettingsCard
                        title="Verification"
                        description="Multi-factor authorization"
                        icon={<ShieldCheck size={20} strokeWidth={2.5} />}
                        href="/settings/2fa"
                    />

                    <div className="pt-10">
                        <p className="text-[10px] font-black text-red-300 uppercase tracking-[0.3em] mb-4">Restrictive Actions</p>
                        <SettingsCard
                            title="Purge Data"
                            description="Permanent account deactivation"
                            icon={<Trash2 size={20} strokeWidth={2.5} />}
                            href="/settings/delete"
                            variant="danger"
                        />
                    </div>
                </div>

                <footer className="pt-12 text-[9px] font-bold text-gray-300 uppercase tracking-widest flex justify-between border-t border-gray-100">
                    <span>Account ID: 0x2241</span>
                    <span>System v1.0.4</span>
                </footer>
            </div>
        </main>
    );
}