package keystone.core.gui.widgets.inputs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Dropdown<T> extends ClickableWidget
{
    public static record Option<T>(T value, Text label) { }

    private List<Option<T>> options;
    private Consumer<Option<T>> selectionChangedCallback;
    private Option<T> selectedOption;

    public Dropdown(int x, int y, int width, Text title, Consumer<Option<T>> callback, Option<T>... options)
    {
        super(x, y, width, options.length * 12 + 1, title);
        this.visible = false;

        this.options = List.of(options);
        this.selectionChangedCallback = callback;
        this.selectedOption = this.options.get(0);
    }
    public Dropdown(int x, int y, int width, Text title, Consumer<Option<T>> callback, Collection<Option<T>> options)
    {
        super(x, y, width, options.size() * 12 + 1, title);
        this.visible = false;

        this.options = List.copyOf(options);
        this.selectionChangedCallback = callback;
        this.selectedOption = this.options.get(0);
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        stack.push();
        stack.translate(0, 0, 200);

        MinecraftClient minecraft = MinecraftClient.getInstance();
        TextRenderer font = minecraft.textRenderer;

        fill(stack, this.x, this.y, this.x + width, this.y + 12 * options.size() + 2, 0xFFFFFFFF);

        // Draw Elements
        int hoveredElement = -1;
        if (isHovered()) hoveredElement = (mouseY - this.y - 1) / 12;
        for (int i = 0; i < options.size(); i++)
        {
            Option<T> option = options.get(i);
            if (hoveredElement == i)
            {
                fill(stack, this.x + 1, this.y + i * 12 + 1, this.x + this.width - 1, this.y + (i + 1) * 12 + 1, 0xFFFFFFFF);
                int color = (option.label.getStyle().getColor() != null) ? option.label.getStyle().getColor().getRgb() : 0x404040;
                font.draw(stack, option.label.getString(), this.x + 2, this.y + i * 12 + 3, color);
            }
            else
            {
                fill(stack, this.x + 1, this.y + i * 12 + 1, this.x + this.width - 1, this.y + (i + 1) * 12 + 1, 0xFF404040);
                int color = (option.label.getStyle().getColor() != null) ? option.label.getStyle().getColor().getRgb() : 0xFFFFFF;
                font.draw(stack, option.label.getString(), this.x + 2, this.y + i * 12 + 3, color);
            }
        }

        stack.pop();
    }
    @Override
    public void onClick(double mouseX, double mouseY)
    {
        int hoveredElement = ((int)mouseY - this.y - 1) / 12;
        if (hoveredElement >= 0 && hoveredElement < options.size())
        {
            visible = false;
            selectedOption = options.get(hoveredElement);
            selectionChangedCallback.accept(selectedOption);
        }
    }
    @Override
    public void appendNarrations(NarrationMessageBuilder builder)
    {
        
    }

    public int size() { return options.size(); }
    public Option<T> getOption(int index) { return options.get(index); }
    public Option<T> getSelectedOption() { return selectedOption; }
    public T getValue(int index) { return options.get(index).value; }
    public T getSelectedValue() { return selectedOption.value; }

    public void setSelectedOption(T entry, boolean raiseEvent) { setSelectedOption(entry, raiseEvent, T::equals); }
    public void setSelectedOption(T entry, boolean raiseEvent, BiFunction<T, T, Boolean> equalityFunction)
    {
        for (int i = 0; i < options.size(); i++)
        {
            Option<T> option = options.get(i);
            if (equalityFunction.apply(entry, option.value))
            {
                visible = false;
                selectedOption = option;
                if (raiseEvent) selectionChangedCallback.accept(selectedOption);
            }
        }
    }
}
