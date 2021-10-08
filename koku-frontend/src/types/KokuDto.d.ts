/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.32.889 on 2021-10-04 11:45:20.

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

  interface ChartYearMonthFilterBuilderImpl {
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

  interface NumberTableRowCellBuilderImpl {
  }

  interface StringTableRowCell extends TableRowCell<string> {
    "@type": "StringTableRowCell";
    value?: string;
  }

  interface StringTableRowCellBuilderImpl {
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

  type KokuColor = "PRIMARY" | "SECONDARY" | "TERTIARY" | "TRANSPARENT";

  type ChartTypeEnum = "line" | "bar" | "radar" | "doughnut" | "polarArea" | "bubble" | "pie" | "scatter";

  type DataLabelsTextAlignEnum = "start" | "center" | "end" | "left" | "right";

  type FontSizeDto = "SMALL" | "MEDIUM" | "LARGE";

  type FormularItemAlign = "LEFT" | "CENTER" | "RIGHT";

  type ICalendarContentUnion = CustomerAppointmentDto | CustomerBirthdayDto | PrivateAppointmentDto;

  type ActivitySequenceItemDtoUnion = ActivityStepDto | ProductDto;

  type ChartFilterUnion = ChartYearMonthFilter;

  type IDashboardColumnContentUnion =
    DiagramDashboardColumnContent
    | DeferredDashboardColumnContent
    | TableDashboardColumnContent;

  type TableRowCellUnion<T> = StringTableRowCell | NumberTableRowCell;

  type FormularItemDtoUnion =
    SVGFormularItemDto
    | SignatureFormularItemDto
    | TextFormularItemDto
    | CheckboxFormularItemDto;

}
