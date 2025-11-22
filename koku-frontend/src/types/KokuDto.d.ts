/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.36.1070 on 2025-11-22 23:09:40.

declare namespace KokuDto {

    interface AbstractCalendarActionDto {
        "@type": "open-routed-content" | "select-user";
        id?: string;
        title?: string;
        text?: string;
        icon?: string;
        imgBase64?: string;
        color?: CalendarActionColorEnumDto;
        loading?: boolean;
    }

    interface AbstractCalendarCallHttpItemActionParamDto {
        "@type": "item-value";
        param?: string;
    }

    interface AbstractCalendarCallHttpItemActionSuccessEventDto {
        "@type": "propagate-global-event";
    }

    interface AbstractCalendarClickActionDto {
        "@type": "open-content" | "open-routed-content";
    }

    interface AbstractCalendarGlobalEventListenerDto {
        "@type": "flash-once" | "refresh" | "replace-via-payload";
        eventName?: string;
    }

    interface AbstractCalendarHeaderInlineContentGlobalEventListenersDto {
        "@type": "event-payload";
        eventName?: string;
    }

    interface AbstractCalendarInlineContentDto {
        "@type": "dock" | "formular" | "header" | "list";
    }

    interface AbstractCalendarItemClickAction {
        "@type": "call-http" | "open-routed-content";
    }

    interface AbstractCalendarItemInlineFormularContentSaveEventDto {
        "@type": "propagate-global-event";
    }

    interface AbstractCalendarItemMoveAction {
        "@type": "call-http";
    }

    interface AbstractCalendarItemResizeAction {
        "@type": "call-http";
    }

    interface AbstractCalendarListSourceConfigDto {
        "@type": "holiday" | "list";
        id?: string;
        name?: string;
        sourceItemColor?: CalendarListSourceColorEnumDto;
        clickAction?: AbstractCalendarItemClickAction;
        dropAction?: AbstractCalendarItemMoveAction;
        resizeAction?: AbstractCalendarItemResizeAction;
    }

    interface AbstractCalendarOpenRoutedContentItemParamDto {
        "@type": "item-value";
        param?: string;
    }

    interface AbstractCalendarRoutedContentDto {
        "@type": "routed-inline-content";
        route?: string;
        itemId?: string;
        globalEventListeners?: AbstractCalendarRoutedContentGlobalEventListenerDto[];
    }

    interface AbstractCalendarRoutedContentGlobalEventListenerDto {
        "@type": "close";
        eventName?: string;
    }

    interface CalendarCallHttpItemActionItemValueParamDto extends AbstractCalendarCallHttpItemActionParamDto {
        "@type": "item-value";
        valuePath?: string;
    }

    interface CalendarCallHttpItemActionPropagateGlobalEventSuccessEventDto extends AbstractCalendarCallHttpItemActionSuccessEventDto {
        "@type": "propagate-global-event";
        eventName?: string;
    }

    interface CalendarCallHttpItemClickAction extends AbstractCalendarItemClickAction {
        "@type": "call-http";
        startDatePath?: string;
        startTimePath?: string;
        endDatePath?: string;
        endTimePath?: string;
        url?: string;
        method?: CalendarCallHttpItemActionMethodEnum;
        urlParams?: AbstractCalendarCallHttpItemActionParamDto[];
        valueMapping?: { [index: string]: string };
        successEvents?: AbstractCalendarCallHttpItemActionSuccessEventDto[];
    }

    interface CalendarCallHttpItemDropAction extends AbstractCalendarItemMoveAction {
        "@type": "call-http";
        startDatePath?: string;
        startTimePath?: string;
        url?: string;
        method?: CalendarCallHttpItemActionMethodEnum;
        urlParams?: AbstractCalendarCallHttpItemActionParamDto[];
        valueMapping?: { [index: string]: string };
        successEvents?: AbstractCalendarCallHttpItemActionSuccessEventDto[];
    }

    interface CalendarCallHttpItemResizeAction extends AbstractCalendarItemResizeAction {
        "@type": "call-http";
        endDatePath?: string;
        endTimePath?: string;
        url?: string;
        method?: CalendarCallHttpItemActionMethodEnum;
        urlParams?: AbstractCalendarCallHttpItemActionParamDto[];
        valueMapping?: { [index: string]: string };
        successEvents?: AbstractCalendarCallHttpItemActionSuccessEventDto[];
    }

    interface CalendarConfigDto {
        listSources?: AbstractCalendarListSourceConfigDto[];
        calendarActions?: AbstractCalendarActionDto[];
        calendarClickAction?: AbstractCalendarClickActionDto;
        globalEventListeners?: AbstractCalendarGlobalEventListenerDto[];
        routedContents?: AbstractCalendarRoutedContentDto[];
    }

    interface CalendarDockInlineContentDto extends AbstractCalendarInlineContentDto {
        "@type": "dock";
        content?: CalendarInlineDockContentItemDto[];
    }

    interface CalendarEventPayloadHeaderInlineContentGlobalEventListenersDto extends AbstractCalendarHeaderInlineContentGlobalEventListenersDto {
        "@type": "event-payload";
        idPath?: string;
        titleValuePath?: string;
    }

    interface CalendarFlashOnceGlobalEventListenerDto extends AbstractCalendarGlobalEventListenerDto {
        "@type": "flash-once";
        sourceId?: string;
    }

    interface CalendarFormularFieldOverrideDto {
        "@type": string;
        fieldId?: string;
        disable?: boolean;
    }

    interface CalendarFormularInlineContentAfterSavePropagateGlobalEventDto extends AbstractCalendarItemInlineFormularContentSaveEventDto {
        "@type": "propagate-global-event";
        eventName?: string;
    }

    interface CalendarFormularInlineContentDto extends AbstractCalendarInlineContentDto {
        "@type": "formular";
        formularUrl?: string;
        sourceUrl?: string;
        submitUrl?: string;
        submitMethod?: CalendarFormularActionSubmitMethodEnumDto;
        maxWidthInPx?: number;
        onSaveEvents?: AbstractCalendarItemInlineFormularContentSaveEventDto[];
        fieldOverrides?: CalendarFormularFieldOverrideDto[];
        sourceOverrides?: CalendarFormularSourceOverrideDto[];
    }

    interface CalendarFormularSourceOverrideDto {
        sourcePath?: string;
        value?: CalendarFormularContextSourceValueEnumDto;
        offsetValue?: number;
        offsetUnit?: CalendarFormularContextSourceOffsetUnitEnumDto;
    }

    interface CalendarHeaderInlineContentDto extends AbstractCalendarInlineContentDto {
        "@type": "header";
        title?: string;
        sourceUrl?: string;
        titlePath?: string;
        content?: AbstractCalendarInlineContentDto;
        globalEventListeners?: AbstractCalendarHeaderInlineContentGlobalEventListenersDto[];
    }

    interface CalendarHolidaySourceConfigDto extends AbstractCalendarListSourceConfigDto {
        "@type": "holiday";
    }

    interface CalendarInlineDockContentItemDto {
        id?: string;
        title?: string;
        route?: string;
        icon?: string;
        content?: AbstractCalendarInlineContentDto;
    }

    interface CalendarListInlineContentDto extends AbstractCalendarInlineContentDto {
        "@type": "list";
        listUrl?: string;
        sourceUrl?: string;
        maxWidthInPx?: number;
        title?: string;
    }

    interface CalendarListSourceConfigDto extends AbstractCalendarListSourceConfigDto {
        "@type": "list";
        sourceUrl?: string;
        sourceItemText?: string;
        editable?: boolean;
        idPath?: string;
        startDateFieldSelectionPath?: string;
        endDateFieldSelectionPath?: string;
        startTimeFieldSelectionPath?: string;
        endTimeFieldSelectionPath?: string;
        userIdFieldSelectionPath?: string;
        searchOperatorHint?: EnumSearchOperatorHint;
        additionalFieldSelectionPaths?: string[];
        displayTextFieldSelectionPath?: string;
        deletedFieldSelectionPath?: string;
    }

    interface CalendarOpenContentClickActionDto extends AbstractCalendarClickActionDto {
        "@type": "open-content";
        content?: AbstractCalendarInlineContentDto;
        fieldOverrides?: CalendarFormularFieldOverrideDto[];
        sourceOverrides?: CalendarFormularSourceOverrideDto[];
    }

