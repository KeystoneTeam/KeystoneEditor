package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.variables.Header;
import keystone.api.variables.Variable;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.core.gui.widgets.ITickableWidget;
import keystone.core.gui.widgets.groups.VerticalLayoutGroup;
import keystone.core.utils.AnnotationUtils;
import keystone.core.utils.Result;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class FieldWidgetList extends VerticalLayoutGroup implements ITickableWidget
{
    //region Widgets
    public static class HeaderWidget extends ClickableWidget
    {
        private final TextRenderer textRenderer;
        private final int textWidth;

        public HeaderWidget(Header header, int x, int y, int width, int topPadding)
        {
            super(x, y, width, topPadding, Text.literal(""));
            this.textRenderer = MinecraftClient.getInstance().textRenderer;
            if (header.value() != null && header.value().trim().length() > 0)
            {
                height += 10;
                setMessage(Text.literal(header.value().trim()));
                textWidth = 10 + textRenderer.getWidth(getMessage());
            }
            else textWidth = 0;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta)
        {
            if (textWidth > 0)
            {
                int lineWidth = (width - textWidth) >> 1;
                context.fill(getX(), getY() + height - 6, getX() + lineWidth, getY() + height - 5, 0xFFFFFFFF);
                context.fill(getX() + lineWidth + textWidth, getY() + height - 6, getX() + width, getY() + height - 5, 0xFFFFFFFF);
                context.drawCenteredTextWithShadow(textRenderer, getMessage(), getX() + width / 2, getY() + height - 10, 0xFFFFFFFF);
            }
            else context.fill(getX(), getY() + height - 6, getX() + width, getY() + height - 5, 0xFFFFFFFF);
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) { }
    }
    //endregion

    protected final Screen parent;
    protected final Class<?> clazz;
    protected final Supplier<Object> instance;
    protected final Field dirtyFlag;
    protected final int intendedWidth;
    protected final int padding;
    protected final BiConsumer<ClickableWidget, ClickableWidget> addDropdown;

    protected int nextWidgetY;

    public FieldWidgetList(Screen screen, Text label, Supplier<Object> instance, int x, int y, int width, int maxHeight, int padding)
    {
        this(screen, label, instance, instance.get().getClass(), x, y, width, maxHeight, padding);
    }
    public FieldWidgetList(Screen screen, Text label, Supplier<Object> instance, Class<?> clazz, int x, int y, int width, int maxHeight, int padding)
    {
        super(x, y, width, maxHeight, padding, label);

        this.parent = screen;
        this.clazz = clazz;
        this.instance = instance;
        this.intendedWidth = width - 2 * padding;
        this.padding = padding;
        this.addDropdown = this::addPinnedWidget;

        Result<Field> dirtyFlagResult = AnnotationUtils.findDirtyFlag(clazz);
        if (dirtyFlagResult.isFailed()) dirtyFlagResult.logFailure();
        this.dirtyFlag = dirtyFlagResult.get();

        rebuild();
    }

    @Override
    public void tick()
    {
        try
        {
            if (this.dirtyFlag != null && this.dirtyFlag.get(this.instance.get()).equals(true))
            {
                clear();
                rebuild();
                this.dirtyFlag.set(this.instance.get(), false);
            }
        }
        catch (IllegalAccessException e)
        {
            Keystone.LOGGER.error("Failed to test dirty flag of FieldWidgetList '" + getMessage().getString() + "'!");
            e.printStackTrace();
        }
    }

    public int getNextWidgetY() { return this.nextWidgetY; }
    public void rebuild()
    {
        this.nextWidgetY = 0;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields)
        {
            Header header = field.getAnnotation(Header.class);
            if (header != null)
            {
                HeaderWidget headerWidget = new HeaderWidget(header, 0, nextWidgetY, intendedWidth, layoutControlledWidgetCount() == 0 ? 0 : 20);
                add(headerWidget);
                nextWidgetY += headerWidget.getHeight() + padding;
            }

            Variable variable = field.getAnnotation(Variable.class);
            if (variable == null) continue;
            String variableName = AnnotationUtils.getFieldName(variable, field);

            try
            {
                field.setAccessible(true);
                nextWidgetY += createVariableEditor(field.getType(), field, variableName, nextWidgetY) + padding;
            }
            catch (SecurityException e)
            {
                String error = "Could not create editor for field '" + variableName + "'!";
                Keystone.LOGGER.error(error);

                MinecraftClient.getInstance().player.sendMessage(Text.literal(error).styled(style -> style.withColor(Formatting.RED)), false);
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                String error = "Could not access field '" + variableName + "'!";
                Keystone.LOGGER.error(error);
                MinecraftClient.getInstance().player.sendMessage(Text.literal(error).styled(style -> style.withColor(Formatting.RED)), false);
                e.printStackTrace();
            }
        }
    }

    private int createVariableEditor(Class<?> type, Field field, String name, int y) throws IllegalAccessException
    {
        //region Block Palette
        if (type == BlockPalette.class)
        {
            add(new BlockPaletteFieldWidget(parent, instance, field, name, 0, y, intendedWidth));
            return BlockPaletteFieldWidget.getFinalHeight();
        }
        //endregion
        //region Block Mask
        else if (type == BlockMask.class)
        {
            add(new BlockMaskFieldWidget(parent, instance, field, name, 0, y, intendedWidth));
            return BlockMaskFieldWidget.getFinalHeight();
        }
        //endregion
        //region Float
        else if (type == float.class)
        {
            add(new FloatFieldWidget(parent, instance, field, name, 0, y, intendedWidth));
            return ParsableTextFieldWidget.getFinalHeight();
        }
        //endregion
        //region Integer
        else if (type == int.class)
        {
            add(new IntegerFieldWidget(parent, instance, field, name, 0, y, intendedWidth));
            return ParsableTextFieldWidget.getFinalHeight();
        }
        //endregion
        //region String
        else if (type == String.class)
        {
            add(new StringFieldWidget(parent, instance, field, name, 0, y, intendedWidth));
            return ParsableTextFieldWidget.getFinalHeight();
        }
        //endregion
        //region Boolean
        else if (type == boolean.class)
        {
            add(new BooleanFieldWidget(parent, instance, field, name, 0, y, intendedWidth));
            return BooleanFieldWidget.getFinalHeight();
        }
        //endregion
        //region Enum
        else if (Enum.class.isAssignableFrom(type))
        {
            add(new EnumFieldWidget(parent, instance, field, name, 0, y, intendedWidth, addDropdown));
            return EnumFieldWidget.getFinalHeight();
        }
        //endregion
        //region Biome
        else if (type == Biome.class)
        {
            add(new BiomeFieldWidget(parent, instance, field, name, 0, y, intendedWidth, addDropdown));
            return BiomeFieldWidget.getFinalHeight();
        }
        //endregion

        return 0;
    }
}
