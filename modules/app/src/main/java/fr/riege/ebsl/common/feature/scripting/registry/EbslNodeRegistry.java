package fr.riege.ebsl.common.feature.scripting.registry;

import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.nodes.*;
import fr.riege.ebsl.common.feature.terminal.goal.GoalUiCatalog;
import fr.riege.ebsl.common.feature.terminal.goal.GoalUiDefinition;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.function.Supplier;

public final class EbslNodeRegistry {
    private static final MapRegistry<String, RegisteredNode> NODES = new MapRegistry<>(null);

    static {
        register(StartNode::new);
        register(StartChainNode::new);
        register(EventCallNode::new);
        register(SetVariableNode::new);
        register(ChangeVariableNode::new);
        register(CreateListNode::new);
        register(AddToListNode::new);
        register(RemoveFirstFromListNode::new);
        register(RemoveLastFromListNode::new);
        register(RemoveListItemNode::new);
        register(ListItemNode::new);
        register(ListLengthNode::new);
        register(OperatorRandomNode::new);
        register(OperatorModNode::new);
        register(GotoNode::new);
        register(GoalNearestBlockNode::new);
        register(TravelNode::new);
        register(ComeNode::new);
        register(StopNode::new);
        register(StopChainNode::new);
        register(StopAllNode::new);
        register(WaitNode::new);
        register(WaitUntilNode::new);
        register(MessageNode::new);
        register(NoRenderNode::new);
        register(WalkNode::new);
        register(JumpNode::new);
        register(CrawlNode::new);
        register(CrouchNode::new);
        register(SprintNode::new);
        register(PressKeyNode::new);
        register(AimAtNode::new);
        register(AimAtBlockNode::new);
        register(SwingNode::new);
        register(BreakNode::new);
        register(BreakBlockNode::new);
        register(UseNode::new);
        register(InteractNode::new);
        register(PlaceHandNode::new);
        register(LookNode::new);
        register(SpaceMobNode::new);
        for (GoalUiDefinition goal : GoalUiCatalog.all()) {
            register(() -> new CatalogGoalNode(goal));
        }
        for (EbslSensorRegistry.SensorDefinition sensor : EbslSensorRegistry.definitions()) {
            register(() -> new CatalogSensorNode(sensor));
        }
    }

    private EbslNodeRegistry() {
    }

    public static EbslNode get(String id) {
        RegisteredNode node = NODES.get(normalize(id));
        return node == null ? null : node.prototype();
    }

    public static EbslNode create(String id) {
        RegisteredNode node = NODES.get(normalize(id));
        return node == null ? null : node.create();
    }

    public static Collection<EbslNode> nodes() {
        return NODES.values().stream().map(RegisteredNode::prototype).toList();
    }

    public static Collection<EbslNode> canonicalNodes() {
        return new LinkedHashSet<>(nodes());
    }

    private static void register(Supplier<EbslNode> factory) {
        EbslNode prototype = factory.get();
        RegisteredNode registered = new RegisteredNode(prototype, factory);
        NODES.register(normalize(prototype.id()), registered);
        for (String alias : prototype.aliases()) {
            NODES.register(normalize(alias), registered);
        }
    }

    private static String normalize(String id) {
        return id == null ? "" : id.toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private record RegisteredNode(EbslNode prototype, Supplier<EbslNode> factory) {
        private EbslNode create() {
            return factory.get();
        }
    }
}