    interface CalendarOpenRoutedContentActionDto extends AbstractCalendarActionDto {
        "@type": "open-routed-content";
        route?: string;
    }

    interface CalendarOpenRoutedContentClickActionDto extends AbstractCalendarClickActionDto {
        "@type": "open-routed-content";
        route?: string;
    }

    interface CalendarOpenRoutedContentItemClickAction extends AbstractCalendarItemClickAction {
        "@type": "open-routed-content";
        route?: string;
        params?: AbstractCalendarOpenRoutedContentItemParamDto[];
    }

    interface CalendarRefreshGlobalEventListenerDto extends AbstractCalendarGlobalEventListenerDto {
        "@type": "refresh";
    }

    interface CalendarReplaceItemViaPayloadGlobalEventListenerDto extends AbstractCalendarGlobalEventListenerDto {
        "@type": "replace-via-payload";
        sourceId?: string;
    }

    interface CalendarRoutedContentCloseGlobalEventListenerDto extends AbstractCalendarRoutedContentGlobalEventListenerDto {
        "@type": "close";
    }

    interface CalendarRoutedContentDto extends AbstractCalendarRoutedContentDto {
        "@type": "routed-inline-content";
        inlineContent?: AbstractCalendarInlineContentDto;
    }

    interface ItemValueCalendarOpenRoutedContentItemParamDto extends AbstractCalendarOpenRoutedContentItemParamDto {
        "@type": "item-value";
        valuePath?: string;
    }

    interface AxesDto {
        x?: AbstractXAxisDtoUnion;
        y?: YAxisDto[];
    }

    interface AbstractXAxisDto {
        "@type": "categorical";
        label?: string;
    }

    interface CategoricalXAxisDto extends AbstractXAxisDto {
        "@type": "categorical";
        categories?: string[];
    }

    interface YAxisDto {
        opposite?: boolean;
        text?: string;
        seriesName?: string[];
    }

    interface AbstractChartFilterDto {
        "@type": "input";
        queryParamName?: string;
    }

    interface AbstractChartDto {
        "@type": "bar" | "line" | "pie";
        title?: string;
        filters?: AbstractChartFilterDto[];
        annotations?: AnnotationsDto;
    }

    interface AnnotationsAxesDto {
        x?: string;
        borderColor?: ColorsEnumDto;
        label?: AnnotationsAxesLabelDto;
    }

    interface AnnotationsAxesLabelDto {
        borderColor?: ColorsEnumDto;
        backgroundColor?: ColorsEnumDto;
        text?: string;
    }

    interface AnnotationsDto {
        xasis?: AnnotationsAxesDto[];
    }

    interface BarChartDto extends AbstractChartDto {
        "@type": "bar";
        axes?: AxesDto;
        series?: NumericSeriesDto[];
        stacked?: boolean;
        showTotals?: boolean;
    }

    interface LineChartDto extends AbstractChartDto {
        "@type": "line";
        axes?: AxesDto;
        series?: NumericSeriesDto[];
    }

    interface PieChartDto extends AbstractChartDto {
        "@type": "pie";
        series?: number[];
        labels?: string[];
    }

    interface NumericSeriesDto {
        name?: string;
        group?: string;
        data?: number[];
    }

    interface DashboardViewDto {
        contentRoot?: IDashboardContent;
    }

    interface IDashboardContent {
        "@type": "grid" | "async-chart" | "appointments" | "async-text" | "text";
        id?: string;
    }

    interface AbstractDashboardContainer extends IDashboardContent {
        "@type": "grid";
    }

    interface AbstractDashboardPanel extends IDashboardContent {
        "@type": "async-chart" | "appointments" | "async-text" | "text";
    }

    interface DashboardAsyncChartPanelDto extends AbstractDashboardPanel {
        "@type": "async-chart";
        chartUrl?: string;
    }

    interface AbstractFormViewGlobalEventListenerDto {
        "@type": "field-update-via-payload";
        eventName?: string;
    }

    interface FormViewDto {
        contentRoot?: IFormularContent;
        businessRules?: KokuBusinessRuleDto[];
        globalEventListeners?: AbstractFormViewGlobalEventListenerDto[];
    }

    interface IFormularContent {
        "@type": "button" | "fieldset" | "grid" | "checkbox" | "document-designer" | "input" | "multi-select" | "multi-select-with-pricing-adjustment" | "picture-upload" | "select" | "stat" | "textarea" | "divider" | "icon";
        id?: string;
    }

    interface AbstractFormButton extends IFormularContent {
        "@type": "button";
        disabled?: boolean;
        buttonType?: EnumButtonType;
        postProcessingActions?: AbstractFormButtonButtonAction[];
    }

    interface AbstractFormButtonButtonAction {
        "@type": "reload" | "open-routed-content";
    }

    interface AbstractOpenRoutedContentFormButtonActionParamDto {
        "@type": "event-payload";
        param?: string;
    }

    interface AbstractOpenRoutedContentFormButtonActionParamDtoImpl extends AbstractOpenRoutedContentFormButtonActionParamDto {
        "@type": "event-payload";
        valuePath?: string;
    }

    interface FormButtonReloadAction extends AbstractFormButtonButtonAction {
        "@type": "reload";
    }

    interface OpenRoutedContentFormButtonActionDto extends AbstractFormButtonButtonAction {
        "@type": "open-routed-content";
        route?: string;
        params?: AbstractOpenRoutedContentFormButtonActionParamDto[];
    }

    interface AbstractFormContainer extends IFormularContent {
        "@type": "fieldset" | "grid";
    }

    interface AbstractFormField<T> extends IFormFieldDefault<T>, IFormularContent {
        "@type": "checkbox" | "document-designer" | "input" | "multi-select" | "multi-select-with-pricing-adjustment" | "picture-upload" | "select" | "stat" | "textarea";
        valuePath?: string;
        required?: boolean;
        readonly?: boolean;
        disabled?: boolean;
        prependOuter?: IFormFieldSlot;
        prependInner?: IFormFieldSlot;
        appendInner?: IFormFieldSlot;
        appendOuter?: IFormFieldSlot;
    }

    interface IFormFieldDefault<T> {
        defaultValue?: T;
    }

    interface IFormFieldSlot {
        "@type": "button";
    }

    interface AbstractFormLayout extends IFormularContent {
        "@type": "divider" | "icon";
    }

    interface KokuBusinessException {
        "@type": "business-exception-with-confirmation-message";
    }

    interface KokuBusinessExceptionButtonDto {
        "@type": "close-button" | "send-to-different-endpoint-button";
        title?: string;
        text?: string;
        icon?: string;
        loading?: boolean;
        disabled?: boolean;
        styles?: KokuBusinessExceptionButtonStyle[];
        size?: KokuBusinessExceptionButtonSizeEnum;
    }

    interface KokuBusinessExceptionCloseButtonDto extends KokuBusinessExceptionButtonDto {
        "@type": "close-button";
    }

    interface KokuBusinessExceptionSendToDifferentEndpointButtonDto extends KokuBusinessExceptionButtonDto {
        "@type": "send-to-different-endpoint-button";
        endpointMethod?: KokuBusinessExceptionSendToDifferentEndpointMethodEnum;
        endpointUrl?: string;
        showLoadingAnimation?: boolean;
        showDisabledState?: boolean;
    }

    interface KokuBusinessExceptionWithConfirmationMessageDto extends KokuBusinessException {
        "@type": "business-exception-with-confirmation-message";
        headline?: string;
        confirmationMessage?: string;
        headerButton?: KokuBusinessExceptionButtonDto;
        closeOnClickOutside?: boolean;
        buttons?: KokuBusinessExceptionButtonDto[];
    }

    interface AbstractKokuBusinessRuleContentDto {
        "@type": "dock" | "formular" | "header";
    }

    interface AbstractKokuBusinessRuleExecutionDto {
        "@type": "call-http-endpoint" | "open-dialog-content";
    }

    interface AbstractKokuBusinessRuleFormularContentSaveEventDto {
        "@type": "propagate-global-event";
    }

