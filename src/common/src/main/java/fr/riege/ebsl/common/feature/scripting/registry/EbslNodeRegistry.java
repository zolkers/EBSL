package fr.riege.ebsl.common.feature.scripting.registry;

import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.nodes.AddToListNode;
import fr.riege.ebsl.common.feature.scripting.nodes.BreakNode;
import fr.riege.ebsl.common.feature.scripting.nodes.CatalogGoalNode;
import fr.riege.ebsl.common.feature.scripting.nodes.ChangeVariableNode;
import fr.riege.ebsl.common.feature.scripting.nodes.ComeNode;
import fr.riege.ebsl.common.feature.scripting.nodes.CrawlNode;
import fr.riege.ebsl.common.feature.scripting.nodes.CreateListNode;
import fr.riege.ebsl.common.feature.scripting.nodes.CrouchNode;
import fr.riege.ebsl.common.feature.scripting.nodes.EventCallNode;
import fr.riege.ebsl.common.feature.scripting.nodes.GoalNearestBlockNode;
import fr.riege.ebsl.common.feature.scripting.nodes.GotoNode;
import fr.riege.ebsl.common.feature.scripting.nodes.InteractNode;
import fr.riege.ebsl.common.feature.scripting.nodes.JumpNode;
import fr.riege.ebsl.common.feature.scripting.nodes.ListItemNode;
import fr.riege.ebsl.common.feature.scripting.nodes.ListLengthNode;
import fr.riege.ebsl.common.feature.scripting.nodes.LookNode;
import fr.riege.ebsl.common.feature.scripting.nodes.MessageNode;
import fr.riege.ebsl.common.feature.scripting.nodes.OperatorModNode;
import fr.riege.ebsl.common.feature.scripting.nodes.OperatorRandomNode;
import fr.riege.ebsl.common.feature.scripting.nodes.PlaceHandNode;
import fr.riege.ebsl.common.feature.scripting.nodes.PressKeyNode;
import fr.riege.ebsl.common.feature.scripting.nodes.RemoveFirstFromListNode;
import fr.riege.ebsl.common.feature.scripting.nodes.RemoveLastFromListNode;
import fr.riege.ebsl.common.feature.scripting.nodes.RemoveListItemNode;
import fr.riege.ebsl.common.feature.scripting.nodes.SetVariableNode;
import fr.riege.ebsl.common.feature.scripting.nodes.SpaceMobNode;
import fr.riege.ebsl.common.feature.scripting.nodes.SprintNode;
import fr.riege.ebsl.common.feature.scripting.nodes.StartChainNode;
import fr.riege.ebsl.common.feature.scripting.nodes.StartNode;
import fr.riege.ebsl.common.feature.scripting.nodes.StopAllNode;
import fr.riege.ebsl.common.feature.scripting.nodes.StopChainNode;
import fr.riege.ebsl.common.feature.scripting.nodes.StopNode;
import fr.riege.ebsl.common.feature.scripting.nodes.SwingNode;
import fr.riege.ebsl.common.feature.scripting.nodes.TravelNode;
import fr.riege.ebsl.common.feature.scripting.nodes.UseNode;
import fr.riege.ebsl.common.feature.scripting.nodes.WaitNode;
import fr.riege.ebsl.common.feature.scripting.nodes.WaitUntilNode;
import fr.riege.ebsl.common.feature.scripting.nodes.WalkNode;
import fr.riege.ebsl.common.feature.terminal.goal.GoalUiCatalog;
import fr.riege.ebsl.common.feature.terminal.goal.GoalUiDefinition;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Locale;

public final class EbslNodeRegistry {
    private static final MapRegistry<String, EbslNode> NODES = new MapRegistry<>(null);

    static {
        register(new StartNode());
        register(new StartChainNode());
        register(new EventCallNode());
        register(new SetVariableNode());
        register(new ChangeVariableNode());
        register(new CreateListNode());
        register(new AddToListNode());
        register(new RemoveFirstFromListNode());
        register(new RemoveLastFromListNode());
        register(new RemoveListItemNode());
        register(new ListItemNode());
        register(new ListLengthNode());
        register(new OperatorRandomNode());
        register(new OperatorModNode());
        register(new GotoNode());
        register(new GoalNearestBlockNode());
        register(new TravelNode());
        register(new ComeNode());
        register(new StopNode());
        register(new StopChainNode());
        register(new StopAllNode());
        register(new WaitNode());
        register(new WaitUntilNode());
        register(new MessageNode());
        register(new WalkNode());
        register(new JumpNode());
        register(new CrawlNode());
        register(new CrouchNode());
        register(new SprintNode());
        register(new PressKeyNode());
        register(new SwingNode());
        register(new BreakNode());
        register(new UseNode());
        register(new InteractNode());
        register(new PlaceHandNode());
        register(new LookNode());
        register(new SpaceMobNode());
        for (GoalUiDefinition goal : GoalUiCatalog.all()) {
            register(new CatalogGoalNode(goal));
        }
    }

    private EbslNodeRegistry() {
    }

    public static EbslNode get(String id) {
        return NODES.get(normalize(id));
    }

    public static Collection<EbslNode> nodes() {
        return NODES.values();
    }

    public static Collection<EbslNode> canonicalNodes() {
        return new LinkedHashSet<>(NODES.values());
    }

    private static void register(EbslNode node) {
        NODES.register(normalize(node.id()), node);
        for (String alias : node.aliases()) {
            NODES.register(normalize(alias), node);
        }
    }

    private static String normalize(String id) {
        return id.toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
