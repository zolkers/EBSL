package fr.riege.ebsl.common.world;

public record BlockId(String namespace, String path) {
    public static final BlockId AIR = new BlockId("minecraft", "air");

    public static BlockId of(String id) {
        int colon = id.indexOf(':');
        if (colon < 0) return new BlockId("minecraft", id);
        return new BlockId(id.substring(0, colon), id.substring(colon + 1));
    }

    @Override public String toString() { return namespace + ":" + path; }
}
