package keystone.core.mixins;

import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import keystone.core.events.KeystoneInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHelper.class)
public abstract class MixinMouseHelper
{
    @Shadow private double xpos;
    @Shadow private double ypos;
    @Shadow private boolean ignoreFirstMove;
    @Shadow @Final private Minecraft minecraft;
    @Shadow private int activeButton;
    @Shadow private double lastMouseEventTime;

    @Shadow private boolean isLeftPressed;
    @Shadow private boolean isMiddlePressed;
    @Shadow private boolean isRightPressed;

    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    public void grabMouseHead(CallbackInfo callback)
    {
        if (Keystone.isActive() && !KeystoneGlobalState.AllowPlayerLook) callback.cancel();
    }

    @Inject(method = "grabMouse", at = @At("TAIL"))
    public void grabMouseTail(CallbackInfo callback)
    {
        if (Keystone.isActive())
        {
            if (isLeftPressed) KeystoneInputHandler.setLeftClickLocation(xpos, ypos);
            if (isMiddlePressed) KeystoneInputHandler.setMiddleClickLocation(xpos, ypos);
            if (isRightPressed) KeystoneInputHandler.setRightClickLocation(xpos, ypos);
        }
    }

    @Inject(method = "onMove", at = @At("HEAD"))
    public void onMove(long handle, double xpos, double ypos, CallbackInfo callback)
    {
        if (handle == Minecraft.getInstance().getWindow().getWindow())
        {
            if (!ignoreFirstMove)
            {
                double mouseX = xpos * (double)minecraft.getWindow().getGuiScaledWidth() / (double)minecraft.getWindow().getWidth();
                double mouseY = ypos * (double)minecraft.getWindow().getGuiScaledHeight() / (double)minecraft.getWindow().getHeight();
                KeystoneInputHandler.onMouseMove(mouseX, mouseY);

                if (activeButton != -1 && lastMouseEventTime > 0)
                {
                    double dragX = (xpos - this.xpos) * (double)minecraft.getWindow().getGuiScaledWidth() / (double)minecraft.getWindow().getWidth();
                    double dragY = (ypos - this.ypos) * (double)minecraft.getWindow().getGuiScaledHeight() / (double)minecraft.getWindow().getHeight();
                    KeystoneInputHandler.onMouseDrag(activeButton, mouseX, mouseY, dragX, dragY);
                }
            }
        }
    }
}
