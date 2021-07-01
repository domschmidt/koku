/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.32.889 on 2021-06-21 10:33:52.

declare namespace KokuDto {

    interface AppointmentGroupDto {
        date?: string;
        appointments?: CustomerAppointmentDto[];
    }

    interface CalendarLoadSettingsDto {
        start?: string;
        end?: string;
        loadCustomerAppointments?: boolean;
        loadCustomerBirthdays?: boolean;
        loadPrivateAppointments?: boolean;
    }

    interface ICalendarContent {
        "@type": "CustomerAppointment" | "CustomerBirthday" | "PrivateAppointment";
    }

    interface PriceHistoryDto {
        price?: number;
        recorded?: string;
    }

    interface SaleDto {
        id?: number;
        date?: string;
        description?: string;
        price?: number;
    }

    interface UploadDto {
        uuid?: string;
        fileName?: string;
        creationDate?: string;
    }

    interface ActivityDto {
        id?: number;
        description?: string;
        approximatelyDuration?: string;
        currentPrice?: number;
        priceHistory?: PriceHistoryDto[];
    }

    interface ActivitySequenceItemDto {
        "@type": "ActivityStepDto" | "ProductDto";
        sequenceId?: number;
    }

    interface ActivityStepDto extends ActivitySequenceItemDto {
        "@type": "ActivityStepDto";
        id?: number;
        description?: string;
    }

    interface ActivityStepDtoBuilderImpl {
    }

    interface LoginAttemptResponseDto {
        tokenTTL?: number;
        refreshTokenTTL?: number;
    }

    interface LoginDto {
        username?: string;
        password?: string;
    }

    interface ChartData {
        labels?: string[];
        datasets?: ChartDataSet[];
    }

    interface ChartDataSet {
        data?: number[];
        label?: string;
        color?: KokuColor;
    }

    interface ChartFilter {
        "@type": "YearMonth";
        label?: string;
        queryParam?: string;
    }

    interface ChartYearMonthFilter extends ChartFilter {
        "@type": "YearMonth";
        value?: string;
    }

    interface ChartYearMonthFilterBuilderImpl {
    }

    interface CustomerAppointmentActivityDto {
        id?: number;
        activity?: ActivityDto;
        sellPrice?: number;
    }

    interface CustomerAppointmentActivityDtoBuilderImpl {
    }

    interface CustomerAppointmentDto extends ICalendarContent {
        "@type": "CustomerAppointment";
        id?: number;
        startDate?: string;
        startTime?: string;
        approximatelyDuration?: string;
        description?: string;
        additionalInfo?: string;
        approxRevenue?: number;
        customer?: CustomerDto;
        activities?: CustomerAppointmentActivityDto[];
        soldProducts?: CustomerAppointmentSoldProductDto[];
        activitySequenceItems?: ActivitySequenceItemDtoUnion[];
        promotions?: PromotionDto[];
        user?: KokuUserDetailsDto;
    }

    interface CustomerAppointmentSoldProductDto {
        id?: number;
        product?: ProductDto;
        sellPrice?: number;
    }

    interface CustomerAppointmentSoldProductDtoBuilderImpl {
    }

    interface CustomerBirthdayDto extends ICalendarContent {
        "@type": "CustomerBirthday";
        id?: number;
        birthday?: string;
        firstName?: string;
        lastName?: string;
    }

    interface CustomerDto {
        id?: number;
        firstName?: string;
        lastName?: string;
        email?: string;
        initials?: string;
        address?: string;
        postalCode?: string;
        city?: string;
        privateTelephoneNo?: string;
        businessTelephoneNo?: string;
        mobileTelephoneNo?: string;
        medicalTolerance?: string;
        additionalInfo?: string;
        birthday?: string;
        onFirstNameBasis?: boolean;
        hayFever?: boolean;
        plasterAllergy?: boolean;
        cyanoacrylateAllergy?: boolean;
        asthma?: boolean;
        dryEyes?: boolean;
        circulationProblems?: boolean;
        epilepsy?: boolean;
        diabetes?: boolean;
        claustrophobia?: boolean;
        neurodermatitis?: boolean;
        contacts?: boolean;
        glasses?: boolean;
        eyeDisease?: string;
        allergy?: string;
        covid19vaccinated?: boolean;
    }

    interface CustomerSalesDto {
        startDate?: string;
        startTime?: string;
        soldProducts?: ProductDto[];
    }

    interface CheckboxFormularItemDto extends FormularItemDto {
        "@type": "CheckboxFormularItemDto";
        value?: boolean;
        readOnly?: boolean;
        label?: string;
        context?: string;
        fontSize?: FontSizeDto;
    }

    interface CheckboxFormularItemDtoBuilderImpl {
    }

    interface FormularDto {
        id?: number;
        description?: string;
        rows?: FormularRowDto[];
    }

