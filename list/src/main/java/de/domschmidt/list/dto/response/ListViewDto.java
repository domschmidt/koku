package de.domschmidt.list.dto.response;

import de.domschmidt.list.dto.response.actions.AbstractListViewActionDto;
import de.domschmidt.list.dto.response.actions.AbstractListViewRoutedContentDto;
import de.domschmidt.list.dto.response.events.AbstractListViewGlobalEventListenerDto;
import de.domschmidt.list.dto.response.fields.ListViewFieldContentDto;
import de.domschmidt.list.dto.response.filters.ListViewFilterContentDto;
import de.domschmidt.list.dto.response.items.AbstractListViewItemClickActionDto;
import de.domschmidt.list.dto.response.items.AbstractListViewRoutedItemDto;
import de.domschmidt.list.dto.response.items.actions.AbstractListViewItemActionDto;
import de.domschmidt.list.dto.response.items.preview.AbstractListViewItemPreviewDto;
import de.domschmidt.list.dto.response.items.style.AbstractListViewGlobalItemStylingDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListViewDto {

    String itemIdPath;
    List<String> fieldFetchPaths = new ArrayList<>();
    List<AbstractListViewActionDto> actions = new ArrayList<>();
    List<ListViewFieldContentDto> fields = new ArrayList<>();
    List<ListViewFilterContentDto> filters = new ArrayList<>();
    List<AbstractListViewRoutedContentDto> routedContents = new ArrayList<>();
    List<AbstractListViewRoutedItemDto> routedItems = new ArrayList<>();
    AbstractListViewItemClickActionDto itemClickAction;
    List<AbstractListViewItemActionDto> itemActions = new ArrayList<>();
    AbstractListViewItemPreviewDto itemPreview;
    List<AbstractListViewGlobalEventListenerDto> globalEventListeners = new ArrayList<>();
    List<AbstractListViewGlobalItemStylingDto> globalItemStyling = new ArrayList<>();

}