    interface AbstractKokuBusinessRuleHeaderContentGlobalEventListenersDto {
        "@type": "event-payload";
        eventName?: string;
    }

    interface AbstractKokuBusinessRuleOpenContentCloseListenerDto {
        "@type": "global-event-listener";
    }

    interface KokuBusinessRuleCallHttpEndpoint extends AbstractKokuBusinessRuleExecutionDto {
        "@type": "call-http-endpoint";
        url?: string;
        method?: KokuBusinessRuleCallHttpEndpointMethodEnum;
    }

    interface KokuBusinessRuleDockContentDto extends AbstractKokuBusinessRuleContentDto {
        "@type": "dock";
        content?: KokuBusinessRuleDockContentItemDto[];
    }

    interface KokuBusinessRuleDockContentItemDto {
        id?: string;
        title?: string;
        route?: string;
        icon?: string;
        content?: AbstractKokuBusinessRuleContentDto;
    }

    interface KokuBusinessRuleDto {
        id?: string;
        references?: KokuBusinessRuleFieldReferenceDto[];
        execution?: AbstractKokuBusinessRuleExecutionDto;
    }

    interface KokuBusinessRuleEventPayloadHeaderContentGlobalEventListenersDto extends AbstractKokuBusinessRuleHeaderContentGlobalEventListenersDto {
        "@type": "event-payload";
        idPath?: string;
        titleValuePath?: string;
    }

    interface KokuBusinessRuleFieldReferenceDto {
        reference?: string;
        requestParam?: string;
        resultValuePath?: string;
        loadingAnimation?: boolean;
        resultUpdateMode?: KokuBusinessRuleFieldReferenceUpdateModeEnum;
        listeners?: KokuBusinessRuleFieldReferenceListenerDto[];
    }

    interface KokuBusinessRuleFieldReferenceListenerDto {
        event?: KokuBusinessRuleFieldReferenceListenerEventEnum;
    }

    interface KokuBusinessRuleFormularContentAfterSavePropagateGlobalEventDto extends AbstractKokuBusinessRuleFormularContentSaveEventDto {
        "@type": "propagate-global-event";
        eventName?: string;
    }

    interface KokuBusinessRuleFormularContentDto extends AbstractKokuBusinessRuleContentDto {
        "@type": "formular";
        formularUrl?: string;
        sourceUrl?: string;
        submitUrl?: string;
        submitMethod?: KokuBusinessRuleFormularActionSubmitMethodEnumDto;
        maxWidthInPx?: number;
        onSaveEvents?: AbstractKokuBusinessRuleFormularContentSaveEventDto[];
        fieldOverrides?: KokuBusinessRuleFormularFieldOverrideDto[];
    }

    interface KokuBusinessRuleFormularFieldOverrideDto {
        "@type": string;
        fieldId?: string;
        disable?: boolean;
    }

    interface KokuBusinessRuleHeaderContentDto extends AbstractKokuBusinessRuleContentDto {
        "@type": "header";
        title?: string;
        sourceUrl?: string;
        titlePath?: string;
        content?: AbstractKokuBusinessRuleContentDto;
        globalEventListeners?: AbstractKokuBusinessRuleHeaderContentGlobalEventListenersDto[];
    }

    interface KokuBusinessRuleOpenContentCloseGlobalEventListenerDto extends AbstractKokuBusinessRuleOpenContentCloseListenerDto {
        "@type": "global-event-listener";
        eventName?: string;
    }

    interface KokuBusinessRuleOpenDialogContentDto extends AbstractKokuBusinessRuleExecutionDto {
        "@type": "open-dialog-content";
        content?: AbstractKokuBusinessRuleContentDto;
        closeEventListeners?: AbstractKokuBusinessRuleOpenContentCloseListenerDto[];
    }

    interface KokuActivityDto {
        id?: number;
        deleted?: boolean;
        version?: number;
        name?: string;
        approximatelyDuration?: string;
        price?: number;
        updated?: string;
        recorded?: string;
    }

    interface KokuActivityStepDto {
        id?: number;
        deleted?: boolean;
        version?: number;
        name?: string;
        updated?: string;
        recorded?: string;
    }

    interface KokuActivityStepSummaryDto {
        id?: number;
        summary?: string;
    }

    interface KokuActivitySummaryDto {
        id?: number;
        summary?: string;
    }

    interface CalendarUserSelectionActionDto extends AbstractCalendarActionDto {
        "@type": "select-user";
    }

    interface InputChartFilterDto extends AbstractChartFilterDto {
        "@type": "input";
        type?: EnumInputChartFilterType;
        label?: string;
        placeholder?: string;
        value?: string;
    }

    interface KokuActivityPriceSummaryRequestDto {
        activities?: KokuCustomerAppointmentActivityDto[];
        promotions?: KokuCustomerAppointmentPromotionDto[];
        date?: string;
        time?: string;
    }

    interface KokuActivitySoldProductPriceSummaryDto {
        priceSum?: string;
    }

    interface KokuActivitySoldProductSummaryRequestDto {
        soldProducts?: KokuCustomerAppointmentSoldProductDto[];
        promotions?: KokuCustomerAppointmentPromotionDto[];
        date?: string;
        time?: string;
    }

    interface KokuCustomerActivityPriceSummaryDto {
        priceSum?: string;
        durationSum?: string;
    }

    interface KokuCustomerAppointmentActivityDto {
        activityId?: number;
        price?: number;
    }

    interface KokuCustomerAppointmentActivityStepTreatmentDto extends KokuCustomerAppointmentTreatmentDto {
        "@type": "activity-step";
        activityStepId?: number;
    }

    interface KokuCustomerAppointmentDto {
        id?: number;
        deleted?: boolean;
        version?: number;
        customerId?: number;
        customerName?: string;
        shortSummaryText?: string;
        longSummaryText?: string;
        date?: string;
        time?: string;
        approximatelyEndDate?: string;
        approximatelyEndTime?: string;
        activities?: KokuCustomerAppointmentActivityDto[];
        treatmentSequence?: KokuCustomerAppointmentTreatmentDtoUnion[];
        soldProducts?: KokuCustomerAppointmentSoldProductDto[];
        promotions?: KokuCustomerAppointmentPromotionDto[];
        activityPriceSummary?: string;
        activityDurationSummary?: string;
        activitySoldProductSummary?: string;
        description?: string;
        additionalInfo?: string;
        userId?: string;
        updated?: string;
        recorded?: string;
    }

    interface KokuCustomerAppointmentProductTreatmentDto extends KokuCustomerAppointmentTreatmentDto {
        "@type": "product";
        productId?: number;
    }

    interface KokuCustomerAppointmentPromotionDto {
        promotionId?: number;
    }

    interface KokuCustomerAppointmentSoldProductDto {
        productId?: number;
        price?: number;
    }

    interface KokuCustomerAppointmentSummaryDto {
        id?: number;
        appointmentSummary?: string;
    }

    interface KokuCustomerAppointmentTreatmentDto {
        "@type": "activity-step" | "product";
    }

    interface KokuCustomerDto {
        id?: number;
        deleted?: boolean;
        version?: number;
        firstName?: string;
        lastName?: string;
        fullName?: string;
        fullNameWithOnFirstNameBasis?: string;
        initials?: string;
        email?: string;
        address?: string;
        postalCode?: string;
        city?: string;
        addressLine2?: string;
        privateTelephoneNo?: string;
        businessTelephoneNo?: string;
        mobileTelephoneNo?: string;
        medicalTolerance?: string;
        additionalInfo?: string;
        birthday?: string;
        onFirstnameBasis?: boolean;
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
        updated?: string;
        recorded?: string;
    }

    interface KokuCustomerSummaryDto {
        id?: number;
        fullName?: string;
    }

    interface DashboardGridContainerDto extends AbstractDashboardContainer {
        "@type": "grid";
        sm?: number;
        md?: number;
        lg?: number;
        xl?: number;
        xl2?: number;
        xl3?: number;
        xl4?: number;
        xl5?: number;
        cols?: number;
        maxWidthInPx?: number;
        content?: IDashboardContent[];
    }

    interface DashboardAppointmentsPanelDto extends AbstractDashboardPanel {
        "@type": "appointments";
        headline?: string;
        emptyMessage?: string;
        start?: string;
        end?: string;
        listSources?: DashboardAppointmentsPanelListSourceDto[];
    }

