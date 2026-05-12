package fr.riege.ebsl.common.feature.aim;

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public record BlockInteractionTarget(BlockAimTarget block, PathPosition standingPosition) {
}
