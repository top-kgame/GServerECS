package top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.extensions.entity.BaseEntityFactory;

import java.util.Collection;
import java.util.List;

public class SimplePerfEntityFactory extends BaseEntityFactory {
    public static final int TYPE_ID = 9101;

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    protected Collection<EcsComponent> generateComponent() {
        return List.of(new ComponentPerfSimple());
    }
}
