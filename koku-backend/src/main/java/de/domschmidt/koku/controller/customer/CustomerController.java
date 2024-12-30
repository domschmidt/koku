package de.domschmidt.koku.controller.customer;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.data.CustomerStatistics;
import de.domschmidt.koku.dto.KokuColor;
import de.domschmidt.koku.dto.charts.ChartData;
import de.domschmidt.koku.dto.charts.ChartDataSet;
import de.domschmidt.koku.dto.charts.ChartTypeEnum;
import de.domschmidt.koku.dto.charts.ChartYearMonthFilter;
import de.domschmidt.koku.dto.customer.CustomerDto;
import de.domschmidt.koku.dto.panels.ChartPanelDto;
import de.domschmidt.koku.kafka.customers.service.CustomerKafkaService;
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
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.Birthday;
import ezvcard.property.StructuredName;
import ezvcard.property.Uid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.util.InMemoryResource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/customers")
@Slf4j
public class CustomerController extends AbstractController<Customer, CustomerDto, CustomerSearchOptions> {

    private final ICustomerAppointmentService appointmentService;
    private final CustomerKafkaService customerKafkaService;

    @Autowired
    public CustomerController(
            final ICustomerService customerService,
            final ICustomerAppointmentService appointmentService,
            final CustomerKafkaService customerKafkaService
    ) {
        super(customerService, new CustomerToCustomerDtoTransformer());
        this.appointmentService = appointmentService;
        this.customerKafkaService = customerKafkaService;
    }

    @GetMapping
    public List<CustomerDto> findAll(final CustomerSearchOptions customerSearchOptions) {
        return super.findAll(customerSearchOptions);
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportAll() {
        final Date exportDate = new Date();
        final List<CustomerDto> allCustomers = super.findAll(CustomerSearchOptions.builder().search("").build());
        final List<VCard> result = new ArrayList<>();

        for (final CustomerDto customer : allCustomers) {
            final VCard vcard = new VCard();

            final StructuredName n = new StructuredName();

            final List<String> nameList = new ArrayList<>();
            final String firstName = customer.getFirstName();
            final String lastName = customer.getLastName();
            if (firstName != null) {
                nameList.add(firstName);
                n.setGiven(firstName);
            }
            if (lastName != null) {
                nameList.add(lastName);
                n.setFamily(lastName);
            }

            vcard.setStructuredName(n);
            vcard.setFormattedName(
                    String.join(" ", nameList)
            );

            if (customer.getBirthday() != null) {
                vcard.setBirthday(new Birthday(Date.from(customer.getBirthday().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())));
            }
            vcard.setProductId("KoKu");

            final String customerPostalCode = customer.getPostalCode();
            final String customerAddress = customer.getAddress();
            final String customerCity = customer.getCity();
            final boolean hasAddress = StringUtils.isNotBlank(customerPostalCode)
                    || StringUtils.isNotBlank(customerAddress)
                    || StringUtils.isNotBlank(customerCity);
            if (hasAddress) {
                final Address address = new Address();
                if (StringUtils.isNotBlank(customerAddress)) {
                    address.setStreetAddress(customerAddress);
                }
                if (StringUtils.isNotBlank(customerPostalCode)) {
                    address.setPostalCode(customerPostalCode);
                }
                if (StringUtils.isNotBlank(customerCity)) {
                    address.setLocality(customerCity);
                }
                address.getTypes().add(AddressType.HOME);
                vcard.addAddress(address);
            }

            final String customerBusinessTelephoneNo = customer.getBusinessTelephoneNo();
            if (StringUtils.isNotBlank(customerBusinessTelephoneNo)) {
                vcard.addTelephoneNumber(customerBusinessTelephoneNo, TelephoneType.WORK);
            }

            final String customerMobileTelephoneNo = customer.getMobileTelephoneNo();
            if (StringUtils.isNotBlank(customerMobileTelephoneNo)) {
                vcard.addTelephoneNumber(customerMobileTelephoneNo, TelephoneType.CELL);
            }

            final String customerPrivateTelephoneNo = customer.getPrivateTelephoneNo();
            if (StringUtils.isNotBlank(customerPrivateTelephoneNo)) {
                vcard.addTelephoneNumber(customerPrivateTelephoneNo, TelephoneType.HOME);
            }

            final String customerEmail = customer.getEmail();
            if (StringUtils.isNotBlank(customerEmail)) {
                vcard.addEmail(customerEmail, EmailType.HOME);
            }

            vcard.setUid(new Uid(customer.getId().toString()));
            vcard.setRevision(exportDate);

            result.add(vcard);
        }

        final String fileContent = Ezvcard.write(result).version(VCardVersion.V2_1).go();

        final HttpHeaders header = new HttpHeaders();
        final String fileName = "koku-contacts.vcf";
        header.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
        );
        // Disable caching
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        return ResponseEntity
                .ok().headers(header)
                .contentLength(fileContent.length())
                .contentType(MediaType.parseMediaType("text/vcard"))
                .body(new InMemoryResource(fileContent));
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
                                        .colors(Arrays.asList(
                                                KokuColor.PRIMARY
                                        ))
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
                                        .colors(Arrays.asList(
                                                KokuColor.PRIMARY
                                        ))
                                        .build(),
                                ChartDataSet.builder()
                                        .label("Produktumsatz")
                                        .data(sortedProductRevenue)
                                        .colors(Arrays.asList(
                                                KokuColor.SECONDARY
                                        ))
                                        .build(),
                                ChartDataSet.builder()
                                        .label("Tätigkeitsumsatz")
                                        .data(sortedActivityRevenue)
                                        .colors(Arrays.asList(
                                                KokuColor.TERTIARY
                                        ))
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
                                        .colors(Arrays.asList(
                                                KokuColor.PRIMARY
                                        ))
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
                                        .colors(Arrays.asList(
                                                KokuColor.PRIMARY
                                        ))
                                        .build(),
                                ChartDataSet.builder()
                                        .label("Produktumsatz")
                                        .data(sortedProductRevenue)
                                        .colors(Arrays.asList(
                                                KokuColor.SECONDARY
                                        ))
                                        .build(),
                                ChartDataSet.builder()
                                        .label("Tätigkeitsumsatz")
                                        .data(sortedActivityRevenue)
                                        .colors(Arrays.asList(
                                                KokuColor.TERTIARY
                                        ))
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
        final CustomerDto newCreatedCustomer = super.create(newCustomer);
        sendCustomerUpdateDelayed(newCreatedCustomer.getId());
        return newCreatedCustomer;
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") Long id, @RequestBody CustomerDto updatedDto) {
        super.update(id, updatedDto);
        sendCustomerUpdateDelayed(id);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        final Customer customer = super.findById(id);
        customer.setDeleted(true);
        this.service.update(customer);
        sendCustomerUpdateDelayed(id);
    }

    @Async
    public void sendCustomerUpdateDelayed(final Long customerId) {
        try {
            final Customer customer = findById(customerId);
            this.customerKafkaService.sendCustomer(this.transformer.transformToDto(customer));
            customer.setKafkaExported(LocalDateTime.now());
        } catch (final Exception e) {
            log.error("Unable to send customer thru kafka", e);
        }
    }
}
