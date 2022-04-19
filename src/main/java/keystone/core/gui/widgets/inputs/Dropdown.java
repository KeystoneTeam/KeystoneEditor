package keystone.core.gui.widgets.inputs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Dropdown<T> extends ClickableWidget
{
    //region Function Interfaces
    public interface Converter<T>
    {
        Text get(T item);
    }
    //endregion

    private List<T> entries;
    private Text[] entryTitles;
    private BiConsumer<T, Text> onSelectedEntryChanged;

    private T selectedEntry;
    private Text selectedEntryTitle;

    public Dropdown(int x, int y, int width, Text title, Converter<T> titleConverter, BiConsumer<T, Text> onSelectedEntryChanged, T... entries)
    {
        super(x, y, width, entries.length * 12 + 1, title);
        this.visible = false;

        this.entries = new ArrayList<>(entries.length);
        for (T entry : entries) this.entries.add(entry);

        this.entryTitles = new Text[this.entries.size()];
        for (int i = 0; i < this.entries.size(); i++) this.entryTitles[i] = titleConverter.get(this.entries.get(i));
        this.onSelectedEntryChanged = onSelectedEntryChanged;

        this.selectedEntry = this.entries.get(0);
        this.selectedEntryTitle = entryTitles[0];

        this.entries = Collections.unmodifiableList(this.entries);
    }
    public Dropdown(int x, int y, int width, Text title, Converter<T> titleConverter, BiConsumer<T, Text> onSelectedEntryChanged, Collection<T> entries)
    {
        super(x, y, width, entries.size() * 12 + 1, title);
        this.visible = false;

        this.entries = new ArrayList<>(entries.size());
        entries.forEach(entry -> this.entries.add(entry));

        this.entryTitles = new Text[this.entries.size()];
        for (int i = 0; i < this.entries.size(); i++) this.entryTitles[i] = titleConverter.get(this.entries.get(i));
        this.onSelectedEntryChanged = onSelectedEntryChanged;

        this.selectedEntry = this.entries.get(0);
        this.selectedEntryTitle = entryTitles[0];

        this.entries = Collections.unmodifiableList(this.entries);
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        stack.push();
        stack.translate(0, 0, 200);

        MinecraftClient minecraft = MinecraftClient.getInstance();
        TextRenderer font = minecraft.textRenderer;

        fill(stack, this.x, this.y, this.x + width, this.y + 12 * entryTitles.length + 2, 0xFFFFFFFF);

        // Draw Elements
        int hoveredElement = -1;
        if (isHovered()) hoveredElement = (mouseY - this.y - 1) / 12;
        for (int i = 0; i < entryTitles.length; i++)
        {
            Text title = entryTitles[i];
            if (hoveredElement == i)
            {
                fill(stack, this.x + 1, this.y + i * 12 + 1, this.x + this.width - 1, this.y + (i + 1) * 12 + 1, 0xFFFFFFFF);
                int color = (title.getStyle().getColor() != null) ? title.getStyle().getColor().getRgb() : 0x404040;
                font.draw(stack, title.getString(), this.x + 2, this.y + i * 12 + 3, color);
            }
            else
            {
                fill(stack, this.x + 1, this.y + i * 12 + 1, this.x + this.width - 1, this.y + (i + 1) * 12 + 1, 0xFF404040);
                int color = (title.getStyle().getColor() != null) ? title.getStyle().getColor().getRgb() : 0xFFFFFF;
                font.draw(stack, title.getString(), this.x + 2, this.y + i * 12 + 3, color);
            }
        }

        stack.pop();
    }
    @Override
    public void onClick(double mouseX, double mouseY)
    {
        int hoveredElement = ((int)mouseY - this.y - 1) / 12;
        if (hoveredElement >= 0 && hoveredElement < entryTitles.length)
        {
            visible = false;
            selectedEntry = entries.get(hoveredElement);
            selectedEntryTitle = entryTitles[hoveredElement];
            onSelectedEntryChanged.accept(selectedEntry, selectedEntryTitle);
        }
    }
    @Override
    public void appendNarrations(NarrationMessageBuilder builder)
    {
        
    }

    public int size() { return entryTitles.length; }
    public T getEntry(int index) { return entries.get(index); }
    public T getSelectedEntry() { return selectedEntry; }
    public Text getSelectedEntryTitle() { return selectedEntryTitle; }

    public void setSelectedEntry(T entry, boolean raiseEvent) { setSelectedEntry(entry, raiseEvent, T::equals); }
    public void setSelectedEntry(T entry, boolean raiseEvent, BiFunction<T, T, Boolean> equalityFunction)
    {
        for (int i = 0; i < entryTitles.length; i++)
        {
            if (equalityFunction.apply(entry, entries.get(i)))
            {
                visible = false;
                selectedEntry = entries.get(i);
                selectedEntryTitle = entryTitles[i];
                if (raiseEvent) onSelectedEntryChanged.accept(selectedEntry, selectedEntryTitle);
            }
        }
    }
}
