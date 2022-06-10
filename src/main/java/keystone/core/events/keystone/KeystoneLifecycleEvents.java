package keystone.core.events.keystone;

import keystone.core.modules.schematic_import.ImportBoundingBox;
import keystone.core.modules.selection.SelectionBoundingBox;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.world.ClientWorld;

import java.util.List;

public final class KeystoneLifecycleEvents
{
    public static final Event<JoinWorld> JOIN = EventFactory.createArrayBacked(JoinWorld.class, listeners -> world ->
    {
        for (final JoinWorld listener : listeners) listener.join(world);
    });
    public static final Event<LeaveWorld> LEAVE = EventFactory.createArrayBacked(LeaveWorld.class, listeners -> () ->
    {
        for (final LeaveWorld listener : listeners) listener.leave();
    });
    public static final Event<SelectionChanged> SELECTION_CHANGED = EventFactory.createArrayBacked(SelectionChanged.class, listeners -> (selectionBoxes, createdSelection, createHistoryEntry) ->
    {
        for (final SelectionChanged listener : listeners) listener.selectionChanged(selectionBoxes, createdSelection, createHistoryEntry);
    });
    public static final Event<ImportsChanged> IMPORTS_CHANGED = EventFactory.createArrayBacked(ImportsChanged.class, listeners -> (importBoxes, createHistoryEntry) ->
    {
        for (final ImportsChanged listener : listeners) listener.importsChanged(importBoxes, createHistoryEntry);
    });

    public interface JoinWorld
    {
        void join(ClientWorld world);
    }
    public interface LeaveWorld
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