    interface DashboardAppointmentsPanelListSourceDto {
        sourceUrl?: string;
        idPath?: string;
        startDateFieldSelectionPath?: string;
        endDateFieldSelectionPath?: string;
        startTimeFieldSelectionPath?: string;
        endTimeFieldSelectionPath?: string;
        searchOperatorHint?: EnumSearchOperatorHint;
        textFieldSelectionPath?: string;
        notesTextFieldSelectionPath?: string;
        sourceItemText?: string;
        sourceItemColor?: KokuColorEnum;
        allDay?: boolean;
        userIdFieldSelectionPath?: string;
        deletedFieldSelectionPath?: string;
    }

    interface DashboardAsyncTextPanelDto extends AbstractDashboardPanel {
        "@type": "async-text";
        sourceUrl?: string;
    }

    interface DashboardTextPanelDto extends AbstractDashboardPanel {
        "@type": "text";
        color?: KokuColorEnum;
        topHeadline?: string;
        headline?: string;
        subHeadline?: string;
        progressDetails?: DashboardTextPanelProgressDetailsDto[];
        progress?: number;
    }

    interface DashboardTextPanelProgressDetailsDto {
        headline?: string;
        headlineColor?: KokuColorEnum;
        subHeadline?: string;
    }

    interface KokuDocumentDto {
        id?: number;
        name?: string;
        deleted?: boolean;
        version?: number;
        template?: string;
        ref?: string;
        updated?: string;
        recorded?: string;
    }

    interface KokuFileDto {
        id?: string;
        deleted?: boolean;
        filename?: string;
        mimeType?: string;
        size?: number;
        ref?: KokuFileRefDto;
        refId?: string;
        refName?: string;
        updated?: string;
        recorded?: string;
    }

    interface ButtonDockableSettings {
        title?: string;
        text?: string;
        icon?: string;
        styles?: EnumButtonStyle[];
    }

    interface KokuFormButton extends AbstractFormButton {
        "@type": "button";
        href?: string;
        hrefTarget?: EnumLinkTarget;
        title?: string;
        text?: string;
        icon?: string;
        loading?: boolean;
        dockable?: boolean;
        dockableSettings?: ButtonDockableSettings;
        styles?: EnumButtonStyle[];
    }

    interface FieldsetContainer extends AbstractFormContainer {
        "@type": "fieldset";
        title?: string;
        content?: IFormularContent[];
    }

    interface GridContainer extends AbstractFormContainer {
        "@type": "grid";
        sm?: number;
        md?: number;
        lg?: number;
        xl?: number;
        xl2?: number;
        cols?: number;
        content?: IFormularContent[];
    }

    interface CheckboxFormularField extends AbstractFormField<boolean> {
        "@type": "checkbox";
        label?: string;
        placeholder?: string;
        minLength?: number;
        maxLength?: number;
        regexp?: string;
        defaultValue?: boolean;
    }

    interface DocumentDesignerFormularField extends AbstractFormField<string> {
        "@type": "document-designer";
        defaultValue?: string;
    }

    interface InputFormularField extends AbstractFormField<string> {
        "@type": "input";
        type?: EnumInputFormularFieldType;
        label?: string;
        placeholder?: string;
        minLength?: number;
        maxLength?: number;
        regexp?: string;
        defaultValue?: string;
    }

    interface MultiSelectFormularField extends AbstractFormField<MultiSelectFormularFieldPossibleValue[]> {
        "@type": "multi-select";
        label?: string;
        placeholder?: string;
        possibleValues?: MultiSelectFormularFieldPossibleValue[];
        defaultValue?: MultiSelectFormularFieldPossibleValue[];
        idPathMapping?: string;
        uniqueValues?: boolean;
    }

    interface MultiSelectFormularFieldPossibleValue {
        id?: string;
        valueMapping?: any;
        text?: string;
        disabled?: boolean;
        color?: KokuColorEnum;
        category?: string;
    }

    interface MultiSelectWithPricingAdjustmentFormularField extends AbstractFormField<MultiSelectWithPricingAdjustmentFormularFieldPossibleValue[]> {
        "@type": "multi-select-with-pricing-adjustment";
        label?: string;
        placeholder?: string;
        possibleValues?: MultiSelectWithPricingAdjustmentFormularFieldPossibleValue[];
        defaultValue?: MultiSelectWithPricingAdjustmentFormularFieldPossibleValue[];
        idPathMapping?: string;
        pricePathMapping?: string;
        uniqueValues?: boolean;
    }

    interface MultiSelectWithPricingAdjustmentFormularFieldPossibleValue {
        id?: string;
        text?: string;
        disabled?: boolean;
        defaultPrice?: number;
        color?: KokuColorEnum;
        category?: string;
    }

    interface PictureUploadFormularField extends AbstractFormField<string> {
        "@type": "picture-upload";
        label?: string;
        defaultValue?: string;
    }

    interface SelectFormularButtonFieldSlot {
        buttonType?: EnumButtonType;
        href?: string;
        hrefTarget?: EnumLinkTarget;
        title?: string;
        text?: string;
        icon?: string;
        loading?: boolean;
        disabled?: boolean;
        styles?: EnumButtonStyle[];
    }

    interface SelectFormularField extends AbstractFormField<string> {
        "@type": "select";
        label?: string;
        placeholder?: string;
        defaultValue?: string;
        possibleValues?: SelectFormularFieldPossibleValue[];
    }

    interface SelectFormularFieldPossibleValue {
        id?: string;
        text?: string;
        disabled?: boolean;
        color?: KokuColorEnum;
        category?: string;
    }

    interface KokuFieldSlotButton extends IFormFieldSlot {
        "@type": "button";
        disabled?: boolean;
        buttonType?: EnumButtonType;
        href?: string;
        hrefTarget?: EnumLinkTarget;
        title?: string;
        text?: string;
        icon?: string;
        loading?: boolean;
        dockable?: boolean;
        dockableSettings?: ButtonDockableSettings;
        styles?: EnumButtonStyle[];
    }

    interface StatFormularField extends AbstractFormField<string> {
        "@type": "stat";
        title?: string;
        description?: string;
        defaultValue?: string;
        icon?: string;
    }

    interface TextareaFormularField extends AbstractFormField<string> {
        "@type": "textarea";
        label?: string;
        placeholder?: string;
        minLength?: number;
        maxLength?: number;
        regexp?: string;
        defaultValue?: string;
    }

    interface DividerLayout extends AbstractFormLayout {
        "@type": "divider";
        text?: string;
    }

    interface IconLayout extends AbstractFormLayout {
        "@type": "icon";
        icon?: string;
    }

    interface AbstractConfigMappingAppendListItemDto {
        "@type": "source-path" | "static-value" | "string-conversion" | "string-transformation";
        targetPath?: string;
    }

    interface AbstractConfigMappingDto {
        "@type": "append-list";
    }

    interface AbstractFormViewEventPayloadFieldUpdateValueSource {
        "@type": "source-path" | "static-value";
    }

    interface AbstractFormViewFieldValueMapping {
        "@type": "append-list" | "field-reference";
    }

    interface AbstractStringTransformationPatternParam {
        "@type": "source-path";
    }

    interface ConfigMappingAppendListDto extends AbstractConfigMappingDto {
        "@type": "append-list";
        valueMapping?: AbstractConfigMappingAppendListItemDto[];
    }

    interface FormViewEventPayloadFieldUpdateGlobalEventListenerDto extends AbstractFormViewGlobalEventListenerDto {
        "@type": "field-update-via-payload";
        fieldValueMapping?: { [index: string]: AbstractFormViewFieldValueMapping };
        configMapping?: { [index: string]: FormViewFieldConfigMapping };
    }

    interface FormViewEventPayloadSourcePathFieldUpdateValueSourceDto extends AbstractFormViewEventPayloadFieldUpdateValueSource {
        "@type": "source-path";
        sourcePath?: string;
    }

    interface FormViewEventPayloadStaticValueFieldUpdateValueSourceDto extends AbstractFormViewEventPayloadFieldUpdateValueSource {
        "@type": "static-value";
        value?: string;
    }

