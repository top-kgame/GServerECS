package top.kgame.lib.ecs.command;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class EcsCommandBuffer {
    private final Queue<EcsCommand> ecsCommands = new ConcurrentLinkedQueue<>();

    public EcsCommandBuffer() {
    }

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

    public Queue<EcsCommand> getEcsCommands() {
        return ecsCommands;
    }
}
