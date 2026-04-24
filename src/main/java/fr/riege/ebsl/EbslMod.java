package fr.riege.ebsl;

import fr.riege.ebsl.event.Event;
import fr.riege.ebsl.event.EventBridge;
import fr.riege.ebsl.event.EventBus;
import fr.riege.ebsl.event.EventBusImpl;
import fr.riege.ebsl.event.EventPhase;
import fr.riege.ebsl.event.EventRegistry;
import fr.riege.ebsl.event.events.game.TickEvent;
import fr.riege.ebsl.event.events.render.RenderWorldEvent;
import fr.riege.ebsl.command.GoalCommands;
import fr.riege.ebsl.command.GoalRegistry;
import fr.riege.ebsl.command.PathCommand;
import fr.riege.ebsl.botting.registry.BotModuleRegistry;
import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.debug.PathVisualizer;
import fr.riege.ebsl.pathfinding.goal.GoalRequestHandlers;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettingsStore;
import fr.riege.ebsl.pathfinding.rotation.RotationExecutor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class EbslMod implements ClientModInitializer {
    public static final String MOD_ID = "ebsl";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static EventBus eventBus;

    @Override
    public void onInitializeClient() {
        eventBus = new EventBusImpl();
        PathfinderSettingsStore.load();
        BotModuleRegistry.bootstrap();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            PathCommand.register(dispatcher));

        new EventBridge(eventBus).register();

        eventBus.subscribe(TickEvent.class, EventPhase.POST, event -> {
            if (event.getClient().player == null || event.getClient().level == null) {
                return;
            }
            PathfindingManager.update(event.getClient());
        });

        eventBus.subscribe(RenderWorldEvent.class, event -> {
            var client = net.minecraft.client.Minecraft.getInstance();
            if (client.player == null) {
                return;
            }
            PathVisualizer.endFrame();
            RotationExecutor.update(client);
            PathVisualizer.renderWorld(event);
        });
        GoalCommands.bootstrap();
        GoalRequestHandlers.bootstrap();
        LOGGER.info("[{}] Event bridge ready, pathfinding commands registered ({} goals).", MOD_ID, GoalRegistry.commands().size());
    }

    public static void onRenderWorld(Matrix4f projection, double camX, double camY, double camZ) {
        postClientEvent(new RenderWorldEvent(projection, camX, camY, camZ));
    }

    public static void postClientEvent(Event event) {
        if (eventBus == null || event == null) {
            return;
        }
        eventBus.post(event);
    }

    public static EventBus events() {
        return eventBus;
    }

    public static List<EventRegistry.Entry> registeredEvents() {
        return EventRegistry.all();
    }
}
