package top.kgame.lib.ecstest.util.entity;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.extensions.entity.BaseEntityFactory;
import top.kgame.lib.ecstest.util.component.*;

import java.util.Collection;
import java.util.List;

public class EntityDefaultFactory123 extends BaseEntityFactory {

    @Override
    public int typeId() {
        return EntityIndex.E123.getId();
    }

    @Override
    protected Collection<EcsComponent> generateComponent() {
        return List.of(new Component1(), new Component2(), new Component3(), new ComponentLexicographic(), new ComponentCommandHolder());
    }
} 