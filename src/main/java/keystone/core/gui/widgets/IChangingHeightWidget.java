package keystone.core.gui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public interface IChangingHeightWidget
{
    List<BiConsumer<IChangingHeightWidget, Integer>> listeners = new ArrayList<>();
    
    int getCurrentHeight();
    void setCurrentHeight(int height, boolean triggerListeners);
    
    default void addListener(BiConsumer<IChangingHeightWidget, Integer> listener) { listeners.add(listener); }
    default void triggerListeners() { listeners.forEach(listener -> listener.accept(this, getCurrentHeight())); }
    default void setCurrentHeight(int height) { setCurrentHeight(height, true); }
}
