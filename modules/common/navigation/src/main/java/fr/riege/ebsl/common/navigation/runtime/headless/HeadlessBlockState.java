package fr.riege.ebsl.common.navigation.runtime.headless;

import fr.riege.ebsl.common.domain.world.BlockId;

public record HeadlessBlockState(
    BlockId id,
    boolean solid,
    boolean water,
    boolean lava,
    boolean dangerous,
    boolean climbable,
    double height
) {
    public static final HeadlessBlockState AIR = new HeadlessBlockState(BlockId.AIR, false, false, false, false, false, 0.0);
    public static final HeadlessBlockState STONE = solid(BlockId.of("minecraft:stone"));

    public static HeadlessBlockState solid(BlockId id) {
        return new HeadlessBlockState(id, true, false, false, false, false, 1.0);
    }

    public static HeadlessBlockState slab(BlockId id, double height) {
        return new HeadlessBlockState(id, true, false, false, false, false, Math.clamp(height, 0.0, 1.0));
    }

    public static HeadlessBlockState climbable(BlockId id) {
        return new HeadlessBlockState(id, false, false, false, false, true, 0.0);
    }

    public boolean isAir() {
        return !solid && !water && !lava && height <= 0.0 && BlockId.AIR.equals(id);
    }
}
