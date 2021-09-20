package keystone.core.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class TextDisplayWidget extends Widget
{
    private final List<ITextComponent> lines;
    private final List<IReorderingProcessor> trimmedLines;
    private final FontRenderer font;
    private int background;
    private int padding;
    private int spacing;
    private float scale;

    public TextDisplayWidget(int x, int y, int width, FontRenderer font)
    {
        super(x, y, width, 0, new StringTextComponent("Text Display"));
        this.lines = new ArrayList<>();
        this.trimmedLines = new ArrayList<>();
        this.font = font;
        this.background = 0;
        this.padding = 5;
        this.spacing = 1;
        this.scale = 1.0f;
    }

    public TextDisplayWidget setBackground(int background)
    {
        this.background = background;
        return this;
    }
    public TextDisplayWidget setPadding(int padding)
    {
        this.padding = padding;
        return this;
    }
    public TextDisplayWidget setSpacing(int spacing)
    {
        this.spacing = spacing;
        return this;
    }
    public TextDisplayWidget setTextSize(int textSize)
    {
        this.scale = textSize * 0.1f;
        return this;
    }

    public void clear()
    {
        lines.clear();
        trimmedLines.clear();
        height = 0;
    }
    public void addText(TextFormatting color, String... lines)
    {
        ITextComponent[] components = new ITextComponent[lines.length];
        for (int i = 0; i < lines.length; i++) components[i] = new StringTextComponent(lines[i]).withStyle(color);
        addText(components);
    }
    public void addException(Throwable throwable)
    {
        if (throwable == null) return;

        try (StringWriter stringWriter = new StringWriter())
        {
            try (PrintWriter printWriter = new PrintWriter(stringWriter))
            {
                throwable.printStackTrace(printWriter);
            }

            String[] strings = stringWriter.toString().split(System.lineSeparator());
            ITextComponent[] lines = new ITextComponent[strings.length];
            for (int i = 0; i < strings.length; i++) lines[i] = new StringTextComponent(strings[i].replace("\t", "  ")).withStyle(TextFormatting.RED);
            addText(lines);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void addText(ITextComponent... lines)
    {
        for (ITextComponent line : lines)
        {
            List<IReorderingProcessor> trimmedLines = RenderComponentsUtil.wrapComponents(line, (int)((width - 2 * padding / scale) / scale), font);
            this.trimmedLines.addAll(trimmedLines);
            this.lines.add(line);
        }
        height = (int)(2 * (padding / scale) + trimmedLines.size() * (10 * scale + spacing / scale) - spacing / scale);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        matrixStack.pushPose();
        matrixStack.scale(scale, scale, scale);

        fill(matrixStack, (int)(x / scale), (int)(y / scale), (int)((x + width) / scale), (int)((y + height) / scale), background);

        float y = this.y + padding / scale;
        for (IReorderingProcessor line : trimmedLines)
        {
            font.drawShadow(matrixStack, line, (x + padding / scale) / scale, y / scale, 0xFFFFFFFF);
            y += 10 * scale + spacing / scale;
        }

        matrixStack.popPose();
    }
}
