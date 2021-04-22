package de.domschmidt.koku.controller.product;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.KokuColor;
import de.domschmidt.koku.dto.product.ProductDto;
import de.domschmidt.koku.dto.charts.ChartData;
import de.domschmidt.koku.dto.charts.ChartDataSet;
import de.domschmidt.koku.dto.charts.ChartTypeEnum;
import de.domschmidt.koku.dto.charts.ChartYearMonthFilter;
import de.domschmidt.koku.dto.panels.ChartPanelDto;
import de.domschmidt.koku.persistence.model.*;
import de.domschmidt.koku.service.ICustomerAppointmentService;
import de.domschmidt.koku.service.IProductPriceHistoryEntryService;
import de.domschmidt.koku.service.IProductService;
import de.domschmidt.koku.service.searchoptions.CustomerAppointmentSearchOptions;
import de.domschmidt.koku.service.searchoptions.ProductSearchOptions;
import de.domschmidt.koku.transformer.ProductToProductDtoTransformer;
import de.domschmidt.koku.utils.NPEGuardUtils;
import de.domschmidt.koku.utils.ProductPriceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;

@RestController
@RequestMapping("/products")
public class ProductController extends AbstractController<Product, ProductDto, ProductSearchOptions> {

    private final ProductToProductDtoTransformer transformer;
    private final IProductPriceHistoryEntryService productPriceHistoryEntryService;
    private final ICustomerAppointmentService appointmentService;

    @Autowired
    public ProductController(final IProductService productService,
                             final ICustomerAppointmentService appointmentService,
                             final IProductPriceHistoryEntryService productPriceHistoryEntryService,
                             final ProductToProductDtoTransformer transformer) {
        super(productService, transformer);
        this.transformer = transformer;
        this.appointmentService = appointmentService;
        this.productPriceHistoryEntryService = productPriceHistoryEntryService;
    }

    @GetMapping
    public List<ProductDto> findAll(final ProductSearchOptions searchOptions) {
        final List<Product> models = this.service.findAll(searchOptions);
        return this.transformer.transformToDtoList(models, searchOptions);
    }

    @GetMapping(value = "/{id}")
    public ProductDto findByIdTransformed(@PathVariable("id") Long id) {
        return super.findByIdTransformed(id);
    }

    @GetMapping(value = "/{id}/statistics/usage")
    public ChartPanelDto findUsageById(@PathVariable("id") Long id) {
        final Product product = this.service.findById(id);
        final List<ActivitySequenceItem> usageInActivitySequences = product.getUsageInActivitySequences();

        final Map<Integer, Integer> usageStatisticsPerYear = new TreeMap<>();
        for (final ActivitySequenceItem usageInActivitySequence : usageInActivitySequences) {
            final CustomerAppointment customerAppointment = usageInActivitySequence.getCustomerAppointment();
            final int appointmentYear = customerAppointment.getStart().getYear();

            if (usageStatisticsPerYear.containsKey(appointmentYear)) {
                usageStatisticsPerYear.put(appointmentYear, usageStatisticsPerYear.get(appointmentYear) + 1);
            } else {
                usageStatisticsPerYear.put(appointmentYear, 1);
            }

        }

        final List<String> sortedYears = new ArrayList<>();
        final List<BigDecimal> sortedUsage = new ArrayList<>();

        for (final Map.Entry<Integer, Integer> currentEntry : usageStatisticsPerYear.entrySet()) {
            sortedYears.add(String.valueOf(currentEntry.getKey()));
            sortedUsage.add(new BigDecimal(currentEntry.getValue()));
        }

        return ChartPanelDto.builder()
                .type(ChartTypeEnum.BAR)
                .data(ChartData.builder()
                        .labels(sortedYears)
                        .datasets(Collections.singletonList(
                                ChartDataSet.builder()
                                        .label("Verwendung in Behandlungen")
                                        .data(sortedUsage)
                                        .color(KokuColor.PRIMARY)
                                        .build()
                        ))
                        .build())
                .build();
    }

