package keystone.core.events.keystone;

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
    public static final Event<SelectionChanged> SELECTION_CHANGED = EventFactory.createArrayBacked(SelectionChanged.class, listeners -> (selectionBoxes, createdSelection) ->
    {
        for (final SelectionChanged listener : listeners) listener.selectionChanged(selectionBoxes, createdSelection);
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
        void selectionChanged(List<SelectionBoundingBox> selectionBoxes, boolean createdSelection);
    }
}
