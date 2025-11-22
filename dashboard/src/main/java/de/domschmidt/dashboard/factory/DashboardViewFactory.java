package de.domschmidt.dashboard.factory;

import de.domschmidt.dashboard.dto.DashboardViewDto;
import de.domschmidt.dashboard.dto.content.containers.AbstractDashboardContainer;
import de.domschmidt.dashboard.dto.content.panels.AbstractDashboardPanel;

import java.util.HashMap;
import java.util.Stack;

public class DashboardViewFactory {

    private final IDashboardViewContentIdGenerator idGenerator;
    private final Stack<AbstractDashboardContainer> containerStack = new Stack<>();
    private final HashMap<String, AbstractDashboardPanel> panels = new HashMap<>();
    private final HashMap<String, AbstractDashboardContainer> containers = new HashMap<>();

    public DashboardViewFactory(
            final IDashboardViewContentIdGenerator idGenerator,
            final AbstractDashboardContainer rootContainer
    ) {
        this.idGenerator = idGenerator;
        rootContainer.setId(this.idGenerator.generateUniqueId(rootContainer.getId(), "container"));
        this.containerStack.add(rootContainer);
        this.containers.put(rootContainer.getId(), rootContainer);
    }

    public String addPanel(final AbstractDashboardPanel panel) {
        panel.setId(this.idGenerator.generateUniqueId(panel.getId(), "field"));
        getTopContentContainer().addContent(panel);
        this.panels.put(panel.getId(), panel);
        return panel.getId();
    }

    public void addContainer(final AbstractDashboardContainer container) {
        container.setId(this.idGenerator.generateUniqueId(container.getId(), "container"));
        getTopContentContainer().addContent(container);
        this.containerStack.add(container);
        this.containers.put(container.getId(), container);
    }

    public void endContainer() {
        this.containerStack.pop();
    }

    private AbstractDashboardContainer getTopContentContainer() {
        return this.containerStack.peek();
    }

    public DashboardViewDto create() {
        final DashboardViewDto result = new DashboardViewDto();

        result.setContentRoot(this.containerStack.firstElement());

        return result;
    }

}
