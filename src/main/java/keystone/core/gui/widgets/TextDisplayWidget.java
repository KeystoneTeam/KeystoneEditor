package keystone.core.gui.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class TextDisplayWidget extends ClickableWidget
{
    private final List<Text> lines;
    private final List<OrderedText> trimmedLines;
    private final TextRenderer font;
    private int background;
    private int padding;
    private int spacing;
    private float scale;

    public TextDisplayWidget(int x, int y, int width, TextRenderer font)
    {
        super(x, y, width, 0, Text.literal("Text Display"));
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
    public void addText(Formatting color, String... lines)
    {
        Text[] components = new Text[lines.length];
        for (int i = 0; i < lines.length; i++) components[i] = Text.literal(lines[i]).styled(style -> style.withColor(color));
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
            Text[] lines = new Text[strings.length];
            for (int i = 0; i < strings.length; i++) lines[i] = Text.literal(strings[i].replace("\t", "  ")).styled(style -> style.withColor(Formatting.RED));
            addText(lines);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void addText(Text... lines)
    {
        for (Text line : lines)
        {
            List<OrderedText> trimmedLines = ChatMessages.breakRenderedChatMessageLines(line, (int)((width - 2 * padding / scale) / scale), font);
            this.trimmedLines.addAll(trimmedLines);
            this.lines.add(line);
        }
        height = (int)(2 * (padding / scale) + trimmedLines.size() * (10 * scale + spacing / scale) - spacing / scale);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        matrixStack.push();
        matrixStack.scale(scale, scale, scale);

        fill(matrixStack, (int)(x / scale), (int)(y / scale), (int)((x + width) / scale), (int)((y + height) / scale), background);

        float y = this.y + padding / scale;
        for (OrderedText line : trimmedLines)
        {
            font.drawWithShadow(matrixStack, line, (x + padding / scale) / scale, y / scale, 0xFFFFFFFF);
            y += 10 * scale + spacing / scale;
        }

        matrixStack.pop();
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder)
    {

    }
}
