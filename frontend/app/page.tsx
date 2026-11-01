import { marketService } from "@/services/homeService";
import CategorySidebar from "@/components/CategorySidebar";
import ProductSection from "@/components/ProductSection";

export const dynamic = "force-dynamic";

export default async function HomePage() {
    const data = await marketService.getHomeData();

    return (
        <main className="bg-gray-50 min-h-screen pt-24 pb-20 px-4 md:px-8 selection:bg-blue-100 selection:text-blue-900">
            <div className="max-w-[1440px] mx-auto">
                <div className="flex flex-col lg:flex-row gap-10 items-start">

                    <CategorySidebar categories={data.categories} />

                    <div className="flex-1 space-y-16">

                        <div className="bg-blue-600 rounded-[2.5rem] p-10 md:p-16 text-white shadow-2xl shadow-blue-200 relative overflow-hidden group">
                            <div className="relative z-10 max-w-lg">
                                <h1 className="text-4xl md:text-6xl font-black tracking-tighter mb-6 leading-none">
                                    Next Gen <br/> <span className="text-blue-200">Shopping.</span>
                                </h1>
                                <p className="text-blue-100 font-bold uppercase tracking-widest text-[10px] opacity-80">
                                    Discover curated products from top verified sellers
                                </p>
                            </div>
                            <span className="absolute right-10 bottom-10 text-8xl opacity-20 group-hover:scale-110 transition-transform duration-700">🚀</span>
                        </div>

                        <div className="space-y-20">
                            <ProductSection
                                title="Featured"
                                subtitle="Handpicked for your style"
                                products={data.featuredProducts}
                                emoji="✨"
                            />

                            <ProductSection
                                title="New Arrivals"
                                subtitle="Fresh from our merchants"
                                products={data.newProducts}
                                emoji="🔥"
                            />

                            <ProductSection
                                title="Most Popular"
                                subtitle="What everyone is talking about"
                                products={data.popularProducts}
                                emoji="📊"
                            />
                        </div>

                        {data.featuredProducts.length === 0 &&
                            data.newProducts.length === 0 && (
                                <div className="text-center py-32 bg-white rounded-[3rem] border-2 border-dashed border-gray-100">
                                    <span className="text-5xl mb-4 block">🔍</span>
                                    <p className="text-sm font-black uppercase tracking-widest text-gray-400">
                                        No products found in the catalog
                                    </p>
                                </div>
                            )}
                    </div>
                </div>
            </div>
        </main>
    );
}