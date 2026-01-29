package ru.massonnn.masutils.client.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import ru.massonnn.masutils.client.utils.render.primitive.PrimitiveCollector;

public interface WorldRenderExtractionCallback {
    Event<WorldRenderExtractionCallback> EVENT = EventFactory.createArrayBacked(WorldRenderExtractionCallback.class, callbacks -> collector -> {
        for (WorldRenderExtractionCallback callback : callbacks) {
            callback.onExtract(collector);
        }
    });

    void onExtract(PrimitiveCollector collector);
}
