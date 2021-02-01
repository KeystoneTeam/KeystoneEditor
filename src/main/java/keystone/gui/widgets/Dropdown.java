package keystone.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

import java.util.function.BiConsumer;

public class Dropdown<T> extends Widget
{
    //region Function Interfaces
    public interface Converter<T>
    {
        ITextComponent get(T item);
    }
    //endregion

    private T[] entries;
    private ITextComponent[] entryTitles;
    private BiConsumer<T, ITextComponent> onSelectedEntryChanged;

    private T selectedEntry;
    private ITextComponent selectedEntryTitle;

    public Dropdown(int x, int y, int width, int height, ITextComponent title, Converter<T> titleConverter, BiConsumer<T, ITextComponent> onSelectedEntryChanged, T... entries)
    {
        super(x, y, width, height, title);
        this.visible = false;

        this.entries = entries;
        this.entryTitles = new ITextComponent[entries.length];
        for (int i = 0; i < entries.length; i++) this.entryTitles[i] = titleConverter.get(entries[i]);
        this.onSelectedEntryChanged = onSelectedEntryChanged;

        this.selectedEntry = entries[0];
        this.selectedEntryTitle = entryTitles[0];
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer font = minecraft.fontRenderer;

        // Draw Elements
        int hoveredElement = -1;
        if (isHovered()) hoveredElement = (mouseY - this.y) / 11;
        for (int i = 0; i < entries.length; i++)
        {
            T entry = entries[i];
            ITextComponent title = entryTitles[i];
            if (hoveredElement == i)
            {
                fill(matrixStack, this.x, this.y + i * 11, this.x + this.width, this.y + (i + 1) * 11, 0xFFFFFFFF);
                int color = (title.getStyle().getColor() != null) ? title.getStyle().getColor().getColor() : 0x808080;
                minecraft.fontRenderer.drawString(matrixStack, title.getString(), this.x + 1, this.y + i * 11 + 1, color);
            }
            else
            {
                fill(matrixStack, this.x, this.y + i * 11, this.x + this.width, this.y + (i + 1) * 11, 0xFF808080);
                int color = (title.getStyle().getColor() != null) ? title.getStyle().getColor().getColor() : 0xFFFFFF;
                minecraft.fontRenderer.drawString(matrixStack, title.getString(), this.x + 1, this.y + i * 11 + 1, color);
            }
        }
    }
    @Override
    public void onClick(double mouseX, double mouseY)
    {
        int hoveredElement = hoveredElement = ((int)mouseY - this.y) / 10;
        if (hoveredElement >= 0 && hoveredElement < entries.length)
        {
            visible = false;
            selectedEntry = entries[hoveredElement];
            selectedEntryTitle = entryTitles[hoveredElement];
            onSelectedEntryChanged.accept(selectedEntry, selectedEntryTitle);
        }
    }

    public T getSelectedEntry() { return selectedEntry; }
    public ITextComponent getSelectedEntryTitle() { return selectedEntryTitle; }
}
