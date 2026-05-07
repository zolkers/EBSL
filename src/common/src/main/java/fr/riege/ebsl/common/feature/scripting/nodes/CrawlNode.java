package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.enums.EbslInputKey;

@EbslNodeDefinition(EbslNodeType.CRAWL)
public final class CrawlNode extends TimedInputNode {
    public CrawlNode() {
        super(EbslInputKey.SNEAK, "20t");
    }
}
