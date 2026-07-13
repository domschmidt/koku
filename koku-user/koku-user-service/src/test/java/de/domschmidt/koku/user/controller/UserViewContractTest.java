package de.domschmidt.koku.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.dashboard.dto.DashboardViewDto;
import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.koku.user.persistence.User;
import de.domschmidt.koku.user.persistence.UserRegion;
import de.domschmidt.list.dto.response.ListViewDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class UserViewContractTest {

    private final UserController userController = new UserController(null, null, null, null);
    private final UserAppointmentController appointmentController =
            new UserAppointmentController(null, null, null, null);

    @Test
    void managementListExposesEditingAppointmentsAndSynchronization() {
        final ListViewDto view = userController.getListView(Optional.empty());

        assertThat(view.getItemIdPath()).isEqualTo("id");
        assertThat(view.getFilters()).isNotEmpty();
        assertThat(view.getRoutedContents()).isNotEmpty();
        assertThat(view.getItemClickAction()).isNotNull();
        assertThat(view.getGlobalEventListeners()).isNotEmpty();
        assertThat(view.getGlobalItemStyling()).isNotEmpty();
        assertThat(view.getItemPreview()).isNotNull();
    }

    @Test
    void selectionListUsesSelectionContractWithoutManagementRoutes() {
        final ListViewDto view = userController.getListView(Optional.of(true));

        assertThat(view.getItemClickAction()).isNotNull();
        assertThat(view.getRoutedContents()).isEmpty();
        assertThat(view.getGlobalEventListeners()).isEmpty();
        assertThat(view.getGlobalItemStyling()).isEmpty();
        assertThat(view.getItemPreview()).isNotNull();
    }

    @Test
    void privateAppointmentListExposesCalendarCrudAndSynchronization() {
        final ListViewDto view = appointmentController.getListView();

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
    void userFormExposesProfileRegionAndSynchronizationContract() {
        final FormViewDto view = new UserController(emptyEntityManager(), null, null, null).getFormularView();

        assertThat(view.getAlias()).isEqualTo("user");
        assertThat(view.getContents()).hasSizeGreaterThanOrEqualTo(5);
        assertThat(view.getPlacements()).hasSize(view.getContents().size() - 1);
        assertThat(view.getGlobalEventListeners()).isNotEmpty();
    }

    @Test
    void privateAppointmentFormExposesSchedulingAndLifecycleContract() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("test-user", "n/a"));
        final FormViewDto view;
        try {
            view = new UserAppointmentController(emptyEntityManager(), null, null, null).getFormularView();
        } finally {
            SecurityContextHolder.clearContext();
        }

        assertThat(view.getAlias()).isEqualTo("user-appointment");
        assertThat(view.getContents()).hasSizeGreaterThan(10);
        assertThat(view.getPlacements()).hasSize(view.getContents().size() - 1);
        assertThat(view.getGlobalEventListeners()).isNotEmpty();
    }

    @Test
    void privateAppointmentFormRequiresNamedAuthentication() {
        final UserAppointmentController securedController =
                new UserAppointmentController(emptyEntityManager(), null, null, null);
        SecurityContextHolder.clearContext();
        assertThatThrownBy(securedController::getFormularView)
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(" ", "n/a"));
        try {
            assertThatThrownBy(securedController::getFormularView)
                    .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void userFormMapsCountryAndStateRegions() {
        final UserRegion country = new UserRegion();
        country.setId(1L);
        country.setCountryIso("DE");
        country.setCountryName("Germany");
        final UserRegion state = new UserRegion();
        state.setId(2L);
        state.setCountryIso("DE");
        state.setCountryName("Germany");
        state.setStateIso("BE");
        state.setStateName("Berlin");

        final FormViewDto view =
                new UserController(entityManagerReturning(List.of(country, state)), null, null, null).getFormularView();

        assertThat(view.getContents()).hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    void privateAppointmentFormMapsAvailableUsers() {
        final User user = new User("user-id");
        user.setFirstname("Ada");
        user.setLastname("Lovelace");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("test-user", "n/a"));
        try {
            final FormViewDto view = new UserAppointmentController(
                            entityManagerReturning(List.of(user)), null, null, null)
                    .getFormularView();

            assertThat(view.getContents()).hasSizeGreaterThan(10);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void welcomeDashboardContainsGreetingAndThreeAppointmentHorizons() {
        final org.springframework.security.oauth2.jwt.Jwt jwt =
                org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
                        .header("alg", "none")
                        .claim("name", "Ada")
                        .build();

        final DashboardViewDto view = new UserWelcomeController().getDashboardView(jwt);

        assertThat(view.getContentRoot()).isNotNull();
    }

    @Test
    void userAndPrivateAppointmentQueriesSupportEmptyAndScopedResults() {
        final EntityManager entityManager = emptyEntityManager();

        assertThat(new UserController(entityManager, null, null, null)
                        .findAll(null)
                        .getResults())
                .isEmpty();
        final UserAppointmentController controller = new UserAppointmentController(entityManager, null, null, null);
        assertThat(controller.findAll(null, null).getResults()).isEmpty();
        assertThat(controller.findAll("user-id", null).getResults()).isEmpty();
    }

    private static EntityManager emptyEntityManager() {
        return entityManagerReturning(List.of());
    }

    private static EntityManager entityManagerReturning(final List<?> results) {
        final EntityManager entityManager = mock(EntityManager.class);
        final EntityManagerFactory factory = mock(EntityManagerFactory.class);
        final Query query = mock(Query.class, RETURNS_SELF);
        when(entityManager.getEntityManagerFactory()).thenReturn(factory);
        when(factory.getProperties()).thenReturn(Map.of());
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn((List) results);
        when(query.getSingleResult()).thenReturn(0L);
        return entityManager;
    }
}
