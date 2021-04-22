package de.domschmidt.koku.controller.customer;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.data.CustomerStatistics;
import de.domschmidt.koku.dto.customer.CustomerDto;
import de.domschmidt.koku.dto.KokuColor;
import de.domschmidt.koku.dto.charts.ChartData;
import de.domschmidt.koku.dto.charts.ChartDataSet;
import de.domschmidt.koku.dto.charts.ChartTypeEnum;
import de.domschmidt.koku.dto.charts.ChartYearMonthFilter;
import de.domschmidt.koku.dto.panels.ChartPanelDto;
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.persistence.model.CustomerAppointmentActivity;
import de.domschmidt.koku.persistence.model.CustomerAppointmentSoldProduct;
import de.domschmidt.koku.service.ICustomerAppointmentService;
import de.domschmidt.koku.service.ICustomerService;
import de.domschmidt.koku.service.searchoptions.CustomerAppointmentSearchOptions;
import de.domschmidt.koku.service.searchoptions.CustomerSearchOptions;
import de.domschmidt.koku.transformer.CustomerToCustomerDtoTransformer;
import de.domschmidt.koku.utils.ActivityPriceUtils;
import de.domschmidt.koku.utils.ProductPriceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;

@RestController
@RequestMapping("/customers")
public class CustomerController extends AbstractController<Customer, CustomerDto, CustomerSearchOptions> {

    private final ICustomerAppointmentService appointmentService;

