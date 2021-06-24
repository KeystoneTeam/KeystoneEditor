package keystone.core.mixins;

import keystone.api.Keystone;
import keystone.core.gui.KeystoneOverlayHandler;
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
    private Minecraft minecraft;

    @Shadow
    private boolean sendRepeatsToGui;

    @Inject(method = "keyPress", at = @At("HEAD"))
    public void keyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo info)
    {
        if (Keystone.isActive())
        {
            if (action != GLFW.GLFW_PRESS && (action != GLFW.GLFW_REPEAT || !sendRepeatsToGui))
            {
                if (action == GLFW.GLFW_RELEASE) KeystoneOverlayHandler.keyReleased(key, scanCode, modifiers);
            }
            else KeystoneOverlayHandler.keyPressed(key, scanCode, modifiers);
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"))
    public void charTyped(long windowPointer, int codePoint, int modifiers, CallbackInfo callback)
    {
        if (Keystone.isActive() && windowPointer == minecraft.getWindow().getWindow())
        {
            if (Character.charCount(codePoint) == 1) KeystoneOverlayHandler.charTyped((char)codePoint, modifiers);
            else for (char c : Character.toChars(codePoint)) KeystoneOverlayHandler.charTyped(c, modifiers);
        }
    }
}
