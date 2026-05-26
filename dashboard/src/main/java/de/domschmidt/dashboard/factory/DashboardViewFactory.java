package de.domschmidt.dashboard.factory;

import de.domschmidt.dashboard.contract.dto.DashboardContainer;
import de.domschmidt.dashboard.contract.dto.DashboardPanel;
import de.domschmidt.dashboard.contract.dto.DashboardView;
import java.util.HashMap;
import java.util.Stack;

public class DashboardViewFactory {

    private final IDashboardViewContentIdGenerator idGenerator;
    private final Stack<DashboardContainer> containerStack = new Stack<>();
    private final HashMap<String, DashboardPanel> panels = new HashMap<>();
    private final HashMap<String, DashboardContainer> containers = new HashMap<>();

    public DashboardViewFactory(
            final IDashboardViewContentIdGenerator idGenerator, final DashboardContainer rootContainer) {
        this.idGenerator = idGenerator;
        rootContainer.setId(this.idGenerator.generateUniqueId(rootContainer.getId(), "container"));
        this.containerStack.add(rootContainer);
        this.containers.put(rootContainer.getId(), rootContainer);
    }

    public String addPanel(final DashboardPanel panel) {
        panel.setId(this.idGenerator.generateUniqueId(panel.getId(), "field"));
        getTopContentContainer().addContentItem(panel);
        this.panels.put(panel.getId(), panel);
        return panel.getId();
    }

    public void addContainer(final DashboardContainer container) {
        container.setId(this.idGenerator.generateUniqueId(container.getId(), "container"));
        getTopContentContainer().addContentItem(container);
        this.containerStack.add(container);
        this.containers.put(container.getId(), container);
    }

    public void endContainer() {
        this.containerStack.pop();
    }

    private DashboardContainer getTopContentContainer() {
        return this.containerStack.peek();
    }

    public DashboardView create() {
        final DashboardView result = new DashboardView();

        result.setContentRoot(this.containerStack.firstElement());

        return result;
    }
}