    interface FormViewFieldConfigMapping {
        targetConfigPath?: string;
        valueMapping?: AbstractConfigMappingDto;
    }

    interface FormViewFieldReferenceMultiSelectValueMapping extends AbstractFormViewFieldValueMapping {
        "@type": "append-list";
        targetPathMapping?: { [index: string]: AbstractFormViewEventPayloadFieldUpdateValueSource };
    }

    interface FormViewFieldReferenceValueMapping extends AbstractFormViewFieldValueMapping {
        "@type": "field-reference";
        source?: AbstractFormViewEventPayloadFieldUpdateValueSource;
    }

    interface SourcePathConfigMappingAppendListItemDto extends AbstractConfigMappingAppendListItemDto {
        "@type": "source-path";
        sourcePath?: string;
    }

    interface StaticValueConfigMappingAppendListItemDto extends AbstractConfigMappingAppendListItemDto {
        "@type": "static-value";
        value?: any;
    }

    interface StringConversionConfigMappingAppendListItemDto extends AbstractConfigMappingAppendListItemDto {
        "@type": "string-conversion";
        sourcePath?: string;
    }

    interface StringTransformationConfigMappingAppendListItemDto extends AbstractConfigMappingAppendListItemDto {
        "@type": "string-transformation";
        transformPattern?: string;
        transformPatternParameters?: { [index: string]: AbstractStringTransformationPatternParam };
    }

    interface StringTransformationSourcePathPatternParam extends AbstractStringTransformationPatternParam {
        "@type": "source-path";
        sourcePath?: string;
    }

    interface ListViewConditionalItemValueStylingDto extends AbstractListViewGlobalItemStylingDto {
        "@type": "condition";
        compareValuePath?: string;
        expectedValues?: any[];
        positiveStyling?: ListViewItemStylingDto;
        negativeStyling?: ListViewItemStylingDto;
    }

    interface ListViewItemStylingDto {
        opacity?: number;
        lineThrough?: boolean;
    }

    interface KokuProductDto {
        id?: number;
        deleted?: boolean;
        version?: number;
        name?: string;
        manufacturerId?: number;
        manufacturerName?: string;
        price?: number;
        formattedPrice?: string;
        updated?: string;
        recorded?: string;
    }

    interface KokuProductManufacturerDto {
        id?: number;
        deleted?: boolean;
        version?: number;
        name?: string;
        updated?: string;
        recorded?: string;
    }

    interface KokuProductManufacturerSummaryDto {
        id?: number;
        summary?: string;
    }

    interface KokuProductSummaryDto {
        id?: number;
        summary?: string;
    }

    interface KokuPromotionDto {
        id?: number;
        deleted?: boolean;
        version?: number;
        name?: string;
        shortSummary?: string;
        longSummary?: string;
        activityAbsoluteItemSavings?: number;
        activityAbsoluteSavings?: number;
        activityRelativeItemSavings?: number;
        activityRelativeSavings?: number;
        productAbsoluteItemSavings?: number;
        productAbsoluteSavings?: number;
        productRelativeItemSavings?: number;
        productRelativeSavings?: number;
        updated?: string;
        recorded?: string;
    }

    interface KokuPromotionSummaryDto {
        id?: number;
        summary?: string;
    }

    interface KokuUserAppointmentDto {
        id?: number;
        deleted?: boolean;
        version?: number;
        userId?: string;
        userName?: string;
        summary?: string;
        startDate?: string;
        startTime?: string;
        endDate?: string;
        endTime?: string;
        description?: string;
        updated?: string;
        recorded?: string;
    }

    interface KokuUserAppointmentSummaryDto {
        id?: number;
        summary?: string;
    }

    interface KokuUserDto {
        id?: string;
        deleted?: boolean;
        version?: number;
        firstname?: string;
        fullname?: string;
        initials?: string;
        lastname?: string;
        avatarBase64?: string;
        regionId?: number;
        updated?: string;
        recorded?: string;
    }

    interface KokuUserRegionDto {
        id?: number;
        country?: string;
        state?: string;
    }

    interface KokuUserSummaryDto {
        id?: string;
        summary?: string;
    }

    interface ListViewDto {
        itemIdPath?: string;
        fieldFetchPaths?: string[];
        actions?: AbstractListViewActionDto[];
        fields?: ListViewFieldContentDto[];
        routedContents?: AbstractListViewRoutedContentDto[];
        routedItems?: AbstractListViewRoutedItemDto[];
        itemClickAction?: AbstractListViewItemClickActionDto;
        itemActions?: AbstractListViewItemActionDto[];
        itemPreview?: AbstractListViewItemPreviewDto;
        globalEventListeners?: AbstractListViewGlobalEventListenerDto[];
        globalItemStyling?: AbstractListViewGlobalItemStylingDto[];
    }

    interface ListViewReference {
        "@type": "source-path-reference" | "field-reference";
    }

    interface ListViewSourcePathReference extends ListViewReference {
        "@type": "source-path-reference";
        valuePath?: string;
    }

    interface AbstractListViewActionDto {
        "@type": "open-inline-content" | "open-routed-content";
        icon?: string;
        loading?: boolean;
    }

    interface AbstractListViewActionEventDto {
        "@type": "reload" | "event-payload-update" | "notification";
    }

    interface AbstractListViewBarcodeContentDtoCaptureEventDto {
        "@type": "propagate-global-event";
    }

    interface AbstractListViewContentDto {
        "@type": "barcode" | "chart" | "dock" | "document-form" | "file-viewer" | "formular" | "grid" | "header" | "list";
    }

    interface AbstractListViewRoutedContentDto {
        "@type": "routed-inline-content";
        route?: string;
        itemId?: string;
    }

    interface AbstractListViewUserConfirmationParamDto {
        "@type": "date-value" | "value";
        param?: string;
    }

    interface ListViewBarcodeContentDto extends AbstractListViewContentDto {
        "@type": "barcode";
        onCaptureEvents?: AbstractListViewBarcodeContentDtoCaptureEventDto[];
    }

    interface ListViewBarcodeContentDtoAfterCapturePropagateGlobalEventDto extends AbstractListViewBarcodeContentDtoCaptureEventDto {
        "@type": "propagate-global-event";
        eventName?: string;
    }

    interface ListViewOpenInlineContentActionDto extends AbstractListViewActionDto {
        "@type": "open-inline-content";
        inlineContent?: AbstractListViewContentDto;
    }

    interface ListViewOpenRoutedContentActionDto extends AbstractListViewActionDto {
        "@type": "open-routed-content";
        route?: string;
    }

    interface ListViewReloadActionEvent extends AbstractListViewActionEventDto {
        "@type": "reload";
    }

    interface ListViewUserConfirmationDateValueParamDto extends AbstractListViewUserConfirmationParamDto {
        "@type": "date-value";
        valueReference?: ListViewReference;
    }

    interface ListViewUserConfirmationDto {
        headline?: string;
        content?: string;
        params?: AbstractListViewUserConfirmationParamDto[];
    }

    interface ListViewUserConfirmationValueParamDto extends AbstractListViewUserConfirmationParamDto {
        "@type": "value";
        valueReference?: ListViewReference;
    }

    interface AbstractListViewEventPayloadOpenRoutedContentGlobalEventListenerParamDto {
        "@type": "event-payload";
    }

    interface AbstractListViewGlobalEventListenerDto {
        "@type": "item-add" | "item-update-via-event-payload" | "open-routed-content" | "event-payload-search-term";
        eventName?: string;
    }

    interface ListViewEventPayloadAddItemGlobalEventListenerDto extends AbstractListViewGlobalEventListenerDto {
        "@type": "item-add";
        idPath?: string;
        valueMapping?: { [index: string]: ListViewReference };
    }

    interface ListViewEventPayloadItemUpdateGlobalEventListenerDto extends AbstractListViewGlobalEventListenerDto {
        "@type": "item-update-via-event-payload";
        idPath?: string;
        valueMapping?: { [index: string]: ListViewReference };
    }

    interface ListViewEventPayloadOpenRoutedContentGlobalEventListenerDto extends AbstractListViewGlobalEventListenerDto {
        "@type": "open-routed-content";
        route?: string;
        params?: AbstractListViewEventPayloadOpenRoutedContentGlobalEventListenerParamDto[];
    }

