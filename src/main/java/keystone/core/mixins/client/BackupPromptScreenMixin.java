package keystone.core.mixins.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import keystone.core.KeystoneConfig;
import keystone.core.KeystoneGlobalState;
import keystone.core.gui.widgets.inputs.FloatWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.BackupPromptScreen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackupPromptScreen.class)
public class BackupPromptScreenMixin extends Screen
{
    protected BackupPromptScreenMixin(Text title)
    {
        super(title);
    }
    
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;", ordinal = 0, shift = At.Shift.BEFORE))
    private void addWidgets(CallbackInfo ci, @Local LocalIntRef textYShift)
    {
        // Check that this screen is the prompt to optimize a world
        if (!isOptimizeScreen()) return;
        KeystoneGlobalState.PurgeUnvisitedChunks = true;
        KeystoneGlobalState.UnvisitedChunkCutoff = (int)KeystoneConfig.unvisitedTimeCutoff;
        
        // Unvisited Chunk Cutoff
        FloatWidget unvisitedChunkCutoff = new FloatWidget(Text.literal("Unvisited Chunk Time Cutoff"), width / 2 - 100, height - FloatWidget.getFinalHeight() - 4, 200, KeystoneConfig.unvisitedTimeCutoff / 20.0f, 0.0f, Float.MAX_VALUE, 0.25f)
        {
            @Override
            protected boolean onSetValue(Float value)
            {
                KeystoneGlobalState.UnvisitedChunkCutoff = (int)(value * 20.0f);
                return true;
            }
        };
        
        // Purge Unvisited Chunks Widget
        CheckboxWidget purgeUnvisitedChunks = CheckboxWidget.builder(Text.literal("Purge Unvisited Chunks"), MinecraftClient.getInstance().textRenderer)
                .pos(this.width / 2 - 155 + 80, 76 + textYShift.get())
                .callback((checkbox, checked) ->
                {
                    unvisitedChunkCutoff.setVisible(checked);
                    KeystoneGlobalState.PurgeUnvisitedChunks = checked;
                })
                .checked(true)
                .build();
        
        // Finalize and Add Widgets
        unvisitedChunkCutoff.setVisible(purgeUnvisitedChunks.isChecked());
        addDrawableChild(purgeUnvisitedChunks);
        addDrawableChild(unvisitedChunkCutoff);
        textYShift.set(textYShift.get() + purgeUnvisitedChunks.getHeight() + 4);
    }
    
    @Unique private boolean isOptimizeScreen()
    {
        return title.equals(Text.translatable("optimizeWorld.confirm.title"));
    }
}
