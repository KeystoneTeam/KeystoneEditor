package keystone.core.gui;

import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WidgetDisabler
{
    private final Set<ClickableWidget> ignoreSet;
    private final Map<ClickableWidget, Boolean> restoreMap;
    private boolean disabled;

    public WidgetDisabler(ClickableWidget... ignore)
    {
        this.ignoreSet = Set.of(ignore);
        this.restoreMap = new HashMap<>();
        this.disabled = false;
    }

    public void disableAll()
    {
        if (!disabled)
        {
            disabled = true;
            restoreMap.clear();
            KeystoneOverlayHandler.forEachClickable(clickable ->
            {
                if (!ignoreSet.contains(clickable))
                {
                    restoreMap.put(clickable, clickable.active);
                    clickable.active = false;
                }
            }, false);
        }
    }
    public void restoreAll()
    {
        if (disabled)
        {
            disabled = false;
            restoreMap.forEach((clickable, active) -> clickable.active = active);
            restoreMap.clear();
        }
    }
}
