package de.domschmidt.koku.controller.activity;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.KokuColor;
import de.domschmidt.koku.dto.activity.ActivityDto;
import de.domschmidt.koku.dto.charts.ChartData;
import de.domschmidt.koku.dto.charts.ChartDataSet;
import de.domschmidt.koku.dto.charts.ChartTypeEnum;
import de.domschmidt.koku.dto.charts.ChartYearMonthFilter;
import de.domschmidt.koku.dto.panels.ChartPanelDto;
import de.domschmidt.koku.persistence.model.Activity;
import de.domschmidt.koku.persistence.model.ActivityPriceHistoryEntry;
import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.persistence.model.CustomerAppointmentActivity;
import de.domschmidt.koku.service.IActivityPriceHistoryEntryService;
import de.domschmidt.koku.service.IActivityService;
import de.domschmidt.koku.service.ICustomerAppointmentService;
import de.domschmidt.koku.service.searchoptions.ActivitySearchOptions;
import de.domschmidt.koku.service.searchoptions.CustomerAppointmentSearchOptions;
import de.domschmidt.koku.transformer.ActivityToActivityDtoTransformer;
import de.domschmidt.koku.utils.ActivityPriceUtils;
import de.domschmidt.koku.utils.NPEGuardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;

@RestController
@RequestMapping("/activities")
public class ActivityController extends AbstractController<Activity, ActivityDto, ActivitySearchOptions> {

    private final ActivityToActivityDtoTransformer transformer;
    private final IActivityPriceHistoryEntryService activityPriceHistoryEntryService;
    private final ICustomerAppointmentService appointmentService;

    @Autowired
    public ActivityController(final IActivityService activityService,
                              final IActivityPriceHistoryEntryService activityPriceHistoryEntryService,
                              final ActivityToActivityDtoTransformer transformer,
                              final ICustomerAppointmentService appointmentService) {
        super(activityService, transformer);
        this.transformer = transformer;
        this.appointmentService = appointmentService;
        this.activityPriceHistoryEntryService = activityPriceHistoryEntryService;
    }

    @GetMapping
    public List<ActivityDto> findAll(final ActivitySearchOptions searchOptions) {
        final List<Activity> models = this.service.findAll(searchOptions);
        return this.transformer.transformToDtoList(models, searchOptions);
    }

    @GetMapping(value = "/{id}")
    public ActivityDto findByIdTransformed(@PathVariable("id") Long id) {
        return super.findByIdTransformed(id);
    }

