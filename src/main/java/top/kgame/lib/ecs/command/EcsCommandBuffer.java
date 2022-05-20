package top.kgame.lib.ecs.command;

import java.util.LinkedList;


public class EcsCommandBuffer {
    private final LinkedList<EcsCommand> ecsCommands = new LinkedList<>();

    public void addCommand(EcsCommand command) {
        ecsCommands.add(command);
    }

    public void execute() {
        while (!ecsCommands.isEmpty()) {
            ecsCommands.poll().execute();
        }
    }

    public void clear() {
        ecsCommands.clear();
    }
}
