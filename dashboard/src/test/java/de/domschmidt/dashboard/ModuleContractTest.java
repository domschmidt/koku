package de.domschmidt.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.dashboard.dto.content.containers.AbstractDashboardContainer;
import de.domschmidt.dashboard.dto.content.panels.AbstractDashboardPanel;
import de.domschmidt.dashboard.factory.DashboardViewFactory;
import de.domschmidt.dashboard.factory.DefaultDashboardViewContentIdGenerator;
import org.junit.jupiter.api.Test;

class ModuleContractTest {
    @Test
    void factoryBuildsNestedDashboardContent() {
        final AbstractDashboardContainer root = mock(AbstractDashboardContainer.class);
        final AbstractDashboardContainer nested = mock(AbstractDashboardContainer.class);
        final AbstractDashboardPanel rootPanel = mock(AbstractDashboardPanel.class);
        final AbstractDashboardPanel nestedPanel = mock(AbstractDashboardPanel.class);
        when(root.getId()).thenReturn("root");
        when(nested.getId()).thenReturn("nested");
        when(rootPanel.getId()).thenReturn("root-panel");
        when(nestedPanel.getId()).thenReturn("nested-panel");
        final DashboardViewFactory factory =
                new DashboardViewFactory(new DefaultDashboardViewContentIdGenerator(), root);

        factory.addPanel(rootPanel);
        factory.addContainer(nested);
        factory.addPanel(nestedPanel);
        factory.endContainer();

        assertSame(root, factory.create().getContentRoot());
        verify(root).addContent(rootPanel);
        verify(root).addContent(nested);
        verify(nested).addContent(nestedPanel);
    }

    @Test
    void idGeneratorCreatesDefaultsAndRejectsDuplicates() {
        final DefaultDashboardViewContentIdGenerator generator = new DefaultDashboardViewContentIdGenerator();

        assertTrue(generator.generateUniqueId(null, "panel").startsWith("panel-"));
        assertEquals("explicit", generator.generateUniqueId("explicit", "panel"));
        assertThrows(IllegalArgumentException.class, () -> generator.generateUniqueId("explicit", "panel"));
    }
}
