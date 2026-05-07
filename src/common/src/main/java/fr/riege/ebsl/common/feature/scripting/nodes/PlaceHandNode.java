package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.enums.EbslInputKey;

@EbslNodeDefinition(EbslNodeType.PLACE_HAND)
public final class PlaceHandNode extends TimedInputNode {
    public PlaceHandNode() {
        super(EbslInputKey.USE, "2t");
    }
}