    interface ListViewEventPayloadOpenRoutedContentGlobalEventListenerParamDto extends AbstractListViewEventPayloadOpenRoutedContentGlobalEventListenerParamDto {
        "@type": "event-payload";
        param?: string;
        valuePath?: string;
    }

    interface ListViewEventPayloadSearchTermGlobalEventListenerDto extends AbstractListViewGlobalEventListenerDto {
        "@type": "event-payload-search-term";
        valuePath?: string;
    }

    interface AbstractListViewFieldDto<T> extends IListViewFieldDefault<T> {
        "@type": "checkbox" | "input" | "picture-upload" | "textarea";
        id?: string;
    }

    interface IListViewFieldDefault<T> {
        defaultValue?: T;
    }

    interface ListViewFieldContentDto {
        id?: string;
        valuePath?: string;
        fieldDefinition?: AbstractListViewFieldDto<any>;
    }

    interface ListViewFieldReference extends ListViewReference {
        "@type": "field-reference";
        fieldId?: string;
    }

    interface ListViewCheckboxFieldDto extends AbstractListViewFieldDto<boolean> {
        "@type": "checkbox";
        label?: string;
        defaultValue?: boolean;
    }

    interface ListViewInputFieldDto extends AbstractListViewFieldDto<string> {
        "@type": "input";
        type?: ListViewInputFieldTypeEnumDto;
        label?: string;
        defaultValue?: string;
    }

    interface ListViewPictureUploadFieldDto extends AbstractListViewFieldDto<string> {
        "@type": "picture-upload";
        label?: string;
        defaultValue?: string;
    }

    interface ListViewTextareaFieldDto extends AbstractListViewFieldDto<string> {
        "@type": "textarea";
        label?: string;
        defaultValue?: string;
    }

    interface ListViewRoutedContentDto extends AbstractListViewRoutedContentDto {
        "@type": "routed-inline-content";
        inlineContent?: AbstractListViewContentDto;
        modalContent?: AbstractListViewContentDto;
    }

    interface ListViewChartContentDto extends AbstractListViewContentDto {
        "@type": "chart";
        chartUrl?: string;
    }

    interface ListViewDockContentDto extends AbstractListViewContentDto {
        "@type": "dock";
        content?: ListViewItemInlineDockContentItemDto[];
    }

    interface ListViewItemInlineDockContentItemDto {
        id?: string;
        title?: string;
        route?: string;
        icon?: string;
        content?: AbstractListViewContentDto;
    }

    interface AbstractListViewDocumentFormContentDtoSaveEventDto {
        "@type": "open-routed-content" | "propagate-global-event";
    }

    interface AbstractListViewDocumentFormContentOpenRoutedContentSubmitEventParamDto {
        "@type": string;
        param?: string;
    }

    interface AbstractListViewDocumentFormContentSubmitEventDto {
        "@type": "propagate-global-event";
    }

    interface AbstractListViewInlineDocumentFormAfterSaveParamDto {
        "@type": "event-payload";
        param?: string;
    }

    interface ListViewDocumentFormContentAfterSavePropagateGlobalEventDto extends AbstractListViewDocumentFormContentSubmitEventDto {
        "@type": "propagate-global-event";
        eventName?: string;
    }

    interface ListViewDocumentFormContentDto extends AbstractListViewContentDto {
        "@type": "document-form";
        documentUrl?: string;
        submitUrl?: string;
        onSubmitEvents?: AbstractListViewDocumentFormContentSubmitEventDto[];
    }

    interface ListViewDocumentFormOpenRoutedContent {
    }

    interface ListViewDocumentFormOpenRoutedContentSaveEventDto extends AbstractListViewDocumentFormContentDtoSaveEventDto {
        "@type": "open-routed-content";
        route?: string;
        params?: AbstractListViewInlineDocumentFormAfterSaveParamDto[];
    }

    interface ListViewEventPayloadDocumentFormContentOpenRoutedContentSubmitEventParamDto extends AbstractListViewInlineFormularContentOpenRoutedContentParamDto {
        "@type": "event-payload";
        valuePath?: string;
    }

    interface ListViewEventPayloadDocumentFormOpenRoutedContentParamDto extends AbstractListViewInlineDocumentFormAfterSaveParamDto {
        "@type": "event-payload";
        valuePath?: string;
    }

    interface ListViewInlineDocumentFormAfterSavePropagateGlobalEventDto extends AbstractListViewDocumentFormContentDtoSaveEventDto {
        "@type": "propagate-global-event";
        eventName?: string;
    }

    interface AbstractListViewInlineFormularContentOpenRoutedContentParamDto {
        "@type": "event-payload";
        param?: string;
    }

    interface AbstractListViewItemInlineFormularContentSaveEventDto {
        "@type": "propagate-global-event" | "open-routed-inline-formular";
    }

    interface ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto extends AbstractListViewInlineFormularContentOpenRoutedContentParamDto {
        "@type": "event-payload";
        valuePath?: string;
    }

    interface ListViewFileViewerContentDto extends AbstractListViewContentDto {
        "@type": "file-viewer";
        sourceUrl?: string;
        fileUrl?: string;
        mimeTypeSourcePath?: string;
    }

    interface ListViewFormularContentDto extends AbstractListViewContentDto {
        "@type": "formular";
        formularUrl?: string;
        sourceUrl?: string;
        submitUrl?: string;
        submitMethod?: ListViewFormularActionSubmitMethodEnumDto;
        maxWidthInPx?: number;
        onSaveEvents?: AbstractListViewItemInlineFormularContentSaveEventDto[];
        fieldOverrides?: ListViewFormularFieldOverrideDto[];
    }

    interface ListViewFormularFieldOverrideDto {
        "@type": "route-based-override";
        fieldId?: string;
        disable?: boolean;
    }

    interface ListViewInlineFormularContentAfterSavePropagateGlobalEventDto extends AbstractListViewItemInlineFormularContentSaveEventDto {
        "@type": "propagate-global-event";
        eventName?: string;
    }

    interface ListViewOpenRoutedInlineFormularContentSaveEventDto extends AbstractListViewItemInlineFormularContentSaveEventDto {
        "@type": "open-routed-inline-formular";
        route?: string;
        params?: AbstractListViewInlineFormularContentOpenRoutedContentParamDto[];
    }

    interface ListViewRouteBasedFormularFieldOverrideDto extends ListViewFormularFieldOverrideDto {
        "@type": "route-based-override";
        routeParam?: string;
    }

    interface ListViewGridContentDto extends AbstractListViewContentDto {
        "@type": "grid";
        sm?: number;
        md?: number;
        lg?: number;
        xl?: number;
        xl2?: number;
        xl3?: number;
        xl4?: number;
        xl5?: number;
        xl6?: number;
        xl7?: number;
        cols?: number;
        content?: AbstractListViewContentDto[];
    }

    interface AbstractListViewInlineHeaderContentGlobalEventListenersDto {
        "@type": "event-payload";
        eventName?: string;
    }

    interface AbstractListViewInlineHeaderContentParamDto {
        "@type": "item-value";
        param?: string;
    }

    interface ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto extends AbstractListViewInlineHeaderContentGlobalEventListenersDto {
        "@type": "event-payload";
        idPath?: string;
        titleValuePath?: string;
    }

    interface ListViewHeaderContentDto extends AbstractListViewContentDto {
        "@type": "header";
        title?: string;
        sourceUrl?: string;
        titlePath?: string;
        content?: AbstractListViewContentDto;
        globalEventListeners?: AbstractListViewInlineHeaderContentGlobalEventListenersDto[];
    }

    interface ListViewInlineHeaderContentParamDto extends AbstractListViewInlineHeaderContentParamDto {
        "@type": "item-value";
        valuePath?: string;
    }

    interface AbstractListViewListContentContextDto {
        "@type": "endpoint";
        alias?: string;
    }

    interface EndpointListViewListContentContextDto extends AbstractListViewListContentContextDto {
        "@type": "endpoint";
        endpointMethod?: EndpointListViewContextMethodEnum;
        endpointUrl?: string;
    }

