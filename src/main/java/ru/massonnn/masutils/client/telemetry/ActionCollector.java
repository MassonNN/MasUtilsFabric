package ru.massonnn.masutils.client.telemetry;

import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.events.LocationEvents;
import ru.massonnn.masutils.client.events.MineshaftEvent;
import ru.massonnn.masutils.client.events.PartyEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ActionCollector {
    private final static CopyOnWriteArrayList<Action> lastActions = new CopyOnWriteArrayList<>();
    private static final int ACTION_TTL_SECONDS = 5 * 60;
    private static final int MAX_ITEMS = 100;

    public static void addAction(String context, String target) {
        purgeActions();
        lastActions.add(new Action(context, target));
        Masutils.LOGGER.info("Telemetry: {} | {}", context, target);
    }

    private static void purgeActions() {
        long threshold = Instant.now().getEpochSecond() - ACTION_TTL_SECONDS;

        lastActions.removeIf(action -> action.timestamp < threshold);

        while (lastActions.size() > MAX_ITEMS) {
            lastActions.removeFirst();
        }
    }

    public static String prepareJson() {
        Map<String, Object> report = new HashMap<>();
        report.put("actions", lastActions);
        report.put("generatedAt", Instant.now().getEpochSecond());

        return Masutils.GSON.toJson(report);
    }

    public static void init () {
        LocationEvents.ON_LOCATION_CHANGE.register((newLocation, prevLocation) -> {
            ActionCollector.addAction(prevLocation.name() + "." + newLocation.name(), "Location changed");
        });
        LocationEvents.JOIN.register(() -> ActionCollector.addAction("", "SkyBlock joined"));
        MineshaftEvent.ON_SPAWNED_MINESHAFT_EVENT.register(() -> ActionCollector.addAction("", "Spawned mineshaft"));
        MineshaftEvent.ON_ENTER_MINESHAFT.register((mineshaftType) -> ActionCollector.addAction(mineshaftType.name(), "Entered mineshaft"));
        MineshaftEvent.ON_LEAVE_MINESHAFT.register(() -> ActionCollector.addAction("", "Left mineshaft"));
        PartyEvent.RECEIVE_COMMAND.register(((command, issuer) -> ActionCollector.addAction(command + "|" + issuer, "Party command")));
    }
}