package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import java.util.List;

abstract class AbstractEbslNode implements EbslNode {
    private final String id;
    private final List<String> aliases;

    AbstractEbslNode() {
        EbslNodeDefinition definition = definition(getClass());
        this.id = definition.value().id();
        this.aliases = List.of(definition.aliases());
    }

    AbstractEbslNode(String id) {
        this(id, List.of());
    }

    AbstractEbslNode(String id, List<String> aliases) {
        this.id = id;
        this.aliases = List.copyOf(aliases);
    }

    @Override
    public final String id() {
        return id;
    }

    @Override
    public final List<String> aliases() {
        return aliases;
    }

    private static EbslNodeDefinition definition(Class<?> nodeClass) {
        EbslNodeDefinition definition = nodeClass.getAnnotation(EbslNodeDefinition.class);
        if (definition == null) {
            throw new IllegalStateException("Missing @EbslNodeDefinition on " + nodeClass.getName());
        }
        return definition;
    }
}
