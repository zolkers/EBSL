package fr.riege.ebsl.common.feature.scripting.nodes;

abstract class NavigationNode extends AbstractEbslNode {
    NavigationNode() {
        super();
    }

    NavigationNode(String id) {
        super(id);
    }

    @Override
    public final boolean waitsForNavigation() {
        return true;
    }
}
