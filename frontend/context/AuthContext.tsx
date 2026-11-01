'use client';

import { createContext, useContext, useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { UserContextType } from "@/types/user.types";
import { useRouter, usePathname } from 'next/navigation';
import { authService } from "@/services/authService";

interface AuthContextType {
    user: UserContextType | null;
    isLoggedIn: boolean;
    isLoading: boolean;
    checkAuth: () => Promise<void>;
    logout: () => Promise<void>;
    hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType>({
    user: null,
    isLoggedIn: false,
    isLoading: true,
    checkAuth: async () => {},
    logout: async () => {},
    hasRole: () => false,
});

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    const [user, setUser] = useState<UserContextType | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const isChecking = useRef(false);

    const router = useRouter();
    const pathname = usePathname();

    const checkAuth = useCallback(async () => {
        if (isChecking.current) return;
        isChecking.current = true;
        setIsLoading(true);

        const isAuthPage = pathname.startsWith('/auth');

        try {
            const data = await authService.getMe();

            if (!data) {
                setUser(null);
            } else {
                setUser(data);

                if (isAuthPage) {
                    router.push('/');
                }
            }
        } catch (error: unknown) {
            console.error('Authentication check failed:', error);
            setUser(null);
        } finally {
            setIsLoading(false);
            isChecking.current = false;
        }
    }, [pathname, router]);

    const logout = useCallback(async () => {
        try {
            await authService.logout();
        } catch (e) {
            console.error("Logout request failed", e);
        } finally {
            setUser(null);
            router.push('/auth/login');
        }
    }, [router]);

    const hasRole = useCallback((role: string) => {
        return user?.roles?.includes(role) ?? false;
    }, [user]);

    useEffect(() => {
        checkAuth();
    }, [checkAuth]);

    const value = useMemo(() => ({
        user,
        isLoggedIn: !!user,
        isLoading,
        checkAuth,
        logout,
        hasRole
    }), [user, isLoading, checkAuth, logout, hasRole]);

    return (
        <AuthContext.Provider value={value}>
            {!isLoading ? children : (
                <div className="h-screen w-screen flex items-center justify-center bg-zinc-950 text-white font-mono text-sm tracking-widest">
                    <div className="flex flex-col items-center gap-2">
                        <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                        <span>INITIALIZING SECURITY NODE...</span>
                    </div>
                </div>
            )}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);