    interface FormularItemDto {
        "@type": "CheckboxFormularItemDto" | "SVGFormularItemDto" | "SignatureFormularItemDto" | "TextFormularItemDto";
        id?: number;
        fieldDefinitionTypeId?: number;
        xs?: number;
        sm?: number;
        md?: number;
        lg?: number;
        xl?: number;
        align?: FormularItemAlign;
    }

    interface FormularReplacementTokenDto {
        tokenName?: string;
        replacementToken?: string;
    }

    interface FormularRowDto {
        id?: number;
        items?: FormularItemDtoUnion[];
    }

    interface SVGFormularItemDto extends FormularItemDto {
        "@type": "SVGFormularItemDto";
        svgContentBase64encoded?: string;
        widthPercentage?: number;
        maxWidthInPx?: number;
    }

    interface SVGFormularItemDtoBuilderImpl {
    }

    interface SignatureFormularItemDto extends FormularItemDto {
        "@type": "SignatureFormularItemDto";
        dataUri?: string;
    }

    interface SignatureFormularItemDtoBuilderImpl {
    }

    interface TextFormularItemDto extends FormularItemDto {
        "@type": "TextFormularItemDto";
        text?: string;
        readOnly?: boolean;
        fontSize?: FontSizeDto;
    }

    interface TextFormularItemDtoBuilderImpl {
    }

    interface ChartPanelDto extends PanelDto {
        type?: ChartTypeEnum;
        data?: ChartData;
        filters?: ChartFilterUnion[];
    }

    interface ChartPanelDtoBuilderImpl {
    }

    interface GaugePanelDto extends PanelDto {
        percentage?: number;
    }

    interface GaugePanelDtoBuilderImpl {
    }

    interface PanelDto {
        title?: string;
    }

    interface TextPanelContent {
        text?: string;
    }

    interface TextPanelContentBuilderImpl {
    }

    interface TextPanelDto extends PanelDto {
        texts?: TextPanelContent[];
    }

    interface TextPanelDtoBuilderImpl {
    }

    interface ProductCategoryDto {
        id?: number;
        description?: string;
    }

    interface ProductChartsDto {
        mostSold?: ChartPanelDto;
        mostUsed?: ChartPanelDto;
    }

    interface ProductDto extends ActivitySequenceItemDto {
        "@type": "ProductDto";
        id?: number;
        manufacturer?: ProductManufacturerDto;
        description?: string;
        categories?: ProductCategoryDto[];
        currentPrice?: number;
        priceHistory?: PriceHistoryDto[];
    }

    interface ProductDtoBuilderImpl {
    }

    interface ProductManufacturerDto {
        id?: number;
        name?: string;
    }

    interface PromotionActivitySettingsDto {
        absoluteSavings?: number;
        relativeSavings?: number;
        absoluteItemSavings?: number;
        relativeItemSavings?: number;
    }

    interface PromotionDto {
        id?: number;
        name?: string;
        startDate?: string;
        endDate?: string;
        productSettings?: PromotionProductSettingsDto;
        activitySettings?: PromotionActivitySettingsDto;
    }

    interface PromotionProductSettingsDto {
        absoluteSavings?: number;
        relativeSavings?: number;
        absoluteItemSavings?: number;
        relativeItemSavings?: number;
    }

    interface StatisticsCurrentMonthInfo {
        name?: string;
        total?: number;
        products?: number;
        activities?: number;
    }

    interface StatisticsDto {
        generated?: string;
        currentMonth?: StatisticsCurrentMonthInfo;
        lastMonthComparison?: StatisticsLastMonthComparisonInfo;
    }

    interface StatisticsLastMonthComparisonInfo {
        percentage?: number;
    }

    interface KokuUserDetailsDto {
        id?: number;
        username?: string;
        firstname?: string;
        lastname?: string;
        avatarBase64?: string;
        password?: string;
    }

    interface PrivateAppointmentDto extends ICalendarContent {
        "@type": "PrivateAppointment";
        id?: number;
        startDate?: string;
        startTime?: string;
        endDate?: string;
        endTime?: string;
        description?: string;
    }

    type KokuColor = "PRIMARY" | "SECONDARY" | "TERTIARY";

    type ChartTypeEnum = "line" | "bar" | "radar" | "doughnut" | "polarArea" | "bubble" | "pie" | "scatter";

    type FontSizeDto = "SMALL" | "MEDIUM" | "LARGE";

    type FormularItemAlign = "LEFT" | "CENTER" | "RIGHT";

    type ICalendarContentUnion = CustomerAppointmentDto | CustomerBirthdayDto | PrivateAppointmentDto;

    type ActivitySequenceItemDtoUnion = ActivityStepDto | ProductDto;

    type ChartFilterUnion = ChartYearMonthFilter;

    type FormularItemDtoUnion = SVGFormularItemDto | SignatureFormularItemDto | TextFormularItemDto | CheckboxFormularItemDto;

}
