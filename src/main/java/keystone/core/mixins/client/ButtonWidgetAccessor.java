package keystone.core.mixins.client;

import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ButtonWidget.class)
public interface ButtonWidgetAccessor
{
    @Accessor("onPress")
    ButtonWidget.PressAction getOnPress();
    
    @Accessor("onPress")
    void setOnPress(ButtonWidget.PressAction pressAction);
}