    interface ListViewListContentDto extends AbstractListViewContentDto {
        "@type": "list";
        listUrl?: string;
        sourceUrl?: string;
        maxWidthInPx?: number;
        context?: { [index: string]: AbstractListViewListContentContextDto };
    }

    interface AbstractListViewItemClickActionDto {
        "@type": "open-routed-content" | "propagate-global-event" | "open-inline-content";
        icon?: string;
        loading?: boolean;
    }

    interface AbstractListViewRoutedItemDto {
        "@type": "routed-item";
        route?: string;
    }

    interface ListViewRoutedDummyItemDto extends AbstractListViewRoutedItemDto {
        "@type": "routed-item";
        text?: string;
    }

    interface AbstractListViewItemActionDto {
        "@type": "condition" | "http-call" | "open-routed-content" | "open-inline-content";
        icon?: string;
        successEvents?: AbstractListViewActionEventDto[];
        failEvents?: AbstractListViewActionEventDto[];
    }

    interface ListViewConditionalItemValueActionDto extends AbstractListViewItemActionDto {
        "@type": "condition";
        compareValuePath?: string;
        expectedValues?: any[];
        positiveAction?: AbstractListViewItemActionDto;
        negativeAction?: AbstractListViewItemActionDto;
    }

    interface AbstractListViewCallHttpListActionParamDto {
        "@type": "value";
        param?: string;
    }

    interface ListViewCallHttpListItemActionDto extends AbstractListViewItemActionDto {
        "@type": "http-call";
        url?: string;
        params?: AbstractListViewCallHttpListActionParamDto[];
        method?: ListViewCallHttpListItemActionMethodEnumDto;
        userConfirmation?: ListViewUserConfirmationDto;
    }

    interface ListViewCallHttpListValueActionParamDto extends AbstractListViewCallHttpListActionParamDto {
        "@type": "value";
        valueReference?: ListViewReference;
    }

    interface AbstractListViewItemActionOpenRoutedContentActionParamDto {
        "@type": "value";
        param?: string;
    }

    interface AbstractListViewItemClickOpenRoutedContentActionParamDto {
        "@type": "value";
        param?: string;
    }

    interface ListViewItemActionOpenRoutedContentActionDto extends AbstractListViewItemActionDto {
        "@type": "open-routed-content";
        route?: string;
        params?: AbstractListViewItemActionOpenRoutedContentActionParamDto[];
    }

    interface ListViewItemActionOpenRoutedContentActionItemValueParamDto extends AbstractListViewItemActionOpenRoutedContentActionParamDto {
        "@type": "value";
        valueReference?: ListViewReference;
    }

    interface ListViewItemClickOpenRoutedContentActionDto extends AbstractListViewItemClickActionDto {
        "@type": "open-routed-content";
        route?: string;
        params?: AbstractListViewItemClickOpenRoutedContentActionParamDto[];
    }

    interface ListViewItemClickOpenRoutedContentActionItemValueParamDto extends AbstractListViewItemClickOpenRoutedContentActionParamDto {
        "@type": "value";
        valueReference?: ListViewReference;
    }

    interface ListViewItemClickPropagateGlobalEventActionDto extends AbstractListViewItemClickActionDto {
        "@type": "propagate-global-event";
        eventName?: string;
    }

    interface ListViewItemOpenInlineContentClickActionDto extends AbstractListViewItemClickActionDto {
        "@type": "open-inline-content";
        inlineContent?: AbstractListViewContentDto;
        headerTitle?: string;
    }

    interface ListViewOpenInlineContentItemActionDto extends AbstractListViewItemActionDto {
        "@type": "open-inline-content";
        inlineContent?: AbstractListViewContentDto;
        headerTitle?: string;
    }

    interface AbstractListViewItemPreviewDto {
        "@type": "avatar" | "text";
        valuePath?: string;
    }

    interface ListViewItemPreviewAvatarDto extends AbstractListViewItemPreviewDto {
        "@type": "avatar";
    }

    interface ListViewItemPreviewTextDto extends AbstractListViewItemPreviewDto {
        "@type": "text";
    }

    interface AbstractListViewGlobalItemStylingDto {
        "@type": "AbstractListViewGlobalItemStylingDto" | "condition";
    }

    interface AbstractListViewNotificationEventParamDto {
        "@type": "date-value" | "value";
        param?: string;
    }

    interface ListViewEventPayloadUpdateActionEventDto extends AbstractListViewActionEventDto {
        "@type": "event-payload-update";
        idPath?: string;
        valueMapping?: { [index: string]: ListViewReference };
    }

    interface ListViewNotificationEvent extends AbstractListViewActionEventDto {
        "@type": "notification";
        serenity?: ListViewNotificationEventSerenityEnumDto;
        text?: string;
        params?: AbstractListViewNotificationEventParamDto[];
    }

    interface ListViewNotificationEventDateValueParamDto extends AbstractListViewNotificationEventParamDto {
        "@type": "date-value";
        valueReference?: ListViewReference;
    }

    interface ListViewNotificationEventValueParamDto extends AbstractListViewNotificationEventParamDto {
        "@type": "value";
        valueReference?: ListViewReference;
    }

    interface ListFieldQuery {
        predicates?: QueryPredicate[];
        sort?: EnumQuerySort;
        sortRanking?: number;
    }

    interface ListQuery {
        globalSearchTerm?: string;
        fieldSelection?: string[];
        fieldPredicates?: { [index: string]: ListFieldQuery };
        page?: number;
        limit?: number;
    }

    interface QueryPredicate {
        searchExpression?: string;
        searchOperator?: EnumSearchOperator;
        searchOperatorHint?: EnumSearchOperatorHint;
        negate?: boolean;
    }

    interface ListPage {
        fieldSelection?: string[];
        fieldPredicates?: { [index: string]: ListFieldQuery };
        globalSearchTerm?: string;
        results?: ListItem[];
        hasMore?: boolean;
        pageIndex?: number;
        pageSize?: number;
    }

    interface ListItem {
        id?: string;
        values?: { [index: string]: any };
    }

    type CalendarActionColorEnumDto = "RED" | "ORANGE" | "AMBER" | "YELLOW" | "LIME" | "GREEN" | "EMERALD" | "TEAL" | "CYAN" | "SKY" | "BLUE" | "INDIGO" | "VIOLET" | "PURPLE" | "FUCHSIA" | "PINK" | "ROSE" | "SLATE" | "GRAY" | "ZINC" | "NEUTRAL" | "STONE";

    type CalendarCallHttpItemActionMethodEnum = "POST" | "PUT" | "GET" | "DELETE";

    type CalendarDataItemColorEnumDto = "PRIMARY" | "SECONDARY" | "TERTIARY";

    type CalendarFormularActionSubmitMethodEnumDto = "PUT" | "POST";

    type CalendarFormularContextSourceOffsetUnitEnumDto = "SECOND" | "MINUTE" | "HOUR" | "DAY" | "WEEK" | "MONTH" | "YEAR";

    type CalendarFormularContextSourceValueEnumDto = "SELECTION_START_DATE" | "SELECTION_START_TIME" | "SELECTION_START_DATETIME" | "SELECTION_END_DATE" | "SELECTION_END_TIME" | "SELECTION_END_DATETIME";

