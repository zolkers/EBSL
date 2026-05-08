package fr.riege.ebsl.common.feature.aim;

import fr.riege.ebsl.common.domain.world.BlockId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockAimTargetingTest {
    @Test
    void matchesBlockGroups() {
        assertTrue(BlockAimTargeting.matches(BlockId.of("minecraft:oak_leaves"), "leaf"));
        assertTrue(BlockAimTargeting.matches(BlockId.of("minecraft:birch_log"), "wood"));
        assertTrue(BlockAimTargeting.matches(BlockId.of("minecraft:crimson_stem"), "wood"));
        assertTrue(BlockAimTargeting.matches(BlockId.of("minecraft:tall_grass"), "grass"));
        assertFalse(BlockAimTargeting.matches(BlockId.of("minecraft:stone"), "wood"));
    }

    @Test
    void keepsExactAndSuffixMatching() {
        assertTrue(BlockAimTargeting.matches(BlockId.of("minecraft:oak_leaves"), "minecraft:oak_leaves"));
        assertTrue(BlockAimTargeting.matches(BlockId.of("minecraft:oak_leaves"), "leaves"));
        assertFalse(BlockAimTargeting.matches(BlockId.of("minecraft:oak_leaves"), "minecraft:leaves"));
    }
}
