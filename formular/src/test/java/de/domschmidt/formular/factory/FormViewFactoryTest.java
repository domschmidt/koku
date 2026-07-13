package de.domschmidt.formular.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

import de.domschmidt.formular.dto.AbstractFormViewGlobalEventListenerDto;
import de.domschmidt.formular.dto.EnumFormViewSubmitMethod;
import de.domschmidt.formular.dto.FormPlacementDto;
import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import de.domschmidt.formular.dto.content.buttons.EnumButtonType;
import de.domschmidt.koku.business_logic.dto.KokuBusinessRuleDto;
import de.domschmidt.koku.business_logic.dto.KokuBusinessRuleFieldReferenceDto;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class FormViewFactoryTest {

    @Test
    void preservesOptionalAliasWithoutForcingOne() {
        final FormViewFactory anonymousFactory = new FormViewFactory();
        final String anonymousRoot = anonymousFactory.addContent(new TestContent());
        final FormViewFactory aliasedFactory = new FormViewFactory("customer appointment");
        final String aliasedRoot = aliasedFactory.addContent(new TestContent());

        assertThat(anonymousFactory.create(anonymousRoot).getAlias()).isNull();
        assertThat(aliasedFactory.create(aliasedRoot).getAlias()).isEqualTo("customer appointment");
    }

    @Test
    void createsDefensiveSnapshotsOfContentsAndPlacements() {
        final FormViewFactory factory = new FormViewFactory("activity");
        final String root = factory.addContent(new TestContent());
        final String child = factory.addContent(new TestContent());
        factory.place(child).in(root).outlet(FormOutlet.CONTENT);

        final FormViewDto view = factory.create(root);

        assertThat(view.getRootId()).isEqualTo(root);
        assertThat(view.getContents()).containsOnlyKeys(root, child);
        assertThat(view.getPlacements()).singleElement().satisfies(placement -> {
            assertThat(placement.getParentId()).isEqualTo(root);
            assertThat(placement.getChildId()).isEqualTo(child);
            assertThat(placement.getOutlet()).isEqualTo(FormOutlet.CONTENT.outletName());
        });
        final List<FormPlacementDto> placements = view.getPlacements();
        assertThatThrownBy(placements::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void replacingAPlacementMovesTheChildInsteadOfDuplicatingIt() {
        final FormViewFactory factory = new FormViewFactory();
        final String root = factory.addContent(new TestContent());
        final String otherParent = factory.addContent(new TestContent());
        final String child = factory.addContent(new TestContent());
        factory.place(otherParent).in(root).outlet(FormOutlet.CONTENT);
        factory.place(child).in(root).outlet(FormOutlet.CONTENT);

        factory.place(child).in(otherParent).outlet("secondary");

        assertThat(factory.create(root).getPlacements())
                .filteredOn(placement -> child.equals(placement.getChildId()))
                .singleElement()
                .satisfies(placement -> {
                    assertThat(placement.getParentId()).isEqualTo(otherParent);
                    assertThat(placement.getOutlet()).isEqualTo("secondary");
                });
    }

    @Test
    void rejectsInvalidRootsAndUnplacedContent() {
        final FormViewFactory factory = new FormViewFactory();
        final String root = factory.addContent(new TestContent());
        factory.addContent(new TestContent());

        assertThatThrownBy(() -> factory.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Root content id");
        assertThatThrownBy(() -> factory.create("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Root content not found");
        assertThatThrownBy(() -> factory.create(root))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not placed");
    }

    @Test
    void rejectsInvalidPlacementCommandsImmediately() {
        final FormViewFactory factory = new FormViewFactory();
        final String root = factory.addContent(new TestContent());
        final String child = factory.addContent(new TestContent());

        final FormViewFactory.PlacementOutletStep missingParent =
                factory.place(child).in("missing");
        final FormViewFactory.PlacementOutletStep missingChild =
                factory.place("missing").in(root);
        final FormViewFactory.PlacementOutletStep selfPlacement =
                factory.place(root).in(root);
        final FormViewFactory.PlacementOutletStep missingOutlet =
                factory.place(child).in(root);

        assertThatThrownBy(() -> missingParent.outlet(FormOutlet.CONTENT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("parent not found");
        assertThatThrownBy(() -> missingChild.outlet(FormOutlet.CONTENT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("child not found");
        assertThatThrownBy(() -> selfPlacement.outlet(FormOutlet.CONTENT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("itself");
        assertThatThrownBy(() -> missingOutlet.outlet((FormOutlet) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("outlet");
    }

    @Test
    void rejectsPlacementCycles() {
        final FormViewFactory factory = new FormViewFactory();
        final String root = factory.addContent(new TestContent());
        final String child = factory.addContent(new TestContent());
        factory.place(child).in(root).outlet(FormOutlet.CONTENT);
        factory.place(root).in(child).outlet(FormOutlet.CONTENT);

        assertThatThrownBy(() -> factory.create(root))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cycle");
    }

    @Test
    void validatesBusinessRuleReferencesAndSnapshotsRulesAndListeners() throws Exception {
        final FormViewFactory factory = new FormViewFactory();
        final String root = factory.addContent(new TestContent());
        final KokuBusinessRuleDto validRule = KokuBusinessRuleDto.builder()
                .id("caller-controlled")
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(root)
                        .build())
                .build();
        final KokuBusinessRuleDto ruleWithoutReferences =
                KokuBusinessRuleDto.builder().build();
        final Field references = KokuBusinessRuleDto.class.getDeclaredField("references");
        references.setAccessible(true);
        references.set(ruleWithoutReferences, null);
        final AbstractFormViewGlobalEventListenerDto listener =
                TestGlobalEventListener.builder().eventName("updated").build();
        factory.addBusinessRule(validRule);
        factory.addBusinessRule(ruleWithoutReferences);
        factory.addGlobalEventListener(listener);

        final FormViewDto view = factory.create(root);

        assertThat(view.getBusinessRules()).containsExactly(validRule, ruleWithoutReferences);
        assertThat(validRule.getId()).isNotBlank().isNotEqualTo("caller-controlled");
        assertThat(ruleWithoutReferences.getId()).isNotBlank().isNotEqualTo(validRule.getId());
        assertThat(view.getGlobalEventListeners()).containsExactly(listener);
        final List<KokuBusinessRuleDto> businessRules = view.getBusinessRules();
        final List<AbstractFormViewGlobalEventListenerDto> globalEventListeners = view.getGlobalEventListeners();
        assertThatThrownBy(businessRules::clear).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(globalEventListeners::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsUnknownAndMissingBusinessRuleReferences() {
        final FormViewFactory unknownReferenceFactory = new FormViewFactory();
        final String unknownRoot = unknownReferenceFactory.addContent(new TestContent());
        unknownReferenceFactory.addBusinessRule(KokuBusinessRuleDto.builder()
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference("missing")
                        .build())
                .build());
        final FormViewFactory missingReferenceFactory = new FormViewFactory();
        final String missingRoot = missingReferenceFactory.addContent(new TestContent());
        missingReferenceFactory.addBusinessRule(KokuBusinessRuleDto.builder()
                .reference(KokuBusinessRuleFieldReferenceDto.builder().build())
                .build());

        assertThatThrownBy(() -> unknownReferenceFactory.create(unknownRoot))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unknown content");
        assertThatThrownBy(() -> missingReferenceFactory.create(missingRoot))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unknown content");
    }

    @Test
    void enumContractsExposeAllWireValues() {
        assertThat(EnumFormViewSubmitMethod.values()).hasSize(2);
        assertThat(EnumButtonType.values()).hasSize(2);
    }

    @Test
    void retriesGeneratedIdsOnCollision() {
        final UUID first = UUID.fromString("00000000-0000-0000-0000-000000000001");
        final UUID second = UUID.fromString("00000000-0000-0000-0000-000000000002");
        try (MockedStatic<UUID> uuid = mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(first, first, second);
            final FormViewFactory factory = new FormViewFactory();

            assertThat(factory.addContent(new TestContent())).isEqualTo(first.toString());
            assertThat(factory.addContent(new TestContent())).isEqualTo(second.toString());
        }
    }

    @Test
    void validatesCorruptInternalPlacementSnapshots() throws Exception {
        assertCorruptPlacementRejected(
                FormPlacementDto.builder().outlet("content").childId("child").build(), "parentId");
        assertCorruptPlacementRejected(
                FormPlacementDto.builder().parentId("root").childId("child").build(), "outlet");
        assertCorruptPlacementRejected(
                FormPlacementDto.builder().parentId("root").outlet("content").build(), "childId");
        assertCorruptPlacementRejected(
                FormPlacementDto.builder()
                        .parentId("missing")
                        .outlet("content")
                        .childId("child")
                        .build(),
                "parent not found");
        assertCorruptPlacementRejected(
                FormPlacementDto.builder()
                        .parentId("root")
                        .outlet("content")
                        .childId("missing")
                        .build(),
                "child not found");
        assertCorruptPlacementRejected(
                FormPlacementDto.builder()
                        .parentId("root")
                        .outlet("content")
                        .childId("root")
                        .build(),
                "itself");
    }

    @Test
    void rejectsDuplicateAndDisconnectedInternalPlacements() throws Exception {
        final FormViewFactory duplicateFactory = factoryWithRootAndChild();
        final String root = firstContentId(duplicateFactory);
        final String child = secondContentId(duplicateFactory);
        placements(duplicateFactory)
                .add(FormPlacementDto.builder()
                        .parentId(root)
                        .outlet("content")
                        .childId(child)
                        .build());
        placements(duplicateFactory)
                .add(FormPlacementDto.builder()
                        .parentId(root)
                        .outlet("secondary")
                        .childId(child)
                        .build());
        assertThatThrownBy(() -> duplicateFactory.create(root)).hasMessageContaining("multiple times");

        final FormViewFactory disconnectedFactory = factoryWithRootAndChild();
        final String disconnectedRoot = firstContentId(disconnectedFactory);
        final String disconnectedChild = secondContentId(disconnectedFactory);
        final String third = disconnectedFactory.addContent(new TestContent());
        placements(disconnectedFactory)
                .add(FormPlacementDto.builder()
                        .parentId(disconnectedChild)
                        .outlet("content")
                        .childId(third)
                        .build());
        placements(disconnectedFactory)
                .add(FormPlacementDto.builder()
                        .parentId(third)
                        .outlet("content")
                        .childId(disconnectedChild)
                        .build());
        assertThatThrownBy(() -> disconnectedFactory.create(disconnectedRoot)).hasMessageContaining("cycle");
    }

    private static void assertCorruptPlacementRejected(final FormPlacementDto placement, final String message)
            throws Exception {
        final FormViewFactory factory = factoryWithRootAndChild();
        final String root = firstContentId(factory);
        final String child = secondContentId(factory);
        if ("root".equals(placement.getParentId())) {
            placement.setParentId(root);
        }
        if ("root".equals(placement.getChildId())) {
            placement.setChildId(root);
        } else if ("child".equals(placement.getChildId())) {
            placement.setChildId(child);
        }
        placements(factory).add(placement);
        assertThatThrownBy(() -> factory.create(root)).hasMessageContaining(message);
    }

    private static FormViewFactory factoryWithRootAndChild() {
        final FormViewFactory factory = new FormViewFactory();
        factory.addContent(new TestContent());
        factory.addContent(new TestContent());
        return factory;
    }

    @SuppressWarnings("unchecked")
    private static List<FormPlacementDto> placements(final FormViewFactory factory) throws Exception {
        final Field field = FormViewFactory.class.getDeclaredField("placements");
        field.setAccessible(true);
        return (List<FormPlacementDto>) field.get(factory);
    }

    @SuppressWarnings("unchecked")
    private static List<String> contentIds(final FormViewFactory factory) throws Exception {
        final Field field = FormViewFactory.class.getDeclaredField("contentById");
        field.setAccessible(true);
        return List.copyOf(((java.util.Map<String, AbstractFormularContent>) field.get(factory)).keySet());
    }

    private static String firstContentId(final FormViewFactory factory) throws Exception {
        return contentIds(factory).get(0);
    }

    private static String secondContentId(final FormViewFactory factory) throws Exception {
        return contentIds(factory).get(1);
    }

    private static final class TestContent extends AbstractFormularContent {}

    @SuperBuilder
    private static final class TestGlobalEventListener extends AbstractFormViewGlobalEventListenerDto {}
}
