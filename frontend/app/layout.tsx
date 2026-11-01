import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import QueryProvider from "@/providers/QueryProvider";
import Header from "@/components/Header";
import {AuthProvider} from "@/context/AuthContext";

const geistSans = Geist({
    variable: "--font-geist-sans",
    subsets: ["latin"],
});

const geistMono = Geist_Mono({
    variable: "--font-geist-mono",
    subsets: ["latin"],
});

export const metadata: Metadata = {
    title: "Marketplace",
    description: "My marketplace",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
    return (
        <html lang="en">
        <body className={`${geistSans.variable} ${geistMono.variable} antialiased bg-gray-100 min-h-screen`}>
        <QueryProvider>
            <AuthProvider>
                <Header />
                <div className="pt-16">
                    {children}
                </div>
            </AuthProvider>
        </QueryProvider>
        </body>
        </html>
    );
}
