/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.36.1070 on 2022-09-27 20:16:52.

declare namespace KokuDto {

    interface DataTableColumnDto<T, S> {
        id: string;
        name: string;
        type: string;
        isKey?: boolean;
        canSort?: boolean;
        canFilter?: boolean;
        defaultSortDir?: DataQueryColumnSortDirDto;
        defaultSortIdx?: number;
        hidden?: boolean;
        footerSummary?: T;
        defaultSearchValue?: T;
        typeSpecificSettings?: S;
    }

    interface DataTableDto {
        columns?: DataTableColumnDto<any, any>[];
        rows?: { [index: string]: any }[];
        tableName?: string;
        pageSize?: number;
        page?: number;
        total?: number;
        totalPages?: number;
    }

    interface DataQueryAdvancedSearchDto {
        search?: any;
        customOp?: DataQueryColumnOPDto;
    }

    interface DataQueryColumnSpecDto {
        search?: any;
        advancedSearchSpec?: DataQueryAdvancedSearchDto[];
        selectValues?: any[];
        sortDir?: DataQueryColumnSortDirDto;
        sortIdx?: number;
    }

    interface DataQuerySpecDto {
        page?: number;
        total?: number;
        columnSpecByColumnId?: { [index: string]: DataQueryColumnSpecDto };
        globalSearch?: string;
    }

    interface AlphaNumericSettingsDto {
        maxCharacterLength?: number;
        minCharacterLength?: number;
    }

    interface NumberSettingsDto {
        min?: number;
        max?: number;
        integralDigits?: number;
        fractionalDigits?: number;
    }

    interface SelectSettingsDto {
        userPresentableValues?: { [index: string]: string };
    }

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

    interface LoginAttemptResponseDto {
        tokenTTL?: number;
        refreshTokenTTL?: number;
    }

    interface LoginDto {
        username?: string;
        password?: string;
    }

    interface CardDavInfoDto {
        endpointUrl?: string;
        user?: string;
        password?: string;
    }

    interface ChartData {
        labels?: string[];
        datasets?: ChartDataSet[];
    }

    interface ChartDataLabelsConfig {
        color?: string;
        textAlign?: DataLabelsTextAlignEnum;
        formatter?: string;
    }

    interface ChartDataSet {
        data?: number[];
        segmentedData?: SegmentedData[];
        fill?: boolean;
        backgroundColor?: string[];
        label?: string;
        colors?: KokuColor[];
        datalabels?: ChartDataLabelsConfig;
    }

    interface ChartElementsOptions {
        point?: ChartElementsPointOptions;
    }

    interface ChartElementsPointOptions {
        radius?: number;
        hoverRadius?: number;
    }

    interface ChartFilter {
        "@type": "YearMonth";
        label?: string;
        queryParam?: string;
    }

    interface ChartOptions {
        scales?: ChartScalesOptions;
        elements?: ChartElementsOptions;
        plugins?: ChartPluginOptions;
    }

    interface ChartPluginLegendOptions {
        display?: boolean;
    }

    interface ChartPluginOptions {
        legend?: ChartPluginLegendOptions;
        tooltip?: ChartPluginTooltipOptions;
    }

    interface ChartPluginTooltipOptions {
        enabled?: boolean;
    }

    interface ChartScaleConfig {
        display?: boolean;
    }

    interface ChartScalesOptions {
        y?: ChartScaleConfig;
        x?: ChartScaleConfig;
    }

    interface ChartTextOverlay {
        text?: string;
        subline?: string;
        subsubline?: string;
    }

    interface ChartYearMonthFilter extends ChartFilter {
        "@type": "YearMonth";
        value?: string;
    }

    interface SegmentedData {
        backgroundColor?: KokuColor;
        borderColor?: KokuColor;
        borderDashed?: boolean;
    }

    interface CustomerAppointmentActivityDto {
        id?: number;
        activity?: ActivityDto;
        sellPrice?: number;
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
        covid19boostered?: boolean;
    }

    interface CustomerSalesDto {
        startDate?: string;
        startTime?: string;
        soldProducts?: ProductDto[];
    }

    interface DashboardColumnConfigDto {
        xsWidthPercentage?: number;
        smWidthPercentage?: number;
        mdWidthPercentage?: number;
        lgWidthPercentage?: number;
        xlWidthPercentage?: number;
        contents?: IDashboardColumnContentUnion[];
        label?: string;
    }

    interface DashboardConfigDto {
        columns?: DashboardColumnConfigDto[];
    }

    interface DeferredDashboardColumnContent extends IDashboardColumnContent {
        "@type": "DeferredDashboardColumnContent";
        href?: string;
    }

