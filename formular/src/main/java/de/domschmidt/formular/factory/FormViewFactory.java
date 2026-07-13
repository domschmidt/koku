package de.domschmidt.formular.factory;

import de.domschmidt.formular.dto.AbstractFormViewGlobalEventListenerDto;
import de.domschmidt.formular.dto.FormPlacementDto;
import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import de.domschmidt.koku.business_logic.dto.KokuBusinessRuleDto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class FormViewFactory {

    private final String alias;
    private final LinkedHashMap<String, AbstractFormularContent> contentById = new LinkedHashMap<>();
    private final List<FormPlacementDto> placements = new ArrayList<>();
    private final List<KokuBusinessRuleDto> businessRules = new ArrayList<>();
    private final List<AbstractFormViewGlobalEventListenerDto> globalEventListeners = new ArrayList<>();

    public FormViewFactory() {
        this(null);
    }

    public FormViewFactory(final String alias) {
        this.alias = alias;
    }

    public String addContent(final AbstractFormularContent content) {
        content.setId(generateUniqueId(this.contentById.keySet()));
        this.contentById.put(content.getId(), content);
        return content.getId();
    }

    public void addBusinessRule(final KokuBusinessRuleDto businessRule) {
        final Set<String> knownIds = new HashSet<>();
        this.businessRules.stream()
                .map(KokuBusinessRuleDto::getId)
                .filter(Objects::nonNull)
                .forEach(knownIds::add);
        businessRule.setId(generateUniqueId(knownIds));
        this.businessRules.add(businessRule);
    }

    public PlacementParentStep place(final String childId) {
        return new PlacementParentStep(childId);
    }

    public class PlacementParentStep {
        private final String childId;

        private PlacementParentStep(final String childId) {
            this.childId = childId;
        }

        public PlacementOutletStep in(final String parentId) {
            return new PlacementOutletStep(parentId, this.childId);
        }
    }

    public class PlacementOutletStep {
        private final String parentId;
        private final String childId;

        private PlacementOutletStep(final String parentId, final String childId) {
            this.parentId = parentId;
            this.childId = childId;
        }

        public void outlet(final FormOutlet outlet) {
            outlet(Objects.requireNonNull(outlet, "outlet must not be null").outletName());
        }

        public void outlet(final String outlet) {
            setPlacement(outlet);
        }

        private void setPlacement(final String outlet) {
            if (!contentById.containsKey(this.parentId)) {
                throw new IllegalArgumentException("Placement parent not found: " + this.parentId);
            }
            if (!contentById.containsKey(this.childId)) {
                throw new IllegalArgumentException("Placement child not found: " + this.childId);
            }
            if (this.parentId.equals(this.childId)) {
                throw new IllegalArgumentException("Content cannot be placed into itself: " + this.childId);
            }
            placements.removeIf(placement -> this.childId.equals(placement.getChildId()));
            placements.add(FormPlacementDto.builder()
                    .parentId(this.parentId)
                    .outlet(outlet)
                    .childId(this.childId)
                    .build());
        }
    }

    private String generateUniqueId(final Set<String> knownIds) {
        String id = UUID.randomUUID().toString();
        while (knownIds.contains(id)) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void addGlobalEventListener(final AbstractFormViewGlobalEventListenerDto listener) {
        this.globalEventListeners.add(listener);
    }

    public FormViewDto create(final String rootId) {
        validateRoot(rootId);
        validatePlacements(rootId);
        validateBusinessRuleReferences();
        final FormViewDto result = new FormViewDto();

        result.setAlias(this.alias);
        result.setRootId(rootId);
        result.setContents(new LinkedHashMap<>(this.contentById));
        result.setPlacements(List.copyOf(this.placements));
        result.setBusinessRules(List.copyOf(this.businessRules));
        result.setGlobalEventListeners(List.copyOf(this.globalEventListeners));

        return result;
    }

    private void validateBusinessRuleReferences() {
        for (final KokuBusinessRuleDto businessRule : this.businessRules) {
            if (businessRule.getReferences() == null) {
                continue;
            }
            businessRule.getReferences().forEach(reference -> {
                if (reference.getReference() == null || !this.contentById.containsKey(reference.getReference())) {
                    throw new IllegalStateException(
                            "Business rule references unknown content: " + reference.getReference());
                }
            });
        }
    }

    private void validateRoot(final String rootId) {
        if (rootId == null || rootId.isEmpty()) {
            throw new IllegalArgumentException("Root content id must be specified");
        }
        if (!this.contentById.containsKey(rootId)) {
            throw new IllegalArgumentException("Root content not found: " + rootId);
        }
    }

    private void validatePlacements(final String rootId) {
        final Map<String, String> parentByChildId = new HashMap<>();

        for (final FormPlacementDto placement : this.placements) {
            registerPlacement(parentByChildId, validatePlacement(placement));
        }

        validateAllContentPlaced(rootId, parentByChildId);
        assertAcyclic(parentByChildId);
    }

    private PlacementIds validatePlacement(final FormPlacementDto placement) {
        final String parentId = requirePlacementParentId(placement);
        requirePlacementOutlet(placement, parentId);
        final String childId = requirePlacementChildId(placement, parentId);
        validatePlacementContent(parentId, childId);
        return new PlacementIds(parentId, childId);
    }

    private String requirePlacementParentId(final FormPlacementDto placement) {
        final String parentId = placement.getParentId();
        if (parentId == null || parentId.isEmpty()) {
            throw new IllegalStateException("Placement parentId must be specified");
        }
        return parentId;
    }

    private void requirePlacementOutlet(final FormPlacementDto placement, final String parentId) {
        final String outlet = placement.getOutlet();
        if (outlet == null || outlet.isEmpty()) {
            throw new IllegalStateException("Placement outlet must be specified for parent: " + parentId);
        }
    }

    private String requirePlacementChildId(final FormPlacementDto placement, final String parentId) {
        final String childId = placement.getChildId();
        if (childId == null || childId.isEmpty()) {
            throw new IllegalStateException("Placement childId must be specified for parent: " + parentId);
        }
        return childId;
    }

    private void validatePlacementContent(final String parentId, final String childId) {
        if (!this.contentById.containsKey(parentId)) {
            throw new IllegalStateException("Placement parent not found: " + parentId);
        }
        if (!this.contentById.containsKey(childId)) {
            throw new IllegalStateException("Placement child not found: " + childId);
        }
        if (parentId.equals(childId)) {
            throw new IllegalStateException("Content cannot be placed into itself: " + childId);
        }
    }

    private void registerPlacement(final Map<String, String> parentByChildId, final PlacementIds placement) {
        final String previousParent = parentByChildId.putIfAbsent(placement.childId(), placement.parentId());
        if (previousParent != null) {
            throw new IllegalStateException("Content is placed multiple times: " + placement.childId());
        }
    }

    private void validateAllContentPlaced(final String rootId, final Map<String, String> parentByChildId) {
        for (final String contentId : this.contentById.keySet()) {
            if (!contentId.equals(rootId) && !parentByChildId.containsKey(contentId)) {
                throw new IllegalStateException("Content is registered but not placed: " + contentId);
            }
        }
    }

    private void assertAcyclic(final Map<String, String> parentByChildId) {
        for (final String contentId : this.contentById.keySet()) {
            final Set<String> visited = new HashSet<>();
            String currentId = contentId;
            while (parentByChildId.containsKey(currentId)) {
                if (!visited.add(currentId)) {
                    throw new IllegalStateException("Placement cycle detected at content: " + contentId);
                }
                currentId = parentByChildId.get(currentId);
            }
        }
    }

    private record PlacementIds(String parentId, String childId) {}
}
