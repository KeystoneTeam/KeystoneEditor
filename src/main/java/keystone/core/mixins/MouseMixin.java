package keystone.core.mixins;

import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import keystone.core.events.KeystoneInputHandler;
import keystone.core.events.minecraft.InputEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.List;

@Mixin(Mouse.class)
public abstract class MouseMixin
{
    @Shadow private double x;
    @Shadow private double y;
    @Shadow private boolean hasResolutionChanged;
    @Shadow @Final private MinecraftClient client;
    @Shadow private int activeButton;
    @Shadow private double glfwTime;

    @Shadow private boolean leftButtonClicked;
    @Shadow private boolean middleButtonClicked;
    @Shadow private boolean rightButtonClicked;

    @Inject(method = "lockCursor", at = @At("HEAD"), cancellable = true)
    public void grabMouseHead(CallbackInfo callback)
    {
        if (Keystone.isActive() && !KeystoneGlobalState.AllowPlayerLook) callback.cancel();
    }

    @Inject(method = "lockCursor", at = @At("TAIL"))
    public void grabMouseTail(CallbackInfo callback)
    {
        if (Keystone.isActive())
        {
            if (leftButtonClicked) KeystoneInputHandler.setLeftClickLocation(x, y);
            if (middleButtonClicked) KeystoneInputHandler.setMiddleClickLocation(x, y);
            if (rightButtonClicked) KeystoneInputHandler.setRightClickLocation(x, y);
        }
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"))
    public void onMove(long handle, double xpos, double ypos, CallbackInfo callback)
    {
        if (handle == client.getWindow().getHandle()) InputEvents.MOUSE_MOVED.invoker().mouseMoved(xpos, ypos);

        if (handle == MinecraftClient.getInstance().getWindow().getHandle())
        {
            if (!hasResolutionChanged)
            {
                double mouseX = xpos * (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth();
                double mouseY = ypos * (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight();
                KeystoneInputHandler.onMouseMove(mouseX, mouseY);

                if (activeButton != -1 && glfwTime > 0)
                {
                    double dragX = (xpos - this.x) * (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth();
                    double dragY = (ypos - this.y) * (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight();
                    KeystoneInputHandler.onMouseDrag(activeButton, mouseX, mouseY, dragX, dragY);
                }
            }
        }
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void onMouseButton(long window, int button, int action, int modifiers, CallbackInfo callback)
    {
        if (window == client.getWindow().getHandle()) InputEvents.MOUSE_CLICKED.invoker().mouseClicked(button, action, modifiers);
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    public void onMouseScroll(long window, double offsetX, double offsetY, CallbackInfo callback)
    {
        if (window == client.getWindow().getHandle()) InputEvents.MOUSE_SCROLLED.invoker().mouseScrolled(offsetX, offsetY);
    }

    @Inject(method = "onFilesDropped", at = @At("HEAD"))
    public void onFilesDropped(long window, List<Path> paths, CallbackInfo callback)
    {
        if (window == client.getWindow().getHandle()) InputEvents.FILES_DROPPED.invoker().filesDropped(paths);
    }

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerAbilities;setFlySpeed(F)V"), cancellable = true)
    public void cancelFlySpeed(long handle, double scrollX, double scrollY, CallbackInfo callback)
    {
        if (Keystone.isActive()) callback.cancel();
    }
}
