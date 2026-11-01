import { ProductSummary } from "@/types/product.types";
import ProductCard from './ProductCard';
import Link from "next/link";

interface ProductSectionProps {
    title: string;
    subtitle?: string;
    products: ProductSummary[];
    emoji?: string;
}

export default function ProductSection({ title, subtitle, products, emoji }: ProductSectionProps) {
    if (!products.length) return null;

    return (
        <section>
            <div className="flex items-end justify-between mb-10 px-2">
                <div className="space-y-1">
                    <div className="flex items-center gap-3">
                        {emoji && <span className="text-3xl">{emoji}</span>}
                        <h2 className="text-4xl font-black tracking-tighter text-gray-900">
                            {title}
                        </h2>
                    </div>
                    {subtitle && (
                        <p className="text-[10px] font-black uppercase tracking-[0.2em] text-gray-400 ml-1">
                            {subtitle}
                        </p>
                    )}
                </div>

                <Link
                    href="/search"
                    className="text-xs font-black uppercase tracking-widest text-blue-600 hover:text-blue-700 transition-colors hidden sm:block border-b-2 border-blue-100 pb-1"
                >
                    View All →
                </Link>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
                {products.map((product) => (
                    <ProductCard key={product.productId} product={product} />
                ))}
            </div>
        </section>
    );
}