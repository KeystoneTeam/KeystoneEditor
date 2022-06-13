package keystone.core.mixins.client;

import keystone.core.KeystoneGlobalState;
import keystone.core.keybinds.KeyBindingUtils;
import keystone.core.keybinds.conflicts.IKeyCondition;
import keystone.core.keybinds.conflicts.IKeyConditionContainer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(KeyBinding.class)
public class KeyBindingMixin implements IKeyConditionContainer
{
    @Shadow private boolean pressed;
    @Shadow @Final private static Map<InputUtil.Key, KeyBinding> KEY_TO_BINDINGS;

    private final List<IKeyCondition> conditions = new ArrayList<>();

    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    public void isPressedConditionCheck(CallbackInfoReturnable<Boolean> callback)
    {
        if (pressed && !testConditions())
        {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "setPressed", at = @At("HEAD"), cancellable = true)
    public void setPressedConditionCheck(boolean pressed, CallbackInfo callback)
    {
        if (pressed && !testConditions())
        {
            this.pressed = false;
            callback.cancel();
        }
    }

    @Inject(method = "onKeyPressed", at = @At("HEAD"), cancellable = true)
    private static void onKeyPressedConditionCheck(InputUtil.Key key, CallbackInfo callback)
    {
        KeyBinding keyBinding = KEY_TO_BINDINGS.get(key);
        if (keyBinding != null && !KeyBindingUtils.testConditions(keyBinding)) callback.cancel();
    }

    @Override
    public void addCondition(IKeyCondition condition) { this.conditions.add(condition); }

    @Override
    public void removeCondition(IKeyCondition condition) { this.conditions.remove(condition); }

    @Override
    public void clearConditions() { this.conditions.clear(); }

    @Override
    public boolean testConditions()
    {
        if (KeystoneGlobalState.BlockingKeys) return false;
        for (IKeyCondition condition : conditions) if (!condition.test()) return false;
        return true;
    }
}