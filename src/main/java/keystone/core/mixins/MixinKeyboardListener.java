package keystone.core.mixins;

import keystone.api.Keystone;
import keystone.gui.KeystoneOverlayHandler;
import net.minecraft.client.KeyboardListener;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardListener.class)
public class MixinKeyboardListener
{
    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    private boolean repeatEventsEnabled;

    @Inject(method = "onKeyEvent", at = @At("HEAD"))
    public void onKeyEvent(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo info)
    {
        if (Keystone.isActive())
        {
            if (action != GLFW.GLFW_PRESS && (action != GLFW.GLFW_REPEAT || !repeatEventsEnabled))
            {
                if (action == GLFW.GLFW_RELEASE) KeystoneOverlayHandler.keyReleased(key, scanCode, modifiers);
            }
            else KeystoneOverlayHandler.keyPressed(key, scanCode, modifiers);
        }
    }

    @Inject(method = "onCharEvent", at = @At("HEAD"))
    public void onCharEvent(long windowPointer, int codePoint, int modifiers, CallbackInfo callback)
    {
        if (Keystone.isActive() && windowPointer == mc.getMainWindow().getHandle())
        {
            if (Character.charCount(codePoint) == 1) KeystoneOverlayHandler.charTyped((char)codePoint, modifiers);
            else for (char c : Character.toChars(codePoint)) KeystoneOverlayHandler.charTyped(c, modifiers);
        }
    }
}
