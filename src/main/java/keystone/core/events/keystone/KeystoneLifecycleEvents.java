package keystone.core.events.keystone;

import keystone.core.modules.schematic_import.ImportBoundingBox;
import keystone.core.modules.selection.SelectionBoundingBox;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.world.ClientWorld;

import java.util.List;

public final class KeystoneLifecycleEvents
{
    public static final Event<OpenWorld> OPEN_WORLD = EventFactory.createArrayBacked(OpenWorld.class, listeners -> world ->
    {
        for (final OpenWorld listener : listeners) listener.join(world);
    });
    public static final Event<CloseWorld> CLOSE_WORLD = EventFactory.createArrayBacked(CloseWorld.class, listeners -> () ->
    {
        for (final CloseWorld listener : listeners) listener.leave();
    });
    public static final Event<SelectionChanged> SELECTION_CHANGED = EventFactory.createArrayBacked(SelectionChanged.class, listeners -> (selectionBoxes, createdSelection, createHistoryEntry) ->
    {
        for (final SelectionChanged listener : listeners) listener.selectionChanged(selectionBoxes, createdSelection, createHistoryEntry);
    });
    public static final Event<ImportsChanged> IMPORTS_CHANGED = EventFactory.createArrayBacked(ImportsChanged.class, listeners -> (importBoxes, createHistoryEntry) ->
    {
        for (final ImportsChanged listener : listeners) listener.importsChanged(importBoxes, createHistoryEntry);
    });

    public interface OpenWorld
    {
        void join(ClientWorld world);
    }
    public interface CloseWorld
    {
        void leave();
    }
    public interface SelectionChanged
    {
        void selectionChanged(List<SelectionBoundingBox> selectionBoxes, boolean createdSelection, boolean createHistoryEntry);
    }
    public interface ImportsChanged
    {
        void importsChanged(List<ImportBoundingBox> importBoxes, boolean createHistoryEntry);
    }
}