    @GetMapping(value = "/{id}/statistics/appliance")
    public ChartPanelDto findUsageById(@PathVariable("id") Long id) {
        final Activity activity = this.service.findById(id);
        final List<CustomerAppointmentActivity> usageInCustomerAppointments = activity.getUsageInCustomerAppointments();

        final Map<Integer, Integer> applianceStatisticsPerYear = new TreeMap<>();
        for (final CustomerAppointmentActivity usageInCustomerAppointment : usageInCustomerAppointments) {
            final int appointmentYear = usageInCustomerAppointment.getCustomerAppointment().getStart().getYear();

            if (applianceStatisticsPerYear.containsKey(appointmentYear)) {
                applianceStatisticsPerYear.put(appointmentYear, applianceStatisticsPerYear.get(appointmentYear) + 1);
            } else {
                applianceStatisticsPerYear.put(appointmentYear, 1);
            }

        }

        final List<String> sortedYears = new ArrayList<>();
        final List<BigDecimal> sortedUsage = new ArrayList<>();

        for (final Map.Entry<Integer, Integer> currentEntry : applianceStatisticsPerYear.entrySet()) {
            sortedYears.add(String.valueOf(currentEntry.getKey()));
            sortedUsage.add(new BigDecimal(currentEntry.getValue()));
        }

        return ChartPanelDto.builder()
                .type(ChartTypeEnum.BAR)
                .data(ChartData.builder()
                        .labels(sortedYears)
                        .datasets(Collections.singletonList(
                                ChartDataSet.builder()
                                        .label("Verwendung in Tätigkeiten")
                                        .data(sortedUsage)
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
        final Activity activity = this.service.findById(id);
        final List<CustomerAppointmentActivity> usages = activity.getUsageInCustomerAppointments();

        final Map<Integer, BigDecimal> revenueStatisticsPerYear = new TreeMap<>();
        for (final CustomerAppointmentActivity customerAppointmentUsage : usages) {
            final CustomerAppointment customerAppointment = customerAppointmentUsage.getCustomerAppointment();
            final int appointmentYear = customerAppointment.getStart().getYear();

            final BigDecimal activityPriceAtDate = ActivityPriceUtils.getActivityPriceForCustomerAppointment(
                    customerAppointmentUsage,
                    customerAppointment
            );

            if (revenueStatisticsPerYear.containsKey(appointmentYear)) {
                final BigDecimal oldRevenue = revenueStatisticsPerYear.get(appointmentYear);
                revenueStatisticsPerYear.put(appointmentYear, oldRevenue.add(activityPriceAtDate));
            } else {
                revenueStatisticsPerYear.put(appointmentYear, activityPriceAtDate);
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
                                        .label("Umsatz in Tätigkeiten")
                                        .data(sortedRevenue)
                                        .colors(Arrays.asList(
                                                KokuColor.PRIMARY
                                        ))
                                        .build()
                        ))
                        .build())
                .build();
    }

    @GetMapping(value = "/statistics/mostapplied")
    public ChartPanelDto findMostAppliedActivities(
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

        final Map<Activity, Integer> applyStatisticPerActivity = new HashMap<>();
        for (final CustomerAppointment customerAppointment : allAppointments) {
            for (final CustomerAppointmentActivity customerAppointmentActivity : customerAppointment.getActivities()) {
                if (applyStatisticPerActivity.containsKey(customerAppointmentActivity.getActivity())) {
                    applyStatisticPerActivity.put(customerAppointmentActivity.getActivity(), applyStatisticPerActivity.get(customerAppointmentActivity.getActivity()) + 1);
                } else {
                    applyStatisticPerActivity.put(customerAppointmentActivity.getActivity(), 1);
                }
            }
        }

        final List<String> sortedActivityNames = new ArrayList<>();
        final List<BigDecimal> sortedActivityApplyCount = new ArrayList<>();

        for (final Map.Entry<Activity, Integer> currentEntry : applyStatisticPerActivity.entrySet()) {
            sortedActivityNames.add(currentEntry.getKey().getDescription());
            sortedActivityApplyCount.add(new BigDecimal(currentEntry.getValue()));
        }

        return ChartPanelDto.builder()
                .type(ChartTypeEnum.BAR)
                .data(ChartData.builder()
                        .labels(sortedActivityNames)
                        .datasets(Collections.singletonList(
                                ChartDataSet.builder()
                                        .label("Anwendungen")
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
    public ChartPanelDto findActivitiesWithMostRevenue(
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

        final Map<Activity, BigDecimal> revenueStatisticPerActivity = new HashMap<>();
        for (final CustomerAppointment customerAppointment : allAppointments) {
            for (final CustomerAppointmentActivity customerAppointmentActivity : customerAppointment.getActivities()) {
                final BigDecimal productPriceAtAppointmentTime = ActivityPriceUtils.getActivityPriceForCustomerAppointment(
                        customerAppointmentActivity,
                        customerAppointment
                );

                if (revenueStatisticPerActivity.containsKey(customerAppointmentActivity.getActivity())) {
                    revenueStatisticPerActivity.put(customerAppointmentActivity.getActivity(), revenueStatisticPerActivity.get(customerAppointmentActivity.getActivity()).add(productPriceAtAppointmentTime));
                } else {
                    revenueStatisticPerActivity.put(customerAppointmentActivity.getActivity(), productPriceAtAppointmentTime);
                }
            }
        }

        final List<String> sortedProductNames = new ArrayList<>();
        final List<BigDecimal> sortedProductSellCount = new ArrayList<>();

        for (final Map.Entry<Activity, BigDecimal> currentEntry : revenueStatisticPerActivity.entrySet()) {
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityDto create(@RequestBody ActivityDto newActivity) {
        final Activity model = this.transformer.transformToEntity(newActivity);
        final Activity savedModel = this.service.create(model);
        final ActivityPriceHistoryEntry newPriceEntry = this.transformer.createNewPriceEntry(newActivity, savedModel);
        savedModel.setPriceHistory(Collections.singletonList(newPriceEntry));
        this.activityPriceHistoryEntryService.update(newPriceEntry);
        return this.transformer.transformToDto(savedModel);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") Long id, @RequestBody ActivityDto updatedDto) {
        final Activity model = this.transformer.transformToEntity(updatedDto);
        List<ActivityPriceHistoryEntry> priceHistory = this.service.findById(model.getId()).getPriceHistory();
        if (priceHistory == null) {
            priceHistory = new ArrayList<>();
        }
        this.service.update(model);
        boolean noHistoryYet = priceHistory.isEmpty();
        if (noHistoryYet || NPEGuardUtils.get(priceHistory.get(priceHistory.size() - 1).getPrice()).compareTo(NPEGuardUtils.get(updatedDto.getCurrentPrice())) != 0) {
            final ActivityPriceHistoryEntry newPriceEntry = this.transformer.createNewPriceEntry(updatedDto, model);
            this.activityPriceHistoryEntryService.update(newPriceEntry);
        }
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        final Activity activity = super.findById(id);
        activity.setDeleted(true);
        this.service.update(activity);
    }
}
