package fr.riege.ebsl.common.coverage;

import fr.riege.ebsl.common.core.registry.EnumRegistry;
import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.feature.scripting.EbslDuration;
import fr.riege.ebsl.common.feature.scripting.EbslDurationUnit;
import fr.riege.ebsl.common.pathfinding.util.BlockPosUtil;
import fr.riege.ebsl.common.pathfinding.util.RegionKey;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoreUtilityCoverageTest {
    private enum TestKey {
        FIRST,
        SECOND
    }

    @Test
    void mapRegistryStoresValuesAndRejectsDuplicates() {
        MapRegistry<String, String> registry = new MapRegistry<>("fallback");

        assertTrue(registry.isEmpty());
        assertEquals("fallback", registry.get("missing"));

        registry.register("one", "value");

        assertFalse(registry.isEmpty());
        assertTrue(registry.contains("one"));
        assertEquals("value", registry.get("one"));
        assertEquals(List.of("value"), List.copyOf(registry.values()));
        assertEquals("one", registry.keys().iterator().next());
        assertThrows(IllegalStateException.class, () -> registry.register("one", "again"));
    }

    @Test
    void enumRegistryStoresValuesAndRejectsDuplicates() {
        EnumRegistry<TestKey, String> registry = new EnumRegistry<>(TestKey.class, "fallback");

        assertEquals("fallback", registry.get(TestKey.SECOND));
        registry.register(TestKey.FIRST, "value");

        assertTrue(registry.contains(TestKey.FIRST));
        assertFalse(registry.contains(TestKey.SECOND));
        assertEquals("value", registry.get(TestKey.FIRST));
        assertEquals(List.of(TestKey.FIRST), List.copyOf(registry.keys()));
        assertThrows(IllegalStateException.class, () -> registry.register(TestKey.FIRST, "again"));
    }

    @Test
    void blockPositionPackingRoundTripsCoordinates() {
        long key = BlockPosUtil.pack(-12345, 72, 45678);

        assertEquals(-12345, BlockPosUtil.unpackX(key));
        assertEquals(72, BlockPosUtil.unpackY(key));
        assertEquals(45678, BlockPosUtil.unpackZ(key));
    }

    @Test
    void regionKeyMatchesPositionAndCoordinatePacking() {
        PathPosition position = new PathPosition(-12, 64, 34);

        assertEquals(RegionKey.pack(-12, 64, 34), RegionKey.pack(position));
    }

    @Test
    void durationParsesSupportedUnitsAndFallbacks() {
        assertEquals(EbslDurationUnit.SECOND, EbslDurationUnit.fromToken("1.5s"));
        assertNull(EbslDurationUnit.fromToken(null));
        assertTrue(EbslDurationUnit.hasDurationSuffix("250ms"));
        assertFalse(EbslDurationUnit.hasDurationSuffix("nope"));
        assertEquals(30, EbslDuration.ticks("1.5s"));
        assertEquals(5, EbslDuration.ticks("250ms"));
        assertEquals(7, EbslDuration.ticks("7t"));
        assertEquals(4, EbslDuration.ticks("4.9"));
        assertEquals(0, EbslDuration.ticks("-4"));
        assertEquals(1, EbslDuration.ticks("bad"));
    }
}
