package keystone.core.mixins.client;

import keystone.api.Keystone;
import keystone.api.enums.WorldType;
import keystone.core.modules.session.SessionModule;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen
{
    protected GameMenuScreenMixin(Text title)
    {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/GameMenuScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 8), cancellable = true)
    private void initWidgets(CallbackInfo callback)
    {
        GameMenuScreen _this = (GameMenuScreen)(Object)this;

        MutableText text = client.isInSingleplayer() ? Text.translatable("menu.returnToMenu") : Text.translatable("menu.disconnect");
        addDrawableChild(new ButtonWidget(_this.width / 2 - 102, _this.height / 4 + 120 + -16, 204, 20, text, this::saveAndQuitButton));

        callback.cancel();
    }

    private void saveAndQuitButton(ButtonWidget button)
    {
        WorldType type = WorldType.get();
        if (type.supportedFeatures.sessions())
        {
            SessionModule session = Keystone.getModule(SessionModule.class);
            session.promptUncommittedChanges(() ->
                    {
                        session.commitChanges();
                        vanillaSaveAndQuitButtonBehaviour(button, true);
                    },
                    () ->
                    {
                        button.active = false;
                        session.revertChanges(true);
                        vanillaSaveAndQuitButtonBehaviour(button, false);
                    }, null
            );
        }
        else vanillaSaveAndQuitButtonBehaviour(button, true);
    }
    private void vanillaSaveAndQuitButtonBehaviour(ButtonWidget button, boolean disconnect)
    {
        button.active = false;

        if (disconnect)
        {
            client.world.disconnect();
            client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
        }

        client.setScreen(new TitleScreen());
    }
}
