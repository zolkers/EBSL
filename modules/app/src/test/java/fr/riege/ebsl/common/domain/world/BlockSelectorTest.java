package fr.riege.ebsl.common.domain.world;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockSelectorTest {
    @ParameterizedTest
    @CsvSource({
        "leaf|wood,minecraft:oak_leaves,true",
        "leaf|wood,minecraft:birch_log,true",
        "leaf|wood,minecraft:stone,false",
        "wood&!crimson_stem,minecraft:birch_log,true",
        "wood&!crimson_stem,minecraft:crimson_stem,false",
        "minecraft:oak_leaves,minecraft:oak_leaves,true",
        "leaves,minecraft:oak_leaves,true",
        "minecraft:leaves,minecraft:oak_leaves,false",
        "stone|wood&!crimson_stem,minecraft:stone,true",
        "stone|wood&!crimson_stem,minecraft:birch_log,true",
        "stone|wood&!crimson_stem,minecraft:crimson_stem,false",
        "'  OAK-LEAVES  |  birch-log  ',minecraft:oak_leaves,true",
        "'  OAK-LEAVES  |  birch-log  ',minecraft:birch_log,true",
        "'  OAK-LEAVES  |  birch-log  ',minecraft:stone,false",
        "'',minecraft:stone,false",
        "!,minecraft:stone,false",
        "wood&,minecraft:oak_log,false"
    })
    void evaluatesSelectors(String selectorText, String blockId, boolean expected) {
        boolean matches = BlockSelector.parse(selectorText).matches(BlockId.of(blockId));
        if (expected) {
            assertTrue(matches);
        } else {
            assertFalse(matches);
        }
    }
}
