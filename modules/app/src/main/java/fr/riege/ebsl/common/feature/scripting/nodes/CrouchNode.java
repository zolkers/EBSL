package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.enums.EbslInputKey;

@EbslNodeDefinition(EbslNodeType.CROUCH)
public final class CrouchNode extends TimedInputNode {
    public CrouchNode() {
        super(EbslInputKey.SNEAK, "20t");
    }
}