    @GetMapping(value = "/{id}/statistics/revenue")
    @Transactional(readOnly = true)
    public ChartPanelDto findRevenueById(@PathVariable("id") Long id) {
        final Product product = this.service.findById(id);
        final List<CustomerAppointmentSoldProduct> allSoldProductAppointments = product.getUsageInSoldProducts();

        final Map<Integer, BigDecimal> revenueStatisticsPerYear = new TreeMap<>();
        for (final CustomerAppointmentSoldProduct soldInProductAppointment : allSoldProductAppointments) {
            final CustomerAppointment customerAppointment = soldInProductAppointment.getCustomerAppointment();
            final int appointmentYear = customerAppointment.getStart().getYear();

            final BigDecimal productPriceAtDate = ProductPriceUtils.getSoldProductPriceForCustomerAppointment(
                    soldInProductAppointment,
                    customerAppointment
            );

            if (revenueStatisticsPerYear.containsKey(appointmentYear)) {
                final BigDecimal oldRevenue = revenueStatisticsPerYear.get(appointmentYear);
                revenueStatisticsPerYear.put(appointmentYear, oldRevenue.add(productPriceAtDate));
            } else {
                revenueStatisticsPerYear.put(appointmentYear, productPriceAtDate);
            }
        }

        final List<String> sortedYears = new ArrayList<>();
        final List<BigDecimal> sortedRevenue = new ArrayList<>();

        for (final Map.Entry<Integer, BigDecimal> currentEntry : revenueStatisticsPerYear.entrySet()) {
            sortedYears.add(String.valueOf(currentEntry.getKey()));
            sortedRevenue.add(currentEntry.getValue());
        }

        return ChartPanelDto.builder()
                .type(ChartTypeEnum.BAR)
                .data(ChartData.builder()
                        .labels(sortedYears)
                        .datasets(Collections.singletonList(
                                ChartDataSet.builder()
                                        .label("Umsatz in Verkäufen")
                                        .data(sortedRevenue)
                                        .color(KokuColor.PRIMARY)
                                        .build()
                        ))
                        .build())
                .build();
    }

    @GetMapping(value = "/statistics/mostsold")
    public ChartPanelDto findMostSoldProducts(
            @RequestParam(required = false) YearMonth start,
            @RequestParam(required = false) YearMonth end
    ) {
        if (start == null) {
            start = Year.now().atMonth(1);
        }
        if (end == null) {
            end = YearMonth.now();
        }
        final CustomerAppointmentSearchOptions searchOptions = CustomerAppointmentSearchOptions.builder()
                .start(start.atDay(1))
                .end(end.atEndOfMonth())
                .build();
        final List<CustomerAppointment> allAppointments = this.appointmentService.findAllAppointmentsOfAllUsers(searchOptions);

        final Map<Product, Integer> soldStatisticPerProduct = new HashMap<>();
        for (final CustomerAppointment customerAppointment : allAppointments) {
            for (final CustomerAppointmentSoldProduct product : customerAppointment.getSoldProducts()) {
                if (soldStatisticPerProduct.containsKey(product.getProduct())) {
                    soldStatisticPerProduct.put(product.getProduct(), soldStatisticPerProduct.get(product.getProduct()) + 1);
                } else {
                    soldStatisticPerProduct.put(product.getProduct(), 1);
                }
            }
        }

        final List<String> sortedProductNames = new ArrayList<>();
        final List<BigDecimal> sortedProductSellCount = new ArrayList<>();

        for (final Map.Entry<Product, Integer> currentEntry : soldStatisticPerProduct.entrySet()) {
            sortedProductNames.add(currentEntry.getKey().getDescription());
            sortedProductSellCount.add(new BigDecimal(currentEntry.getValue()));
        }

        return ChartPanelDto.builder()
                .type(ChartTypeEnum.BAR)
                .data(ChartData.builder()
                        .labels(sortedProductNames)
                        .datasets(Collections.singletonList(
                                ChartDataSet.builder()
                                        .label("Verkäufe")
                                        .data(sortedProductSellCount)
                                        .color(KokuColor.PRIMARY)
                                        .build()
                        ))
                        .build())
                .filters(Arrays.asList(
                        ChartYearMonthFilter.builder()
                                .label("von")
                                .queryParam("start")
                                .value(start)
                                .build(),
                        ChartYearMonthFilter.builder()
                                .label("bis")
                                .queryParam("end")
                                .value(end)
                                .build()
                ))
                .build();
    }

