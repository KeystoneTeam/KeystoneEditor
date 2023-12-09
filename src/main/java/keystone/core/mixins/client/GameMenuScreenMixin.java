package keystone.core.mixins.client;

import keystone.api.Keystone;
import keystone.api.enums.WorldType;
import keystone.core.modules.session.SessionModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen
{
    @Shadow private @Nullable ButtonWidget exitButton;
    
    protected GameMenuScreenMixin(Text title)
    {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("RETURN"))
    private void modifySaveAndQuit(CallbackInfo callback)
    {
        ButtonWidget.PressAction originalAction = exitButton.onPress;
        exitButton.onPress = button -> saveAndQuitButton(button, originalAction);
    }

    @Unique
    private static void saveAndQuitButton(ButtonWidget button, ButtonWidget.PressAction originalAction)
    {
        WorldType type = WorldType.get();
        MinecraftClient client = MinecraftClient.getInstance();
        
        if (type.supportedFeatures.sessions())
        {
            SessionModule session = Keystone.getModule(SessionModule.class);
            session.promptUncommittedChanges(() ->
                    {
                        session.commitChanges();
                        originalAction.onPress(button);
                    },
                    () ->
                    {
                        button.active = false;
                        session.revertChanges(true);
                        
                        TitleScreen titleScreen = new TitleScreen();
                        if (type == WorldType.SINGLEPLAYER || type == WorldType.HOSTING_LAN) client.setScreen(titleScreen);
                        else if (type == WorldType.REALMS) client.setScreen(new RealmsMainScreen(titleScreen));
                        else client.setScreen(new MultiplayerScreen(titleScreen));
                    }, null
            );
        }
        else originalAction.onPress(button);
    }
}
