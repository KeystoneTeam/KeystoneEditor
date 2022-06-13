package keystone.core.gui.screens;

import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

import java.util.ArrayList;
import java.util.List;

public class PromptQuestionScreen extends Screen
{
    private final Screen parent;
    private final OrderedText question;
    private int questionY;
    
    private final Text confirmMessage;
    private Text denyMessage;
    private Text cancelMessage;

    private final Runnable confirmCallback;
    private Runnable denyCallback;
    private Runnable cancelCallback;

    public PromptQuestionScreen(Screen parent, Text question, Text confirmMessage, Runnable confirmCallback)
    {
        super(question);

        this.parent = parent;
        this.question = Language.getInstance().reorder(question);
        this.confirmCallback = confirmCallback;
        this.confirmMessage = confirmMessage;
    }
    public PromptQuestionScreen addDenyButton(Text message, Runnable callback)
    {
        this.denyMessage = message;
        this.denyCallback = callback;
        return this;
    }
    public PromptQuestionScreen addCancelButton(Text message, Runnable callback)
    {
        this.cancelMessage = message;
        this.cancelCallback = callback;
        return this;
    }

    @Override
    protected void init()
    {
        int questionWidth = client.textRenderer.getWidth(question);
        int questionX = (width - questionWidth) >> 1;
        
        List<ButtonWidget> buttons = new ArrayList<>(3);
        buttons.add(new ButtonNoHotkey(0, 0, 1, 20, confirmMessage, button ->
        {
            client.setScreen(parent);
            confirmCallback.run();
        }));
        if (denyCallback != null) buttons.add(new ButtonNoHotkey(0, 0, 1, 20, denyMessage, button ->
        {
            client.setScreen(parent);
            denyCallback.run();
        }));
        if (cancelCallback != null) buttons.add(new ButtonNoHotkey(0, 0, 1, 20, cancelMessage, button ->
        {
            client.setScreen(parent);
            cancelCallback.run();
        }));

        if (buttons.size() == 1)
        {
            questionY = (height - 34) >> 1;

            ButtonWidget button = buttons.get(0);
            button.setWidth(questionWidth >> 1);
            button.x = questionX + (questionWidth - button.getWidth()) >> 1;
            button.y = questionY + 14;
        }
        else if (buttons.size() == 2)
        {
            questionY = (height - 34) >> 1;

            ButtonWidget button = buttons.get(0);
            button.setWidth((questionWidth >> 1) - 2);
            button.x = questionX;
            button.y = questionY + 14;

            button = buttons.get(1);
            button.setWidth((questionWidth >> 1) - 2);
            button.x = questionX + button.getWidth() + 4;
            button.y = questionY + 14;
        }
        else
        {
            questionY = (height - 58) >> 1;

            ButtonWidget button = buttons.get(0);
            button.setWidth((questionWidth >> 1) - 2);
            button.x = questionX;
            button.y = questionY + 14;

            button = buttons.get(1);
            button.setWidth((questionWidth >> 1) - 2);
            button.x = questionX + button.getWidth() + 4;
            button.y = questionY + 14;

            button = buttons.get(2);
            button.setWidth(questionWidth);
            button.x = questionX;
            button.y = questionY + 38;
        }
        for (ButtonWidget button : buttons) addDrawableChild(button);
    }
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
    {
        fill(matrices, 0, 0, width, height, 0xB0000000);
        super.render(matrices, mouseX, mouseY, delta);
        drawCenteredTextWithShadow(matrices, client.textRenderer, question, width >> 1, questionY, 0xFFFFFFFF);
    }
}
