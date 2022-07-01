package top.kgame.lib.ecstest.util.entity;

public enum EntityIndex {
    E0(1),
    E1(2),
    E12(3),
    E23(4),
    E123(5),
    ;
    private final int id;
    EntityIndex(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
}
