package top.kgame.lib.ecstest.util.entity;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.extensions.entity.BaseEntityFactory;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;

import java.util.Collection;
import java.util.List;

public class EntityDefaultFactory0 extends BaseEntityFactory {

    @Override
    public int typeId() {
        return EntityIndex.E0.getId();
    }

    @Override
    protected Collection<EcsComponent> generateComponent() {
        return List.of(new ComponentLexicographic(), new ComponentCommandHolder());
    }
} 