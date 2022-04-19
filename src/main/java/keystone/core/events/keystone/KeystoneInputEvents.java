package keystone.core.events.keystone;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class KeystoneInputEvents
{
    public static final Event<MouseMoved> MOUSE_MOVED = EventFactory.createArrayBacked(MouseMoved.class, listeners -> (mouseX, mouseY) ->
    {
        for (final MouseMoved listener : listeners) listener.mouseMoved(mouseX, mouseY);
    });
    public static final Event<MouseClicked> MOUSE_CLICKED = EventFactory.createArrayBacked(MouseClicked.class, listeners -> (button, modifiers, mouseX, mouseY, gui) ->
    {
        for (final MouseClicked listener : listeners) listener.mouseClicked(button, modifiers, mouseX, mouseY, gui);
    });
    public static final Event<MouseDragStart> START_MOUSE_DRAG = EventFactory.createArrayBacked(MouseDragStart.class, listeners -> (button, mouseX, mouseY, gui) ->
    {
        for (final MouseDragStart listener : listeners) listener.startDrag(button, mouseX, mouseY, gui);
    });
    public static final Event<MouseDrag> MOUSE_DRAG = EventFactory.createArrayBacked(MouseDrag.class, listeners -> (button, mouseX, mouseY, dragX, dragY, gui) ->
    {
        for (final MouseDrag listener : listeners) listener.drag(button, mouseX, mouseY, dragX, dragY, gui);
    });
    public static final Event<MouseDragEnd> END_MOUSE_DRAG = EventFactory.createArrayBacked(MouseDragEnd.class, listeners -> (button, mouseX, mouseY, gui) ->
    {
        for (final MouseDragEnd listener : listeners) listener.endDrag(button, mouseX, mouseY, gui);
    });

    public interface MouseMoved
    {
        void mouseMoved(double mouseX, double mouseY);
    }
    public interface MouseClicked
    {
        void mouseClicked(int button, int modifiers, double mouseX, double mouseY, boolean gui);
    }
    public interface MouseDragStart
    {
        void startDrag(int button, double mouseX, double mouseY, boolean gui);
    }
    public interface MouseDrag
    {
        void drag(int button, double mouseX, double mouseY, double dragX, double dragY, boolean gui);
    }
    public interface MouseDragEnd
    {
        void endDrag(int button, double mouseX, double mouseY, boolean gui);
    }
}
