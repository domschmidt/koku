package de.domschmidt.koku.activity.controller;

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

class ActivityViewContractTest {

    @Test
    void activityFormExposesCrudControlsAndStableAlias() {
        final FormViewDto view = new ActivityController(null, null, null, null).getFormularView();

        assertThat(view.getAlias()).isEqualTo("activity");
        assertThat(view.getRootId()).isNotBlank();
        assertThat(view.getContents()).hasSizeGreaterThanOrEqualTo(8).containsKey(view.getRootId());
        assertThat(view.getPlacements()).hasSize(view.getContents().size() - 1);
        assertThat(view.getGlobalEventListeners()).isNotEmpty();
    }

    @Test
    void activityListExposesCreateOpenDeleteRestoreAndUpdateContracts() {
        final ListViewDto view = new ActivityController(null, null, null, null).getListView(null);

        assertCrudList(view);
        assertThat(view.getFields()).hasSize(1);
    }

    @Test
    void activityStepFormAndListExposeCompleteCrudContracts() {
        final ActivityStepController controller = new ActivityStepController(null, null, null, null);
        final FormViewDto form = controller.getFormularView();
        final ListViewDto list = controller.getListView();

        assertThat(form.getAlias()).isEqualTo("activity-step");
        assertThat(form.getContents()).hasSizeGreaterThanOrEqualTo(7);
        assertThat(form.getPlacements()).hasSize(form.getContents().size() - 1);
        assertCrudList(list);
    }

    @Test
    void activityAndStepQueriesReturnStableEmptyPages() {
        final EntityManager entityManager = emptyEntityManager();

        assertThat(new ActivityController(entityManager, null, null, null)
                        .findAll(null)
                        .getResults())
                .isEmpty();
        assertThat(new ActivityStepController(entityManager, null, null, null)
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

    private static void assertCrudList(final ListViewDto view) {
        assertThat(view.getItemIdPath()).isEqualTo("id");
        assertThat(view.getActions()).isNotEmpty();
        assertThat(view.getRoutedItems()).isNotEmpty();
        assertThat(view.getRoutedContents()).isNotEmpty();
        assertThat(view.getItemClickAction()).isNotNull();
        assertThat(view.getItemActions()).isNotEmpty();
        assertThat(view.getGlobalEventListeners()).isNotEmpty();
        assertThat(view.getGlobalItemStyling()).isNotEmpty();
    }
}