    type CalendarHolidaySourceConfigCountryEnum = "AD" | "AE" | "AF" | "AG" | "AI" | "AL" | "AM" | "AO" | "AQ" | "AR" | "AS" | "AT" | "AU" | "AW" | "AX" | "AZ" | "BA" | "BB" | "BD" | "BE" | "BF" | "BG" | "BH" | "BI" | "BJ" | "BL" | "BM" | "BN" | "BO" | "BQ" | "BR" | "BS" | "BT" | "BV" | "BW" | "BY" | "BZ" | "CA" | "CC" | "CD" | "CF" | "CG" | "CH" | "CI" | "CK" | "CL" | "CM" | "CN" | "CO" | "CP" | "CR" | "CU" | "CV" | "CW" | "CX" | "CY" | "CZ" | "DE" | "DG" | "DJ" | "DK" | "DM" | "DO" | "DZ" | "EC" | "EE" | "EG" | "EH" | "ER" | "ES" | "ET" | "FI" | "FJ" | "FK" | "FM" | "FO" | "FR" | "GA" | "GB" | "GD" | "GE" | "GF" | "GG" | "GH" | "GI" | "GL" | "GM" | "GN" | "GP" | "GQ" | "GR" | "GS" | "GT" | "GU" | "GW" | "GY" | "HK" | "HM" | "HN" | "HR" | "HT" | "HU" | "IC" | "ID" | "IE" | "IL" | "IM" | "IN" | "IO" | "IQ" | "IR" | "IS" | "IT" | "JE" | "JM" | "JO" | "JP" | "KE" | "KG" | "KH" | "KI" | "KM" | "KN" | "KP" | "KR" | "KW" | "KY" | "KZ" | "LA" | "LB" | "LC" | "LI" | "LK" | "LR" | "LS" | "LT" | "LU" | "LV" | "LY" | "MA" | "MC" | "MD" | "ME" | "MF" | "MG" | "MH" | "MK" | "ML" | "MM" | "MN" | "MO" | "MP" | "MQ" | "MR" | "MS" | "MT" | "MU" | "MV" | "MW" | "MX" | "MY" | "MZ" | "NA" | "NC" | "NE" | "NF" | "NG" | "NI" | "NL" | "NO" | "NP" | "NR" | "NU" | "NZ" | "OM" | "PA" | "PE" | "PF" | "PG" | "PH" | "PK" | "PL" | "PM" | "PN" | "PR" | "PS" | "PT" | "PW" | "PY" | "QA" | "RE" | "RO" | "RS" | "RU" | "RW" | "SA" | "SB" | "SC" | "SD" | "SE" | "SG" | "SH" | "SI" | "SJ" | "SK" | "SL" | "SM" | "SN" | "SO" | "SR" | "SS" | "ST" | "SV" | "SX" | "SY" | "SZ" | "TC" | "TD" | "TF" | "TG" | "TH" | "TJ" | "TK" | "TL" | "TM" | "TN" | "TO" | "TR" | "TT" | "TV" | "TW" | "TZ" | "UA" | "UG" | "UM" | "US" | "UY" | "UZ" | "VA" | "VC" | "VE" | "VG" | "VI" | "VN" | "VU" | "WF" | "WS" | "XK" | "YE" | "YT" | "ZA" | "ZM" | "ZW";

    type CalendarListSourceColorEnumDto = "RED" | "ORANGE" | "AMBER" | "YELLOW" | "LIME" | "GREEN" | "EMERALD" | "TEAL" | "CYAN" | "SKY" | "BLUE" | "INDIGO" | "VIOLET" | "PURPLE" | "FUCHSIA" | "PINK" | "ROSE" | "SLATE" | "GRAY" | "ZINC" | "NEUTRAL" | "STONE";

    type CalendarListSourceConfigReocurringRuleEnumDto = "YEARLY";

    type ColorsEnumDto = "PRIMARY" | "SECONDARY" | "ACCENT" | "INFO" | "SUCCESS" | "WARNING" | "ERROR" | "RED" | "ORANGE" | "AMBER" | "YELLOW" | "LIME" | "GREEN" | "EMERALD" | "TEAL" | "CYAN" | "SKY" | "BLUE" | "INDIGO" | "VIOLET" | "PURPLE" | "FUCHSIA" | "PINK" | "ROSE" | "SLATE" | "GRAY" | "ZINC" | "NEUTRAL" | "STONE";

    type EnumFormViewSubmitMethod = "PUT" | "POST";

    type EnumButtonType = "BUTTON" | "SUBMIT";

    type KokuBusinessExceptionButtonKeyListenerEnum = "ESC";

    type KokuBusinessExceptionButtonSizeEnum = "XS" | "SM" | "MD" | "LG" | "XL";

    type KokuBusinessExceptionButtonStyle = "NEUTRAL" | "PRIMARY" | "SECONDARY" | "ACCENT" | "INFO" | "SUCCESS" | "WARNING" | "ERROR" | "OUTLINE" | "DASH" | "SOFT" | "GHOST" | "LINK" | "ACTIVE" | "DISABLED" | "WIDE" | "BLOCK" | "SQUARE" | "CIRCLE";

    type KokuBusinessExceptionSendToDifferentEndpointMethodEnum = "GET" | "POST" | "PUT" | "DELETE";

    type KokuBusinessRuleCallHttpEndpointMethodEnum = "GET" | "POST" | "PUT" | "DELETE";

    type KokuBusinessRuleFieldReferenceListenerEventEnum = "CHANGE" | "INPUT" | "CLICK" | "BLUR" | "FOCUS" | "CLICK_PREPEND_OUTER" | "BLUR_PREPEND_OUTER" | "FOCUS_PREPEND_OUTER" | "CLICK_PREPEND_INNER" | "BLUR_PREPEND_INNER" | "FOCUS_PREPEND_INNER" | "CLICK_APPEND_INNER" | "BLUR_APPEND_INNER" | "FOCUS_APPEND_INNER" | "CLICK_APPEND_OUTER" | "BLUR_APPEND_OUTER" | "FOCUS_APPEND_OUTER" | "INIT" | "REINIT";

    type KokuBusinessRuleFieldReferenceUpdateModeEnum = "ALWAYS";

    type KokuBusinessRuleFormularActionSubmitMethodEnumDto = "PUT" | "POST";

    type KokuColorEnum = "PRIMARY" | "SECONDARY" | "ACCENT" | "INFO" | "SUCCESS" | "WARNING" | "ERROR" | "RED" | "ORANGE" | "AMBER" | "YELLOW" | "LIME" | "GREEN" | "EMERALD" | "TEAL" | "CYAN" | "SKY" | "BLUE" | "INDIGO" | "VIOLET" | "PURPLE" | "FUCHSIA" | "PINK" | "ROSE" | "SLATE" | "GRAY" | "ZINC" | "NEUTRAL" | "STONE";

    type EnumInputChartFilterType = "TEXT" | "PASSWORD" | "EMAIL" | "NUMBER" | "DATE" | "DATETIME" | "WEEK" | "MONTH" | "TEL" | "URL" | "SEARCH" | "TIME";

    type KokuFileRefDto = "CUSTOMER";

    type EnumButtonSize = "XS" | "SM" | "MD" | "LG" | "XL";

    type EnumButtonStyle = "NEUTRAL" | "PRIMARY" | "SECONDARY" | "ACCENT" | "INFO" | "SUCCESS" | "WARNING" | "ERROR" | "OUTLINE" | "DASH" | "SOFT" | "GHOST" | "LINK" | "ACTIVE" | "DISABLED" | "WIDE" | "BLOCK" | "SQUARE" | "CIRCLE";

    type EnumLinkTarget = "BLANK" | "SELF";

    type EnumInputFormularFieldType = "TEXT" | "PASSWORD" | "EMAIL" | "NUMBER" | "DATE" | "DATETIME" | "WEEK" | "MONTH" | "TEL" | "URL" | "SEARCH" | "TIME";

    type ListViewInputFieldTypeEnumDto = "TEXT" | "PASSWORD" | "EMAIL" | "NUMBER" | "DATE" | "DATETIME" | "WEEK" | "MONTH" | "TEL" | "URL" | "SEARCH" | "TIME";

    type EndpointListViewContextMethodEnum = "GET" | "POST" | "PUT" | "DELETE";

    type ListViewFormularActionSubmitMethodEnumDto = "PUT" | "POST";

    type ListViewCallHttpListItemActionMethodEnumDto = "PUT" | "POST" | "DELETE" | "GET";

    type ListViewNotificationEventSerenityEnumDto = "SUCCESS" | "ERROR";

    type EnumQuerySort = "ASC" | "DESC";

    type EnumSearchOperator = "EQ" | "LESS" | "GREATER" | "GREATER_OR_EQ" | "LESS_OR_EQ" | "LIKE" | "STARTS_WITH" | "ENDS_WITH";

    type EnumSearchOperatorHint = "YEARLY_RECURRING";

    type AbstractXAxisDtoUnion = CategoricalXAxisDto;

    type AbstractChartDtoUnion = BarChartDto | LineChartDto | PieChartDto;

    type KokuCustomerAppointmentTreatmentDtoUnion = KokuCustomerAppointmentActivityStepTreatmentDto | KokuCustomerAppointmentProductTreatmentDto;

}
