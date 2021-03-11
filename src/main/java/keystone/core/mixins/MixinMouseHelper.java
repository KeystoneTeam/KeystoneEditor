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
    @Shadow private double mouseX;
    @Shadow private double mouseY;
    @Shadow private boolean ignoreFirstMove;
    @Shadow @Final private Minecraft minecraft;
    @Shadow private int activeButton;
    @Shadow private double eventTime;

    @Shadow private boolean leftDown;
    @Shadow private boolean middleDown;
    @Shadow private boolean rightDown;

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
            if (leftDown) KeystoneInputHandler.setLeftClickLocation(mouseX, mouseY);
            if (middleDown) KeystoneInputHandler.setMiddleClickLocation(mouseX, mouseY);
            if (rightDown) KeystoneInputHandler.setRightClickLocation(mouseX, mouseY);
        }
    }

    @Inject(method = "cursorPosCallback", at = @At("HEAD"))
    public void cursorPosCallback(long handle, double xpos, double ypos, CallbackInfo callback)
    {
        if (handle == Minecraft.getInstance().getMainWindow().getHandle())
        {
            if (!ignoreFirstMove)
            {
                double mouseX = xpos * (double)minecraft.getMainWindow().getScaledWidth() / (double)minecraft.getMainWindow().getWidth();
                double mouseY = ypos * (double)minecraft.getMainWindow().getScaledHeight() / (double)minecraft.getMainWindow().getHeight();
                KeystoneInputHandler.onMouseMove(mouseX, mouseY);

                if (activeButton != -1 && eventTime > 0)
                {
                    double dragX = (xpos - this.mouseX) * (double)minecraft.getMainWindow().getScaledWidth() / (double)minecraft.getMainWindow().getWidth();
                    double dragY = (ypos - this.mouseY) * (double)minecraft.getMainWindow().getScaledHeight() / (double)minecraft.getMainWindow().getHeight();
                    KeystoneInputHandler.onMouseDrag(activeButton, mouseX, mouseY, dragX, dragY);
                }
            }
        }
    }
}
