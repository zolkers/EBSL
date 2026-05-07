package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.enums.EbslInputKey;

@EbslNodeDefinition(EbslNodeType.JUMP)
public final class JumpNode extends TimedInputNode {
    public JumpNode() {
        super(EbslInputKey.JUMP, "4t");
    }
}
