package de.domschmidt.koku.document.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.list.dto.response.ListViewDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DocumentViewContractTest {

    private final DocumentController controller = new DocumentController(null, null, null);

    @Test
    void formExposesTemplateEditingCrudControlsAndSynchronization() {
        final FormViewDto view = controller.getFormularView();

        assertThat(view.getAlias()).isEqualTo("document");
        assertThat(view.getRootId()).isNotBlank();
        assertThat(view.getContents()).hasSizeGreaterThanOrEqualTo(8);
        assertThat(view.getPlacements()).hasSize(view.getContents().size() - 1);
        assertThat(view.getGlobalEventListeners()).isNotEmpty();
    }

    @Test
    void listExposesFilteringCrudAndRoutedDocumentEditing() {
        final ListViewDto view = controller.getListView();

        assertThat(view.getItemIdPath()).isEqualTo("id");
        assertThat(view.getFilters()).isNotEmpty();
        assertThat(view.getActions()).isNotEmpty();
        assertThat(view.getRoutedItems()).isNotEmpty();
        assertThat(view.getRoutedContents()).isNotEmpty();
        assertThat(view.getItemClickAction()).isNotNull();
        assertThat(view.getItemActions()).isNotEmpty();
        assertThat(view.getGlobalEventListeners()).isNotEmpty();
        assertThat(view.getGlobalItemStyling()).isNotEmpty();
    }

    @Test
    void captureListConnectsSelectionToConfiguredSubmitUrl() {
        final ListViewDto view = controller.getCaptureListView("customer", "services/customers/42/documents");

        assertThat(view.getItemIdPath()).isEqualTo("id");
        assertThat(view.getFilters()).isNotEmpty();
        assertThat(view.getRoutedContents()).isNotEmpty();
        assertThat(view.getItemClickAction()).isNotNull();
    }

    @Test
    void documentQueryReturnsStableEmptyPage() {
        assertThat(new DocumentController(null, null, emptyEntityManager())
                        .findAll(null)
                        .getResults())
                .isEmpty();
    }

    private static EntityManager emptyEntityManager() {
        final EntityManager entityManager = mock(EntityManager.class);
        final EntityManagerFactory factory = mock(EntityManagerFactory.class);
        final Query query = mock(Query.class, RETURNS_SELF);
        when(entityManager.getEntityManagerFactory()).thenReturn(factory);
        when(factory.getProperties()).thenReturn(Map.of());
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());
        when(query.getSingleResult()).thenReturn(0L);
        return entityManager;
    }
}