    @Autowired
    public CustomerController(final ICustomerService customerService,
                              final ICustomerAppointmentService appointmentService) {
        super(customerService, new CustomerToCustomerDtoTransformer());
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<CustomerDto> findAll(final CustomerSearchOptions customerSearchOptions) {
        return super.findAll(customerSearchOptions);
    }

    @GetMapping(value = "/{id}")
    public CustomerDto findByIdTransformed(@PathVariable("id") Long id) {
        return super.findByIdTransformed(id);
    }

    @GetMapping(value = "/{id}/statistics/visits")
    public ChartPanelDto findVisitsById(@PathVariable("id") Long id) {
        final Customer customer = this.service.findById(id);
        final List<CustomerAppointment> allCustomerAppointments = customer.getCustomerAppointments();

        final Map<Integer, List<CustomerAppointment>> visitStatisticPerYear = new TreeMap<>();
        for (final CustomerAppointment customerAppointment : allCustomerAppointments) {
            final int appointmentYear = customerAppointment.getStart().getYear();
            if (visitStatisticPerYear.containsKey(appointmentYear)) {
                final List<CustomerAppointment> oldList = visitStatisticPerYear.get(appointmentYear);
                oldList.add(customerAppointment);
                visitStatisticPerYear.put(appointmentYear, oldList);
            } else {
                final List<CustomerAppointment> newList = new ArrayList<>();
                newList.add(customerAppointment);
                visitStatisticPerYear.put(appointmentYear, newList);
            }
        }

        final List<String> sortedYears = new ArrayList<>();
        final List<BigDecimal> sortedVisitsPerYear = new ArrayList<>();

        for (final Map.Entry<Integer, List<CustomerAppointment>> currentEntry : visitStatisticPerYear.entrySet()) {
            sortedYears.add(String.valueOf(currentEntry.getKey()));
            sortedVisitsPerYear.add(new BigDecimal(currentEntry.getValue().size()));
        }

        return ChartPanelDto.builder()
                .type(ChartTypeEnum.BAR)
                .data(ChartData.builder()
                        .labels(sortedYears)
                        .datasets(Collections.singletonList(
                                ChartDataSet.builder()
                                        .label("Besuche")
                                        .data(sortedVisitsPerYear)
                                        .color(KokuColor.PRIMARY)
                                        .build()
                        ))
                        .build())
                .build();
    }

    @GetMapping(value = "/{id}/statistics/revenue")
    public ChartPanelDto findRevenueById(@PathVariable("id") Long id) {
        final Customer customer = this.service.findById(id);
        final List<CustomerAppointment> allCustomerAppointments = customer.getCustomerAppointments();

        final Map<Integer, CustomerStatistics> revenueStatisticsPerYear = new TreeMap<>();
        for (final CustomerAppointment customerAppointment : allCustomerAppointments) {

            final int appointmentYear = customerAppointment.getStart().getYear();

            BigDecimal activityRevenueSum = BigDecimal.ZERO;
            BigDecimal productRevenueSum = BigDecimal.ZERO;

            for (final CustomerAppointmentActivity activity : customerAppointment.getActivities()) {
                activityRevenueSum = activityRevenueSum.add(ActivityPriceUtils.getActivityPriceForCustomerAppointment(activity, customerAppointment));
            }

            for (final CustomerAppointmentSoldProduct soldProduct : customerAppointment.getSoldProducts()) {
                productRevenueSum = productRevenueSum.add(ProductPriceUtils.getSoldProductPriceForCustomerAppointment(soldProduct, customerAppointment));
            }

            final BigDecimal totalAppointmentRevenueSum = activityRevenueSum.add(productRevenueSum);

            if (revenueStatisticsPerYear.containsKey(appointmentYear)) {
                final CustomerStatistics oldStatistics = revenueStatisticsPerYear.get(appointmentYear);
                revenueStatisticsPerYear.put(
                        appointmentYear,
                        CustomerStatistics.builder()
                                .revenue(oldStatistics.getRevenue().add(totalAppointmentRevenueSum))
                                .productRevenue(oldStatistics.getProductRevenue().add(productRevenueSum))
                                .activityRevenue(oldStatistics.getActivityRevenue().add(activityRevenueSum))
                                .build()
                );
            } else {
                revenueStatisticsPerYear.put(
                        appointmentYear,
                        CustomerStatistics.builder()
                                .revenue(totalAppointmentRevenueSum)
                                .productRevenue(productRevenueSum)
                                .activityRevenue(activityRevenueSum)
                                .build()
                );
            }
        }

        final List<String> sortedYears = new ArrayList<>();
        final List<BigDecimal> sortedRevenue = new ArrayList<>();
        final List<BigDecimal> sortedProductRevenue = new ArrayList<>();
        final List<BigDecimal> sortedActivityRevenue = new ArrayList<>();

        for (final Map.Entry<Integer, CustomerStatistics> currentEntry : revenueStatisticsPerYear.entrySet()) {
            sortedYears.add(String.valueOf(currentEntry.getKey()));
            sortedRevenue.add(currentEntry.getValue().getRevenue());
            sortedProductRevenue.add(currentEntry.getValue().getProductRevenue());
            sortedActivityRevenue.add(currentEntry.getValue().getActivityRevenue());
        }

        return ChartPanelDto.builder()
                .type(ChartTypeEnum.BAR)
                .data(ChartData.builder()
                        .labels(sortedYears)
                        .datasets(Arrays.asList(
                                ChartDataSet.builder()
                                        .label("Gesamtumsatz")
                                        .data(sortedRevenue)
                                        .color(KokuColor.PRIMARY)
                                        .build(),
                                ChartDataSet.builder()
                                        .label("Produktumsatz")
                                        .data(sortedProductRevenue)
                                        .color(KokuColor.SECONDARY)
                                        .build(),
                                ChartDataSet.builder()
                                        .label("Tätigkeitsumsatz")
                                        .data(sortedActivityRevenue)
                                        .color(KokuColor.TERTIARY)
                                        .build()
                        ))
                        .build())
                .build();
    }

    @GetMapping(value = "/statistics/mostvisited")
    public ChartPanelDto findMostVisitingCustomers(
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

        final Map<Customer, Integer> visitStatisticPerCustomer = new HashMap<>();
        for (final CustomerAppointment customerAppointment : allAppointments) {
            final Customer customer = customerAppointment.getCustomer();
            if (visitStatisticPerCustomer.containsKey(customer)) {
                visitStatisticPerCustomer.put(customer, visitStatisticPerCustomer.get(customer) + 1);
            } else {
                visitStatisticPerCustomer.put(customer, 1);
            }
        }

        final List<String> sortedCustomerNames = new ArrayList<>();
        final List<BigDecimal> sortedActivityApplyCount = new ArrayList<>();

        for (final Map.Entry<Customer, Integer> currentEntry : visitStatisticPerCustomer.entrySet()) {
            sortedCustomerNames.add(currentEntry.getKey().getFirstName() + ' ' + currentEntry.getKey().getLastName());
            sortedActivityApplyCount.add(new BigDecimal(currentEntry.getValue()));
        }

        return ChartPanelDto.builder()
                .type(ChartTypeEnum.BAR)
                .data(ChartData.builder()
                        .labels(sortedCustomerNames)
                        .datasets(Collections.singletonList(
                                ChartDataSet.builder()
                                        .label("Besuche")
                                        .data(sortedActivityApplyCount)
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
    public ChartPanelDto findCustomersWithMostRevenue(
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

        final Map<Customer, CustomerStatistics> visitStatisticPerCustomer = new HashMap<>();
        for (final CustomerAppointment customerAppointment : allAppointments) {
            final Customer customer = customerAppointment.getCustomer();

            BigDecimal activityRevenueSum = BigDecimal.ZERO;
            BigDecimal productRevenueSum = BigDecimal.ZERO;

            for (final CustomerAppointmentActivity activity : customerAppointment.getActivities()) {
                activityRevenueSum = activityRevenueSum.add(ActivityPriceUtils.getActivityPriceForCustomerAppointment(activity, customerAppointment));
            }

            for (final CustomerAppointmentSoldProduct soldProduct : customerAppointment.getSoldProducts()) {
                productRevenueSum = productRevenueSum.add(ProductPriceUtils.getSoldProductPriceForCustomerAppointment(soldProduct, customerAppointment));
            }

            final BigDecimal totalAppointmentRevenueSum = activityRevenueSum.add(productRevenueSum);

            if (visitStatisticPerCustomer.containsKey(customer)) {
                final CustomerStatistics oldStatistics = visitStatisticPerCustomer.get(customer);
                visitStatisticPerCustomer.put(
                        customer,
                        CustomerStatistics.builder()
                                .revenue(oldStatistics.getRevenue().add(totalAppointmentRevenueSum))
                                .productRevenue(oldStatistics.getProductRevenue().add(productRevenueSum))
                                .activityRevenue(oldStatistics.getActivityRevenue().add(activityRevenueSum))
                                .build()
                );
            } else {
                visitStatisticPerCustomer.put(
                        customer,
                        CustomerStatistics.builder()
                                .revenue(totalAppointmentRevenueSum)
                                .productRevenue(productRevenueSum)
                                .activityRevenue(activityRevenueSum)
                                .build()
                );
            }
        }

        final List<String> sortedCustomerNames = new ArrayList<>();
        final List<BigDecimal> sortedRevenue = new ArrayList<>();
        final List<BigDecimal> sortedProductRevenue = new ArrayList<>();
        final List<BigDecimal> sortedActivityRevenue = new ArrayList<>();

        for (final Map.Entry<Customer, CustomerStatistics> currentEntry : visitStatisticPerCustomer.entrySet()) {
            sortedCustomerNames.add(currentEntry.getKey().getFirstName() + ' ' + currentEntry.getKey().getLastName());
            sortedRevenue.add(currentEntry.getValue().getRevenue());
            sortedProductRevenue.add(currentEntry.getValue().getProductRevenue());
            sortedActivityRevenue.add(currentEntry.getValue().getActivityRevenue());
        }

        return ChartPanelDto.builder()
                .type(ChartTypeEnum.BAR)
                .data(ChartData.builder()
                        .labels(sortedCustomerNames)
                        .datasets(Arrays.asList(
                                ChartDataSet.builder()
                                        .label("Gesamtumsatz")
                                        .data(sortedRevenue)
                                        .color(KokuColor.PRIMARY)
                                        .build(),
                                ChartDataSet.builder()
                                        .label("Produktumsatz")
                                        .data(sortedProductRevenue)
                                        .color(KokuColor.SECONDARY)
                                        .build(),
                                ChartDataSet.builder()
                                        .label("Tätigkeitsumsatz")
                                        .data(sortedActivityRevenue)
                                        .color(KokuColor.TERTIARY)
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
    public CustomerDto create(@RequestBody CustomerDto newCustomer) {
        return super.create(newCustomer);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") Long id, @RequestBody CustomerDto updatedDto) {
        super.update(id, updatedDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        final Customer customer = super.findById(id);
        customer.setDeleted(true);
        this.service.update(customer);
    }
}
