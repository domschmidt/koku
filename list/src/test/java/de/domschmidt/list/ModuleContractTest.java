package de.domschmidt.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import de.domschmidt.list.dto.response.actions.AbstractListViewActionDto;
import de.domschmidt.list.dto.response.actions.AbstractListViewRoutedContentDto;
import de.domschmidt.list.dto.response.events.AbstractListViewGlobalEventListenerDto;
import de.domschmidt.list.dto.response.fields.AbstractListViewFieldDto;
import de.domschmidt.list.dto.response.filters.AbstractListViewFilterDto;
import de.domschmidt.list.dto.response.items.AbstractListViewItemClickActionDto;
import de.domschmidt.list.dto.response.items.AbstractListViewRoutedItemDto;
import de.domschmidt.list.dto.response.items.actions.AbstractListViewItemActionDto;
import de.domschmidt.list.dto.response.items.preview.AbstractListViewItemPreviewDto;
import de.domschmidt.list.dto.response.items.style.AbstractListViewGlobalItemStylingDto;
import de.domschmidt.list.factory.DefaultListViewContentIdGenerator;
import de.domschmidt.list.factory.ListViewFactory;
import org.junit.jupiter.api.Test;

class ModuleContractTest {
    @Test
    void enumContractsExposeConstants() {
        assertEquals(
                4,
                de.domschmidt.list.dto.response.items.actions.call_http.ListViewCallHttpListItemActionMethodEnumDto
                        .values()
                        .length);
        assertEquals(
                4,
                de.domschmidt.list.dto.response.inline_content.list.EndpointListViewContextMethodEnum.values().length);
        assertEquals(
                2,
                de.domschmidt.list.dto.response.notifications.ListViewNotificationEventSerenityEnumDto.values().length);
        assertEquals(
                2,
                de.domschmidt.list.dto.response.items.actions.ListViewFormularActionSubmitMethodEnumDto.values()
                        .length);
    }

    @Test
    void factoryCollectsCompleteListViewContract() {
        final ListViewFactory factory = new ListViewFactory(new DefaultListViewContentIdGenerator(), "id");
        final AbstractListViewActionDto action = mock(AbstractListViewActionDto.class);
        final AbstractListViewRoutedContentDto routedContent = mock(AbstractListViewRoutedContentDto.class);
        final AbstractListViewRoutedItemDto routedItem = mock(AbstractListViewRoutedItemDto.class);
        final AbstractListViewItemActionDto itemAction = mock(AbstractListViewItemActionDto.class);
        final AbstractListViewGlobalItemStylingDto styling = mock(AbstractListViewGlobalItemStylingDto.class);
        final AbstractListViewFieldDto<?> field = mock(AbstractListViewFieldDto.class);
        final AbstractListViewFilterDto filter = mock(AbstractListViewFilterDto.class);
        final AbstractListViewGlobalEventListenerDto listener = mock(AbstractListViewGlobalEventListenerDto.class);
        final AbstractListViewItemClickActionDto clickAction = mock(AbstractListViewItemClickActionDto.class);
        final AbstractListViewItemPreviewDto preview = mock(AbstractListViewItemPreviewDto.class);

        factory.addAction(action);
        factory.addRoutedContent(routedContent);
        factory.addRoutedItem(routedItem);
        factory.addItemAction(itemAction);
        factory.addGlobalItemStyling(styling);
        final var fieldReference = factory.addField("name", field);
        factory.addFilter("active", filter);
        final var sourceReference = factory.addSourcePath("details");
        factory.addGlobalEventListener(listener);
        factory.setItemClickAction(clickAction);
        factory.setItemPreview(preview);
        final var view = factory.create();

        assertEquals("id", view.getItemIdPath());
        assertEquals(java.util.List.of("name", "active", "details"), view.getFieldFetchPaths());
        assertEquals("name", fieldReference.getFieldId());
        assertEquals("details", sourceReference.getValuePath());
        assertSame(action, view.getActions().getFirst());
        assertSame(routedContent, view.getRoutedContents().getFirst());
        assertSame(routedItem, view.getRoutedItems().getFirst());
        assertSame(itemAction, view.getItemActions().getFirst());
        assertSame(styling, view.getGlobalItemStyling().getFirst());
        assertSame(listener, view.getGlobalEventListeners().getFirst());
        assertSame(clickAction, view.getItemClickAction());
        assertSame(preview, view.getItemPreview());
    }

    @Test
    void idGeneratorRejectsDuplicates() {
        final DefaultListViewContentIdGenerator generator = new DefaultListViewContentIdGenerator();

        assertEquals("field", generator.generateUniqueId("field"));
        assertThrows(IllegalArgumentException.class, () -> generator.generateUniqueId("field"));
    }
}
