package keystone.core.mixins.client;

import keystone.api.Keystone;
import keystone.core.events.minecraft.InputEvents;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin
{
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private boolean repeatEvents;

    @Inject(method = "onKey", at = @At("HEAD"))
    public void keyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo info)
    {
        if (windowPointer == client.getWindow().getHandle()) InputEvents.KEY_PRESSED.invoker().keyPressed(key, action, scanCode, modifiers);

        if (Keystone.isEnabled())
        {
            if (action != GLFW.GLFW_PRESS && (action != GLFW.GLFW_REPEAT || !repeatEvents))
            {
                if (action == GLFW.GLFW_RELEASE) KeystoneOverlayHandler.keyReleased(key, scanCode, modifiers);
            }
            else KeystoneOverlayHandler.keyPressed(key, scanCode, modifiers);
        }
    }

    @Inject(method = "onChar", at = @At("HEAD"))
    public void charTyped(long windowPointer, int codePoint, int modifiers, CallbackInfo callback)
    {
        if (windowPointer == client.getWindow().getHandle()) InputEvents.CHAR_TYPED.invoker().charTyped(codePoint, modifiers);

        if (Keystone.isEnabled() && windowPointer == client.getWindow().getHandle())
        {
            if (Character.charCount(codePoint) == 1) KeystoneOverlayHandler.charTyped((char)codePoint, modifiers);
            else for (char c : Character.toChars(codePoint)) KeystoneOverlayHandler.charTyped(c, modifiers);
        }
    }
}
