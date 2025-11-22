package de.domschmidt.formular.factory;

import de.domschmidt.formular.dto.AbstractFormViewGlobalEventListenerDto;
import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.formular.dto.content.buttons.AbstractFormButton;
import de.domschmidt.formular.dto.content.containers.AbstractFormContainer;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import de.domschmidt.formular.dto.content.layouts.AbstractFormLayout;
import de.domschmidt.koku.business_logic.dto.KokuBusinessRuleDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class FormViewFactory {

    private final IFormViewContentIdGenerator idGenerator;
    private final Stack<AbstractFormContainer> containerStack = new Stack<>();
    private final HashMap<String, AbstractFormField<?>> fields = new HashMap<>();
    private final HashMap<String, AbstractFormContainer> containers = new HashMap<>();
    private final HashMap<String, AbstractFormButton> buttons = new HashMap<>();
    private final HashMap<String, AbstractFormLayout> layouts = new HashMap<>();
    private final List<KokuBusinessRuleDto> businessRules = new ArrayList<>();
    private final List<AbstractFormViewGlobalEventListenerDto> globalEventListeners = new ArrayList<>();

    public FormViewFactory(
            final IFormViewContentIdGenerator idGenerator,
            final AbstractFormContainer rootContainer
    ) {
        this.idGenerator = idGenerator;
        rootContainer.setId(this.idGenerator.generateUniqueId(rootContainer.getId(), "container"));
        this.containerStack.add(rootContainer);
        this.containers.put(rootContainer.getId(), rootContainer);
    }

    public String addField(final AbstractFormField<?> field) {
        field.setId(this.idGenerator.generateUniqueId(field.getId(), "field"));
        getTopContentContainer().addContent(field);
        this.fields.put(field.getId(), field);
        return field.getId();
    }

    public void addButton(final AbstractFormButton button) {
        button.setId(this.idGenerator.generateUniqueId(button.getId(), "button"));
        getTopContentContainer().addContent(button);
        this.buttons.put(button.getId(), button);
    }

    public void addBusinessRule(final KokuBusinessRuleDto businessRule) {
        businessRule.setId(this.idGenerator.generateUniqueId(businessRule.getId(), "field"));
        this.businessRules.add(businessRule);
    }

    public void addLayout(final AbstractFormLayout layout) {
        layout.setId(this.idGenerator.generateUniqueId(layout.getId(), "layout"));
        getTopContentContainer().addContent(layout);
        this.layouts.put(layout.getId(), layout);
    }

    public void addContainer(final AbstractFormContainer container) {
        container.setId(this.idGenerator.generateUniqueId(container.getId(), "container"));
        getTopContentContainer().addContent(container);
        this.containerStack.add(container);
        this.containers.put(container.getId(), container);
    }

    public void endContainer() {
        this.containerStack.pop();
    }

    private AbstractFormContainer getTopContentContainer() {
        return this.containerStack.peek();
    }

    public void addGlobalEventListener(
            final AbstractFormViewGlobalEventListenerDto listener
    ) {
        this.globalEventListeners.add(listener);
    }

    public FormViewDto create() {
        final FormViewDto result = new FormViewDto();

        result.setContentRoot(this.containerStack.firstElement());
        result.setBusinessRules(this.businessRules);
        result.setGlobalEventListeners(this.globalEventListeners);

        return result;
    }

}