    @GetMapping(value = "/statistics/mostrevenue")
    @Transactional(readOnly = true)
    public ChartPanelDto findProductsWithMostRevenue(
            @RequestParam(required = false) YearMonth start,
            @RequestParam(required = false) YearMonth end
    ) {
        if (start == null) {
            start = Year.now().atMonth(1);
        }
        if (end == null) {
            end = YearMonth.now();
        }
        final CustomerAppointmentSearchOptions searchOptions = CustomerAppointmentSearchOptions.builder()
                .start(start.atDay(1))
                .end(end.atEndOfMonth())
                .build();
        final List<CustomerAppointment> allAppointments = this.appointmentService.findAllAppointmentsOfAllUsers(searchOptions);

        final Map<Product, BigDecimal> revenueStatisticPerProduct = new HashMap<>();
        for (final CustomerAppointment customerAppointment : allAppointments) {
            for (final CustomerAppointmentSoldProduct soldProduct : customerAppointment.getSoldProducts()) {
                final BigDecimal productPriceAtAppointmentTime = ProductPriceUtils.getSoldProductPriceForCustomerAppointment(
                        soldProduct,
                        customerAppointment
                );

                if (revenueStatisticPerProduct.containsKey(soldProduct.getProduct())) {
                    revenueStatisticPerProduct.put(soldProduct.getProduct(), revenueStatisticPerProduct.get(soldProduct.getProduct()).add(productPriceAtAppointmentTime));
                } else {
                    revenueStatisticPerProduct.put(soldProduct.getProduct(), productPriceAtAppointmentTime);
                }
            }
        }

        final List<String> sortedProductNames = new ArrayList<>();
        final List<BigDecimal> sortedProductSellCount = new ArrayList<>();

        for (final Map.Entry<Product, BigDecimal> currentEntry : revenueStatisticPerProduct.entrySet()) {
            sortedProductNames.add(currentEntry.getKey().getDescription());
            sortedProductSellCount.add(currentEntry.getValue());
        }


        return ChartPanelDto.builder()
                .type(ChartTypeEnum.BAR)
                .data(ChartData.builder()
                        .labels(sortedProductNames)
                        .datasets(Collections.singletonList(
                                ChartDataSet.builder()
                                        .label("Umsatz")
                                        .data(sortedProductSellCount)
                                        .color(KokuColor.PRIMARY)
                                        .build()
                        ))
                        .build())
                .filters(Arrays.asList(
                        ChartYearMonthFilter.builder()
                                .label("von")
                                .queryParam("start")
                                .value(start)
                                .build(),
                        ChartYearMonthFilter.builder()
                                .label("bis")
                                .queryParam("end")
                                .value(end)
                                .build()
                ))
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto create(@RequestBody ProductDto newProduct) {
        final Product model = this.transformer.transformToEntity(newProduct);
        final Product savedModel = this.service.create(model);
        final ProductPriceHistoryEntry newPriceEntry = this.transformer.createNewPriceEntry(newProduct, savedModel);
        savedModel.setPriceHistory(Collections.singletonList(newPriceEntry));
        this.productPriceHistoryEntryService.update(newPriceEntry);
        return this.transformer.transformToDto(savedModel);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void update(@PathVariable("id") Long id, @RequestBody ProductDto updatedDto) {
        final Product model = this.transformer.transformToEntity(updatedDto);
        List<ProductPriceHistoryEntry> priceHistory = this.service.findById(model.getId()).getPriceHistory();
        if (priceHistory == null) {
            priceHistory = new ArrayList<>();
        }
        this.service.update(model);
        boolean noHistoryYet = priceHistory.isEmpty();
        if (noHistoryYet || NPEGuardUtils.get(priceHistory.get(priceHistory.size() - 1).getPrice()).compareTo(NPEGuardUtils.get(updatedDto.getCurrentPrice())) != 0) {
            final ProductPriceHistoryEntry newPriceEntry = this.transformer.createNewPriceEntry(updatedDto, model);
            this.productPriceHistoryEntryService.update(newPriceEntry);
        }
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        final Product product = super.findById(id);
        product.setDeleted(true);
        this.service.update(product);
    }
}
