package de.domschmidt.koku.product.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.koku.product.persistence.ProductManufacturer;
import de.domschmidt.list.dto.response.ListViewDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProductViewContractTest {

    @Test
    void manufacturerFormAndListExposeCompleteCrudContracts() {
        final ProductManufacturersController controller = new ProductManufacturersController(null, null, null, null);
        final FormViewDto form = controller.getFormularView();
        final ListViewDto list = controller.getListView();

        assertThat(form.getAlias()).isEqualTo("product-manufacturer");
        assertThat(form.getContents()).hasSizeGreaterThanOrEqualTo(7);
        assertThat(form.getPlacements()).hasSize(form.getContents().size() - 1);
        assertCrudList(list);
    }

    @Test
    void productListExposesCompleteCrudAndSynchronizationContracts() {
        final ListViewDto list = new ProductController(null, null, null, null).getListView();

        assertCrudList(list);
        assertThat(list.getFields()).hasSize(3);
        assertThat(list.getFieldFetchPaths()).contains("id");
    }

    @Test
    void productFormExposesManufacturerCreationPricingAndLifecycleControls() {
        final EntityManager entityManager = emptyEntityManager();

        final FormViewDto form = new ProductController(entityManager, null, null, null).getFormularView();

        assertThat(form.getAlias()).isEqualTo("product");
        assertThat(form.getContents()).hasSizeGreaterThanOrEqualTo(10);
        assertThat(form.getPlacements()).hasSize(9);
        assertThat(form.getBusinessRules()).isNotEmpty();
        assertThat(form.getGlobalEventListeners()).isNotEmpty();
    }

    @Test
    void productFormMapsAvailableManufacturers() {
        final ProductManufacturer manufacturer = new ProductManufacturer();
        manufacturer.setId(7L);
        manufacturer.setName("Koku Labs");

        final FormViewDto form = new ProductController(entityManagerReturning(List.of(manufacturer)), null, null, null)
                .getFormularView();

        assertThat(form.getContents()).hasSizeGreaterThanOrEqualTo(10);
    }

    @Test
    void productAndManufacturerQueriesReturnStableEmptyPages() {
        final EntityManager entityManager = emptyEntityManager();

        assertThat(new ProductController(entityManager, null, null, null)
                        .findAll(null)
                        .getResults())
                .isEmpty();
        assertThat(new ProductManufacturersController(entityManager, null, null, null)
                        .findAll(null)
                        .getResults())
                .isEmpty();
    }

    private static EntityManager emptyEntityManager() {
        return entityManagerReturning(List.of());
    }

    @SuppressWarnings("unchecked")
    private static EntityManager entityManagerReturning(final List<?> results) {
        final EntityManager entityManager = mock(EntityManager.class);
        final EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        final Query query = mock(Query.class, RETURNS_SELF);
        when(entityManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(entityManagerFactory.getProperties()).thenReturn(Map.of());
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn((List) results);
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
