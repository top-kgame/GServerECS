package top.kgame.lib.ecs.tools;

import java.util.BitSet;

public class EcsUtils {
    public static final BitSet EMPTY_BITSET = new BitSet();
    public static boolean isEmptyBitSet(BitSet bitSet) {
        return EMPTY_BITSET.equals(bitSet);
    }
}
