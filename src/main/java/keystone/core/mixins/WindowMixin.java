package keystone.core.mixins;

import keystone.api.Keystone;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Window.class)
public class WindowMixin
{
    @Shadow private double scaleFactor;
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;
    @Shadow private int framebufferWidth;
    @Shadow private int framebufferHeight;

    /**
     * @author CodeCracked
     */
    @Overwrite
    public void setScaleFactor(double scaleFactor)
    {
        if (Keystone.isActive())
        {
            scaleFactor = Math.max(1, Math.round(this.framebufferHeight / 720.0));
        }

        this.scaleFactor = scaleFactor;
        int i = (int)((double)this.framebufferWidth / scaleFactor);
        this.scaledWidth = (double)this.framebufferWidth / scaleFactor > (double)i ? i + 1 : i;
        int j = (int)((double)this.framebufferHeight / scaleFactor);
        this.scaledHeight = (double)this.framebufferHeight / scaleFactor > (double)j ? j + 1 : j;
    }
}
