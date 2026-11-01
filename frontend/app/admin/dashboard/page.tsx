'use client';

import Link from 'next/link';

export default function AdminDashboard() {
    const adminModules = [
        {
            title: "Products",
            desc: "Manage catalog, prices and stock levels",
            path: "/admin/products",
            icon: "📦",
            count: "124 items",
        },
        {
            title: "Orders",
            desc: "Track and update shipping status",
            path: "/admin/orders",
            icon: "💳",
            count: "12 active",
        },
        {
            title: "Verification",
            desc: "Review seller registration requests",
            path: "/admin/requests",
            icon: "📝",
            count: "New",
        },
        {
            title: "Analytics",
            desc: "Sales reports and traffic data",
            path: "/admin/analytics",
            icon: "📊",
            count: "Live",
        }
    ];

    return (
        <main className="min-h-screen bg-[#fafafa] pt-20 pb-20 px-6 selection:bg-blue-600 selection:text-white">
            <div className="max-w-5xl mx-auto">

                <header className="mb-12 flex items-center justify-between border-b-2 border-gray-900 pb-10">
                    <div>
                        <h1 className="text-4xl font-black tracking-[ -0.05em] text-gray-900 uppercase leading-none">
                            Admin<span className="text-blue-600">.</span>Panel
                        </h1>
                        <p className="text-[10px] font-bold uppercase tracking-[0.5em] text-gray-400 mt-3">
                            Control Interface v3.0
                        </p>
                    </div>

                    <Link href="/" className="text-[10px] font-bold uppercase tracking-widest bg-gray-900 text-white px-8 py-4 rounded-xl hover:bg-blue-600 transition-all duration-300 shadow-lg shadow-gray-900/10">
                        Exit to site
                    </Link>
                </header>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {adminModules.map((module) => (
                        <Link
                            key={module.path}
                            href={module.path}
                            className="group relative bg-white p-10 rounded-[2.5rem] border border-blue-100 shadow-[0_20px_40px_rgba(37,99,235,0.06)] hover:shadow-[0_30px_60px_rgba(37,99,235,0.12)] hover:border-blue-400 transition-all duration-500 flex flex-col justify-between min-h-[220px] overflow-hidden"
                        >
                            <div className="absolute -right-10 -bottom-10 w-40 h-40 bg-blue-50/50 rounded-full blur-3xl opacity-100 group-hover:bg-blue-100/60 transition-colors duration-500" />

                            <div className="relative z-10 flex justify-between items-start">
                                <div className="w-14 h-14 bg-blue-50/50 rounded-2xl flex items-center justify-center text-2xl border border-blue-100 group-hover:scale-110 transition-transform duration-500">
                                    <span className="grayscale-0">{module.icon}</span>
                                </div>

                                <span className={`text-[10px] font-black uppercase tracking-widest px-4 py-2 rounded-xl border transition-all ${
                                    module.title === "Verification"
                                        ? 'bg-blue-600 text-white border-blue-600 animate-pulse'
                                        : 'bg-white text-gray-400 border-gray-100 group-hover:text-blue-600 group-hover:border-blue-200'
                                }`}>
                                    {module.count}
                                </span>
                            </div>

                            <div className="relative z-10 mt-8">
                                <h2 className="text-2xl font-black uppercase tracking-tight text-gray-900 group-hover:text-blue-600 transition-colors">
                                    {module.title}
                                </h2>
                                <p className="text-[11px] font-medium text-gray-400 uppercase tracking-widest mt-2 leading-relaxed">
                                    {module.desc}
                                </p>
                            </div>

                            <div className="absolute left-0 top-0 bottom-0 w-1.5 bg-blue-600 opacity-20 group-hover:opacity-100 transition-opacity duration-300" />
                        </Link>
                    ))}
                </div>

            </div>
        </main>
    );
}