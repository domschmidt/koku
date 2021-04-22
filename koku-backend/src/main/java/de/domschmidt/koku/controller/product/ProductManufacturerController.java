package de.domschmidt.koku.controller.product;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.product.ProductManufacturerDto;
import de.domschmidt.koku.persistence.model.ProductManufacturer;
import de.domschmidt.koku.service.IProductManufacturerService;
import de.domschmidt.koku.service.searchoptions.ProductManufacturerSearchOptions;
import de.domschmidt.koku.transformer.ProductManufacturerToProductManufacturerDtoTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productmanufacturers")
public class ProductManufacturerController extends AbstractController<ProductManufacturer, ProductManufacturerDto, ProductManufacturerSearchOptions> {

    @Autowired
    public ProductManufacturerController(final IProductManufacturerService manufacturerService) {
        super(manufacturerService, new ProductManufacturerToProductManufacturerDtoTransformer());
    }

    @GetMapping
    public List<ProductManufacturerDto> findAll(final ProductManufacturerSearchOptions productManufacturerSearchOptions) {
        return super.findAll(productManufacturerSearchOptions);
    }

    @GetMapping(value = "/{id}")
    public ProductManufacturerDto findByIdTransformed(@PathVariable("id") Long id) {
        return super.findByIdTransformed(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductManufacturerDto create(@RequestBody ProductManufacturerDto newProductManufacturer) {
        return super.create(newProductManufacturer);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") Long id, @RequestBody ProductManufacturerDto updatedDto) {
        super.update(id, updatedDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        final ProductManufacturer productManufacturer = super.findById(id);
        productManufacturer.setDeleted(true);
        this.service.update(productManufacturer);
    }
}
