'use client';

import { useState, useRef, useEffect } from "react";
import Link from "next/link";
import { useRouter, usePathname } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

export default function Header() {
    const { user, isLoggedIn, isLoading, logout, hasRole } = useAuth();
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState("");
    const dropdownRef = useRef<HTMLDivElement>(null);
    const router = useRouter();
    const pathname = usePathname();

    useEffect(() => {
        setIsMenuOpen(false);
    }, [pathname]);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsMenuOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        if (!searchQuery.trim()) return;
        router.push(`/search?q=${encodeURIComponent(searchQuery)}`);
    };

    const getPriorityRole = () => {
        if (hasRole('ADMIN')) return 'Administrator';
        if (hasRole('SELLER')) return 'Seller';
        return 'Member';
    };

    return (
        <header className="fixed top-0 left-0 w-full z-50 bg-white/90 backdrop-blur-md border-b border-gray-100 px-6">
            <div className="max-w-7xl mx-auto h-20 flex items-center justify-between gap-8">

                <Link href="/" className="flex items-center gap-2.5 group">
                    <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center group-hover:rotate-6 transition-transform duration-300 shadow-lg shadow-blue-200">
                        <span className="text-white font-black text-xl">M</span>
                    </div>
                    <span className="text-xl font-black tracking-tighter text-gray-900 hidden sm:block">
                        MY<span className="text-blue-600">SHOP</span>
                    </span>
                </Link>

                <form
                    onSubmit={handleSearch}
                    className="flex-1 max-w-md relative group hidden md:block">
                    <input
                        type="text"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        placeholder="Search for objects..."
                        className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3 px-6 pl-12 outline-none
                   text-gray-900 font-medium placeholder:text-gray-400
                   focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10
                   transition-all text-sm shadow-sm"
                    />
                    <span className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-blue-600 transition-colors text-base">
                        🔍
                    </span>
                </form>

                <div className="flex items-center gap-4">
                    {isLoading ? (
                        <div className="h-10 w-24 bg-gray-50 animate-pulse rounded-xl" />
                    ) : isLoggedIn ? (
                        <div className="flex items-center gap-3">
                            <Link href="/wishlist" className="p-3 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-xl transition-all hidden lg:block text-xl">
                                ♡
                            </Link>
                            <Link href="/cart" className="p-3 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-xl transition-all hidden lg:block text-xl">
                                🛒
                            </Link>

                            <div className="h-6 w-px bg-gray-100 mx-2 hidden lg:block" />

                            <div className="relative" ref={dropdownRef}>
                                <button
                                    onClick={() => setIsMenuOpen(!isMenuOpen)}
                                    className="flex items-center gap-3 p-1 pr-4 bg-gray-50 hover:bg-white border border-gray-100 rounded-xl transition-all group shadow-sm hover:shadow-md active:scale-95"
                                >
                                    <div className="w-9 h-9 bg-gray-900 rounded-lg flex items-center justify-center text-white text-xs font-bold shadow-md">
                                        {user?.displayName?.[0]?.toUpperCase() || 'U'}
                                    </div>
                                    <div className="text-left hidden sm:block leading-tight">
                                        <p className="text-xs font-bold text-gray-900">{user?.displayName || 'User'}</p>
                                        <p className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">{getPriorityRole()}</p>
                                    </div>
                                    <span className={`text-[10px] text-gray-400 transition-transform duration-300 ${isMenuOpen ? 'rotate-180' : ''}`}>
                                        ▼
                                    </span>
                                </button>

                                {isMenuOpen && (
                                    <div className="absolute right-0 mt-3 w-64 bg-white border border-gray-100 shadow-2xl shadow-gray-200/50 rounded-2xl overflow-hidden p-2 animate-in fade-in slide-in-from-top-2 duration-200">
                                        <div className="px-4 py-2 mb-1">
                                            <p className="text-[10px] font-bold uppercase tracking-widest text-gray-400">Account</p>
                                        </div>

                                        <MenuLink href="/profile" label="Personal Cabinet" icon="👤" />
                                        <MenuLink href="/user-orders" label="Order History" icon="📦" />
                                        <MenuLink href="/chats" label="Messages" icon="💬" />

                                        {hasRole('SELLER') && (
                                            <>
                                                <div className="px-4 py-2 mt-2 mb-1 border-t border-gray-50 pt-3">
                                                    <p className="text-[10px] font-bold uppercase tracking-widest text-emerald-500">Seller</p>
                                                </div>
                                                <MenuLink href="/seller-orders" label="Incoming Orders" icon="📈" isSeller />
                                                <MenuLink href="/inventory" label="Inventory" icon="🛠" isSeller />
                                            </>
                                        )}

                                        {hasRole('ADMIN') && (
                                            <>
                                                <div className="px-4 py-2 mt-2 mb-1 border-t border-gray-50 pt-3">
                                                    <p className="text-[10px] font-bold uppercase tracking-widest text-blue-500">Admin</p>
                                                </div>
                                                <MenuLink href="/admin/dashboard" label="Dashboard" icon="⚡" isAdmin />
                                                <MenuLink href="/categories" label="Categories" icon="📂" isAdmin />
                                            </>
                                        )}

                                        <button
                                            onClick={logout}
                                            className="w-full mt-2 flex items-center gap-3 px-4 py-3 text-left text-red-500 hover:bg-red-50 rounded-xl transition-colors font-bold text-xs border-t border-gray-50 pt-4"
                                        >
                                            <span className="text-base">✕</span> Sign Out
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>
                    ) : (
                        <div className="flex items-center gap-4">
                            <Link href="/auth/login" className="text-sm font-bold text-gray-500 hover:text-blue-600 transition-colors">
                                Log in
                            </Link>
                            <Link href="/auth/register" className="bg-blue-600 text-white px-6 py-3 rounded-xl text-sm font-bold hover:bg-blue-700 hover:shadow-lg hover:shadow-blue-200 transition-all active:scale-95">
                                Join Now
                            </Link>
                        </div>
                    )}
                </div>
            </div>
        </header>
    );
}

function MenuLink({ href, label, icon, isSeller, isAdmin }: { href: string; label: string; icon: string; isSeller?: boolean; isAdmin?: boolean }) {
    const textColor = isSeller ? 'text-emerald-600' : isAdmin ? 'text-blue-600' : 'text-gray-700';

    return (
        <Link
            href={href}
            className={`flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-gray-50 transition-all group ${textColor}`}
        >
            <span className="text-lg transition-transform group-hover:scale-110 duration-200">
                {icon}
            </span>
            <span className="text-sm font-bold">{label}</span>
        </Link>
    );
}