package fr.riege.ebsl.common.feature.scripting.manager;

import java.util.List;
import java.util.Map;

public record EbslGraphDocument(
    Map<String, EbslGraphNodePosition> positions,
    List<EbslGraphConnection> connections
) {
    public static EbslGraphDocument empty() {
        return new EbslGraphDocument(Map.of(), List.of());
    }
}
