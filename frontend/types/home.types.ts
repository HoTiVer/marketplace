import {CategoryType} from "@/types/category.types";
import {ProductSummary} from "@/types/product.types";

export interface HomeApiResponse {
    categories: CategoryType[];
    featuredProducts: ProductSummary[];
    newProducts: ProductSummary[];
    popularProducts: ProductSummary[];
}