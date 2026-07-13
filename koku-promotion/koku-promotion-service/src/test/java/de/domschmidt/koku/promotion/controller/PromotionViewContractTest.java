package de.domschmidt.koku.promotion.controller;

import static org.assertj.core.api.Assertions.assertThat;
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

class PromotionViewContractTest {

    @Test
    void formExposesStableAliasAndCrudControls() {
        final FormViewDto view = new PromotionController(null, null, null, null).getFormularView();

        assertThat(view.getAlias()).isEqualTo("promotion");
        assertThat(view.getRootId()).isNotBlank();
        assertThat(view.getContents()).hasSizeGreaterThanOrEqualTo(8);
        assertThat(view.getPlacements()).hasSize(view.getContents().size() - 1);
        assertThat(view.getGlobalEventListeners()).isNotEmpty();
    }

    @Test
    void listExposesCreateOpenAndSynchronizationContracts() {
        final ListViewDto view = new PromotionController(null, null, null, null).getListView();

        assertThat(view.getItemIdPath()).isEqualTo("id");
        assertThat(view.getActions()).isNotEmpty();
        assertThat(view.getRoutedItems()).isNotEmpty();
        assertThat(view.getRoutedContents()).isNotEmpty();
        assertThat(view.getItemClickAction()).isNotNull();
        assertThat(view.getGlobalEventListeners()).isNotEmpty();
    }

    @Test
    void queryProducesAnEmptyPage() {
        final EntityManager entityManager = mock(EntityManager.class);
        final EntityManagerFactory factory = mock(EntityManagerFactory.class);
        final Query query = mock(Query.class, RETURNS_SELF);
        when(entityManager.getEntityManagerFactory()).thenReturn(factory);
        when(factory.getProperties()).thenReturn(Map.of());
        when(entityManager.createQuery(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        assertThat(new PromotionController(entityManager, null, null, null)
                        .findAll(null)
                        .getResults())
                .isEmpty();
    }
}
