package keystone.core.gui.widgets.inputs.fields;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.inputs.Dropdown;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumFieldWidget extends ButtonNoHotkey
{
    private final Minecraft mc;
    private final FontRenderer font;
    private final Supplier<Object> instance;
    private final Field field;
    private final String name;

    private Enum value;
    private Dropdown<Enum> dropdown;

    public EnumFieldWidget(Supplier<Object> instance, Field field, String name, int x, int y, int width, Consumer<Widget[]> disableWidgets, Runnable restoreWidgets, BiConsumer<Widget, Boolean> addDropdown) throws IllegalAccessException
    {
        super(x, y + 11, width, 20, new StringTextComponent(name), (button) ->
        {
            EnumFieldWidget paletteWidget = (EnumFieldWidget)button;

            disableWidgets.accept(new Widget[] { paletteWidget.dropdown });
            paletteWidget.dropdown.visible = true;
        });

        this.mc = Minecraft.getInstance();
        this.font = mc.fontRenderer;
        this.instance = instance;
        this.field = field;
        this.name = name;
        this.value = (Enum)field.get(instance.get());

        Class<? extends Enum> enumClass = field.getType().asSubclass(Enum.class);
        this.dropdown = null;
        this.dropdown = new Dropdown<Enum>(x, y + 11, width, new StringTextComponent(name), entry -> new StringTextComponent(AnnotationUtils.getEnumValueName(enumClass.cast(entry))), (entry, title) ->
        {
            try
            {
                Enum newValue = (Enum)enumClass.cast(entry);
                field.set(instance.get(), newValue);
                value = newValue;
                setMessage(title);
            }
            catch (IllegalAccessException e)
            {
                String error = "Cannot set Enum field '" + name + "'!";
                Keystone.LOGGER.error(error);
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                e.printStackTrace();
            }

            restoreWidgets.run();
            dropdown.visible = false;
        }, enumClass.getEnumConstants());
        this.dropdown.setSelectedEntry(this.value, false);
        setMessage(this.dropdown.getSelectedEntryTitle());
        addDropdown.accept(this.dropdown, true);
    }
    public static final int getHeight()
    {
        return 31;
    }

    @Override
    public int getHeightRealms()
    {
        return getHeight();
    }
    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        drawCenteredString(matrixStack, font, name, x + width / 2, y - 11, 0xFFFFFF);
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    }

    public Object getValue() { return value; }
}
