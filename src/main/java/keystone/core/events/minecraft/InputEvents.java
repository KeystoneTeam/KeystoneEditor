package keystone.core.events.minecraft;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.nio.file.Path;
import java.util.List;

public final class InputEvents
{
    public static final Event<MouseMoved> MOUSE_MOVED = EventFactory.createArrayBacked(MouseMoved.class, listeners -> (x, y) ->
    {
        for (final MouseMoved listener : listeners) listener.mouseMoved(x, y);
    });
    public static final Event<MouseClicked> MOUSE_CLICKED = EventFactory.createArrayBacked(MouseClicked.class, listeners -> (button, action, modifiers) ->
    {
        for (final MouseClicked listener : listeners) listener.mouseClicked(button, action, modifiers);
    });
    public static final Event<MouseScrolled> MOUSE_SCROLLED = EventFactory.createArrayBacked(MouseScrolled.class, listeners -> (offsetX, offsetY) ->
    {
        for (final MouseScrolled listener : listeners) listener.mouseScrolled(offsetX, offsetY);
    });
    public static final Event<FilesDropped> FILES_DROPPED = EventFactory.createArrayBacked(FilesDropped.class, listeners -> (paths) ->
    {
        for (final FilesDropped listener : listeners) listener.filesDropped(paths);
    });
    public static final Event<KeyEvent> KEY_EVENT = EventFactory.createArrayBacked(KeyEvent.class, listeners -> (key, action, scancode, modifiers) ->
    {
        boolean cancelEvent = false;
        for (final KeyEvent listener : listeners) if (listener.onKey(key, action, scancode, modifiers)) cancelEvent = true;
        return cancelEvent;
    });
    public static final Event<CharTyped> CHAR_TYPED = EventFactory.createArrayBacked(CharTyped.class, listeners -> (codePoint, modifiers) ->
    {
        for (final CharTyped listener : listeners) listener.charTyped(codePoint, modifiers);
    });
    
    public interface MouseMoved
    {
        void mouseMoved(double x, double y);
    }
    public interface MouseClicked
    {
        void mouseClicked(int button, int action, int modifiers);
    }
    public interface MouseScrolled
    {
        void mouseScrolled(double offsetX, double offsetY);
    }
    public interface FilesDropped
    {
        void filesDropped(List<Path> paths);
    }
    public interface KeyEvent
    {
        boolean onKey(int key, int action, int scancode, int modifiers);
    }
    public interface CharTyped
    {
        void charTyped(int codePoint, int modifiers);
    }
}
