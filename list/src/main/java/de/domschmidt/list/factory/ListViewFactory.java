package de.domschmidt.list.factory;

import de.domschmidt.list.dto.response.ListViewDto;
import de.domschmidt.list.dto.response.ListViewSourcePathReference;
import de.domschmidt.list.dto.response.actions.AbstractListViewActionDto;
import de.domschmidt.list.dto.response.actions.AbstractListViewRoutedContentDto;
import de.domschmidt.list.dto.response.events.AbstractListViewGlobalEventListenerDto;
import de.domschmidt.list.dto.response.fields.AbstractListViewFieldDto;
import de.domschmidt.list.dto.response.fields.ListViewFieldContentDto;
import de.domschmidt.list.dto.response.fields.ListViewFieldReference;
import de.domschmidt.list.dto.response.items.AbstractListViewItemClickActionDto;
import de.domschmidt.list.dto.response.items.AbstractListViewRoutedItemDto;
import de.domschmidt.list.dto.response.items.actions.AbstractListViewItemActionDto;
import de.domschmidt.list.dto.response.items.preview.AbstractListViewItemPreviewDto;
import de.domschmidt.list.dto.response.items.style.AbstractListViewGlobalItemStylingDto;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ListViewFactory {

    private final IListViewContentIdGenerator idGenerator;
    private final String itemIdPath;
    private final List<String> fieldFetchPaths = new ArrayList<>();
    private final List<AbstractListViewRoutedContentDto> routedContents = new ArrayList<>();
    private final List<AbstractListViewRoutedItemDto> routedItems = new ArrayList<>();
    private final List<AbstractListViewItemActionDto> itemActions = new ArrayList<>();
    private final List<ListViewFieldContentDto> fields = new ArrayList<>();
    private final List<AbstractListViewActionDto> actions = new ArrayList<>();
    private final List<AbstractListViewGlobalEventListenerDto> globalEventListeners = new ArrayList<>();
    private final List<AbstractListViewGlobalItemStylingDto> globalItemStyling = new ArrayList<>();

    @Setter
    private AbstractListViewItemClickActionDto itemClickAction;
    @Setter
    private AbstractListViewItemPreviewDto itemPreview;

    public ListViewFactory(
            final IListViewContentIdGenerator idGenerator,
            final String itemIdPath
    ) {
        this.itemIdPath = itemIdPath;
        this.idGenerator = idGenerator;
    }

    public void addAction(
            final AbstractListViewActionDto action
    ) {
        this.actions.add(action);
    }

    public void addRoutedItem(
            final AbstractListViewRoutedItemDto item
    ) {
        this.routedItems.add(item);
    }

    public void addRoutedContent(
            final AbstractListViewRoutedContentDto routedContent
    ) {
        this.routedContents.add(routedContent);
    }

    public void addItemAction(
            final AbstractListViewItemActionDto itemAction
    ) {
        this.itemActions.add(itemAction);
    }

    public void addGlobalItemStyling(
            final AbstractListViewGlobalItemStylingDto itemStyling
    ) {
        this.globalItemStyling.add(itemStyling);
    }

    public ListViewFieldReference addField(
            final String valuePath,
            final AbstractListViewFieldDto<?> fieldDefinition
    ) {
        final String uniqueFieldId = this.idGenerator.generateUniqueId(valuePath, "field");
        this.fields.add(ListViewFieldContentDto.builder()
                .id(uniqueFieldId)
                .valuePath(valuePath)
                .fieldDefinition(fieldDefinition)
                .build()
        );
        this.fieldFetchPaths.add(valuePath);
        return new ListViewFieldReference(uniqueFieldId);
    }

    public ListViewSourcePathReference addSourcePath(
            final String valuePath
    ) {
        final String uniqueFieldId = this.idGenerator.generateUniqueId(valuePath, "source");
        this.fieldFetchPaths.add(valuePath);
        return new ListViewSourcePathReference(uniqueFieldId);
    }

    public void addGlobalEventListener(
            final AbstractListViewGlobalEventListenerDto listener
    ) {
        this.globalEventListeners.add(listener);
    }

    public ListViewDto create() {
        return new ListViewDto(
                this.itemIdPath,
                this.fieldFetchPaths,
                this.actions,
                this.fields,
                this.routedContents,
                this.routedItems,
                this.itemClickAction,
                this.itemActions,
                this.itemPreview,
                this.globalEventListeners,
                this.globalItemStyling
        );
    }

}
