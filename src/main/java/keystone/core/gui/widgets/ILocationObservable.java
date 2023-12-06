package keystone.core.gui.widgets;

import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.ArrayList;
import java.util.List;

public interface ILocationObservable extends LocationListener
{
    List<LocationListener> listeners = new ArrayList<>();

    default void trigger(ClickableWidget widget) { onLocationChanged(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight()); }
    default boolean addListener(LocationListener listener) { return listeners.add(listener); }
    default boolean removeListener(LocationListener listener) { return listeners.remove(listener); }
    default void clear() { listeners.clear(); }

    @Override
    default void onLocationChanged(int x, int y, int width, int height)
    {
        for (LocationListener listener : listeners) listener.onLocationChanged(x, y, width, height);
    }
}