    interface DiagramDashboardColumnContent extends IDashboardColumnContent {
        "@type": "DiagramDashboardColumnContent";
        type?: ChartTypeEnum;
        data?: ChartData;
        options?: ChartOptions;
        overlay?: ChartTextOverlay;
        label?: string;
    }

    interface IDashboardColumnContent {
        "@type": "DeferredDashboardColumnContent" | "DiagramDashboardColumnContent" | "TableDashboardColumnContent";
    }

    interface NumberTableRowCell extends TableRowCell<number> {
        "@type": "NumberTableRowCell";
        value?: number;
    }

    interface StringTableRowCell extends TableRowCell<string> {
        "@type": "StringTableRowCell";
        value?: string;
    }

    interface TableColumn {
        label?: string;
    }

    interface TableDashboardColumnContent extends IDashboardColumnContent {
        "@type": "TableDashboardColumnContent";
        label?: string;
        columns?: TableColumn[];
        rows?: TableRow[];
    }

    interface TableRow {
        cells?: TableRowCellUnion<any>[];
    }

    interface TableRowCell<T> {
        "@type": "NumberTableRowCell" | "StringTableRowCell";
        value?: T;
    }

    interface CheckboxFormularItemDto extends FormularItemDto {
        "@type": "CheckboxFormularItemDto";
        value?: boolean;
        readOnly?: boolean;
        label?: string;
        context?: string;
        fontSize?: number;
    }

    interface DateFormularItemDto extends FormularItemDto {
        "@type": "DateFormularItemDto";
        value?: string;
        context?: string;
        fontSize?: number;
        readOnly?: boolean;
        dayDiff?: number;
        monthDiff?: number;
        yearDiff?: number;
    }

    interface FormularDto {
        id?: number;
        description?: string;
        tags?: { [index: string]: string };
        rows?: FormularRowDto[];
    }

    interface FormularItemDto {
        "@type": "CheckboxFormularItemDto" | "DateFormularItemDto" | "QrCodeFormularItemDto" | "SVGFormularItemDto" | "SignatureFormularItemDto" | "TextFormularItemDto";
        id: number;
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
        align?: FormularRowAlignDto;
    }

    interface QrCodeFormularItemDto extends FormularItemDto {
        "@type": "QrCodeFormularItemDto";
        value?: string;
    }

    interface SVGFormularItemDto extends FormularItemDto {
        "@type": "SVGFormularItemDto";
        svgContentBase64encoded?: string;
        widthPercentage?: number;
        maxWidthInPx?: number;
        label?: string;
    }

    interface SignatureFormularItemDto extends FormularItemDto {
        "@type": "SignatureFormularItemDto";
        dataUri?: string;
    }

    interface TextFormularItemDto extends FormularItemDto {
        "@type": "TextFormularItemDto";
        text?: string;
        readOnly?: boolean;
        fontSize?: number;
    }

    interface ChartPanelDto extends PanelDto {
        type?: ChartTypeEnum;
        data?: ChartData;
        filters?: ChartFilterUnion[];
    }

    interface GaugePanelDto extends PanelDto {
        percentage?: number;
    }

    interface PanelDto {
        title?: string;
    }

    interface TextPanelContent {
        text?: string;
    }

    interface TextPanelDto extends PanelDto {
        texts?: TextPanelContent[];
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

    type DataQueryColumnOPDto = "EQ" | "LT" | "LOE" | "GT" | "GOE" | "LIKE" | "SW" | "EW";

    type DataQueryColumnSortDirDto = "ASC" | "DESC";

    type KokuColor = "PRIMARY" | "SECONDARY" | "TERTIARY" | "TRANSPARENT";

    type ChartTypeEnum = "line" | "bar" | "radar" | "doughnut" | "polarArea" | "bubble" | "pie" | "scatter";

    type DataLabelsTextAlignEnum = "start" | "center" | "end" | "left" | "right";

    type FormularItemAlign = "LEFT" | "CENTER" | "RIGHT";

    type FormularRowAlignDto = "TOP" | "CENTER" | "BOTTOM";

    type ICalendarContentUnion = CustomerAppointmentDto | CustomerBirthdayDto | PrivateAppointmentDto;

    type ActivitySequenceItemDtoUnion = ActivityStepDto | ProductDto;

    type ChartFilterUnion = ChartYearMonthFilter;

    type IDashboardColumnContentUnion = DiagramDashboardColumnContent | DeferredDashboardColumnContent | TableDashboardColumnContent;

    type TableRowCellUnion<T> = StringTableRowCell | NumberTableRowCell;

    type FormularItemDtoUnion = SVGFormularItemDto | SignatureFormularItemDto | TextFormularItemDto | CheckboxFormularItemDto | QrCodeFormularItemDto | DateFormularItemDto;

}
