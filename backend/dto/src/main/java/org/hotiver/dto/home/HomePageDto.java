package org.hotiver.dto.home;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hotiver.dto.category.CategoryDto;
import org.hotiver.dto.product.ListProductDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class HomePageDto {

    private List<CategoryDto> categories;
    private List<ListProductDto> featuredProducts;
    private List<ListProductDto> newProducts;
    private List<ListProductDto> popularProducts;

}
