package top.kgame.lib.ecstest.util.component;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.command.EcsCommand;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecstest.util.KV;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComponentCommandHolder implements EcsComponent {
    private final List<KV<EcsCommandScope, EcsCommand>> commands = new ArrayList<>();

    public void update(EcsCommand command, EcsCommandScope level) {
        commands.add(new KV<>(level, command));
    }

    public void clear() {
        commands.clear();
    }

    public List<KV<EcsCommandScope, EcsCommand>> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}

