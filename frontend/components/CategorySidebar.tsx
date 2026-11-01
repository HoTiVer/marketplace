import Link from "next/link";
import { CategoryType } from "@/types/category.types";

interface SidebarProps {
    categories: CategoryType[];
}

export default function CategorySidebar({ categories }: SidebarProps) {
    return (
        <aside className="w-full lg:w-72 bg-white rounded-[2.5rem] shadow-xl shadow-gray-200/50 p-8 h-fit lg:sticky lg:top-28 border border-gray-100">
            <div className="flex items-center gap-3 mb-8">
                <span className="text-2xl">📂</span>
                <h2 className="text-xl font-black tracking-tighter text-gray-900">Categories</h2>
            </div>

            <nav>
                <ul className="flex flex-row lg:flex-col gap-2 overflow-x-auto lg:overflow-visible pb-4 lg:pb-0 scrollbar-hide">
                    {categories.map((cat) => (
                        <li key={cat.id} className="shrink-0 lg:shrink">
                            <Link
                                href={`/search?category=${cat.id}`}
                                className="flex items-center px-5 py-3.5 rounded-2xl text-sm font-bold text-gray-500 hover:bg-blue-50 hover:text-blue-600 border border-transparent hover:border-blue-100 transition-all active:scale-95 whitespace-nowrap"
                            >
                                {cat.name}
                            </Link>
                        </li>
                    ))}
                </ul>
            </nav>

            <div className="mt-10 pt-8 border-t border-gray-50 hidden lg:block">
                <div className="bg-gray-50 rounded-2xl p-5">
                    <p className="text-[9px] font-black uppercase tracking-[0.2em] text-gray-400 mb-2">Support</p>
                    <p className="text-xs font-bold text-gray-900">Need help finding something?</p>
                    <Link href="/help" className="text-blue-600 text-[10px] font-black uppercase mt-3 block hover:underline">
                        Contact Us →
                    </Link>
                </div>
            </div>
        </aside>
    );
}