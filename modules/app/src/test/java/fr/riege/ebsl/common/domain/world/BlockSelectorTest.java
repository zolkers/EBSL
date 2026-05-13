package fr.riege.ebsl.common.domain.world;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockSelectorTest {
    @Test
    void supportsOrSelectors() {
        BlockSelector selector = BlockSelector.parse("leaf|wood");

        assertTrue(selector.matches(BlockId.of("minecraft:oak_leaves")));
        assertTrue(selector.matches(BlockId.of("minecraft:birch_log")));
        assertFalse(selector.matches(BlockId.of("minecraft:stone")));
    }

    @Test
    void supportsAndNotSelectors() {
        BlockSelector selector = BlockSelector.parse("wood&!crimson_stem");

        assertTrue(selector.matches(BlockId.of("minecraft:birch_log")));
        assertFalse(selector.matches(BlockId.of("minecraft:crimson_stem")));
    }

    @Test
    void keepsSimpleSelectors() {
        assertTrue(BlockSelector.parse("minecraft:oak_leaves").matches(BlockId.of("minecraft:oak_leaves")));
        assertTrue(BlockSelector.parse("leaves").matches(BlockId.of("minecraft:oak_leaves")));
        assertFalse(BlockSelector.parse("minecraft:leaves").matches(BlockId.of("minecraft:oak_leaves")));
    }

    @Test
    void givesAndPrecedenceOverOr() {
        BlockSelector selector = BlockSelector.parse("stone|wood&!crimson_stem");

        assertTrue(selector.matches(BlockId.of("minecraft:stone")));
        assertTrue(selector.matches(BlockId.of("minecraft:birch_log")));
        assertFalse(selector.matches(BlockId.of("minecraft:crimson_stem")));
    }

    @Test
    void normalizesWhitespaceCaseAndDashes() {
        BlockSelector selector = BlockSelector.parse("  OAK-LEAVES  |  birch-log  ");

        assertTrue(selector.matches(BlockId.of("minecraft:oak_leaves")));
        assertTrue(selector.matches(BlockId.of("minecraft:birch_log")));
        assertFalse(selector.matches(BlockId.of("minecraft:stone")));
    }

    @ParameterizedTest
    @CsvSource({
        "'',minecraft:stone",
        "!,minecraft:stone",
        "wood&,minecraft:oak_log"
    })
    void blankOrMalformedSelectorsDoNotMatch(String selectorText, String blockId) {
        assertFalse(BlockSelector.parse(selectorText).matches(BlockId.of(blockId)));
    }
}
