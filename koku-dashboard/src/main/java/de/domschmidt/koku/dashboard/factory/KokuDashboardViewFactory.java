package de.domschmidt.koku.dashboard.factory;

import de.domschmidt.dashboard.factory.IDashboardViewContentIdGenerator;
import de.domschmidt.koku.dashboard.contract.dto.KokuDashboardContent;
import de.domschmidt.koku.dashboard.contract.dto.KokuDashboardGridContainer;
import de.domschmidt.koku.dashboard.contract.dto.KokuDashboardPanel;
import de.domschmidt.koku.dashboard.contract.dto.KokuDashboardView;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Stack;

public class KokuDashboardViewFactory {

    private final IDashboardViewContentIdGenerator idGenerator;
    private final Stack<KokuDashboardGridContainer> containerStack = new Stack<>();
    private final HashMap<String, KokuDashboardPanel> panels = new HashMap<>();
    private final HashMap<String, KokuDashboardGridContainer> containers = new HashMap<>();

    public KokuDashboardViewFactory(
            final IDashboardViewContentIdGenerator idGenerator, final KokuDashboardGridContainer rootContainer) {
        this.idGenerator = idGenerator;
        setId(rootContainer, this.idGenerator.generateUniqueId(getId(rootContainer), "container"));
        this.containerStack.add(rootContainer);
        this.containers.put(getId(rootContainer), rootContainer);
    }

    public String addPanel(final KokuDashboardPanel panel) {
        setId(panel, this.idGenerator.generateUniqueId(getId(panel), "field"));
        getTopContentContainer().addContentItem((KokuDashboardContent) panel);
        this.panels.put(getId(panel), panel);
        return getId(panel);
    }

    public void addContainer(final KokuDashboardGridContainer container) {
        setId(container, this.idGenerator.generateUniqueId(getId(container), "container"));
        getTopContentContainer().addContentItem(container);
        this.containerStack.add(container);
        this.containers.put(getId(container), container);
    }

    public void endContainer() {
        this.containerStack.pop();
    }

    public KokuDashboardView create() {
        return new KokuDashboardView().contentRoot(this.containerStack.firstElement());
    }

    private KokuDashboardGridContainer getTopContentContainer() {
        return this.containerStack.peek();
    }

    private static String getId(final Object content) {
        try {
            return (String) content.getClass().getMethod("getId").invoke(content);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Dashboard content must expose getId()", e);
        }
    }

    private static void setId(final Object content, final String id) {
        try {
            content.getClass().getMethod("setId", String.class).invoke(content, id);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Dashboard content must expose setId(String)", e);
        }
    }
}
