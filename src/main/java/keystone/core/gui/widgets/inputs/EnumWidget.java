package keystone.core.gui.widgets.inputs;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EnumWidget<T extends Enum<T>> extends ButtonNoHotkey
{
    private final Minecraft mc;
    private final FontRenderer font;
    private final Runnable restoreWidgets;
    private final BiConsumer<Widget, Boolean> addDropdown;
    private final ITextComponent name;

    private boolean built;
    private T value;
    private Dropdown<T> dropdown;

    public EnumWidget(ITextComponent name, int x, int y, int width, T value, Consumer<Widget[]> disableWidgets, Runnable restoreWidgets, BiConsumer<Widget, Boolean> addDropdown)
    {
        super(x, y, width, getFinalHeight(), name, (button) ->
        {
            EnumWidget enumWidget = (EnumWidget)button;

            disableWidgets.accept(new Widget[] { enumWidget.dropdown });
            enumWidget.dropdown.y = enumWidget.y + getEnumOffset() + 20;
            enumWidget.dropdown.visible = true;
        });

        this.mc = Minecraft.getInstance();
        this.font = mc.font;
        this.value = value;
        this.restoreWidgets = restoreWidgets;
        this.addDropdown = addDropdown;
        this.name = name;

        if (autoBuild()) build();
    }
    public static int getEnumOffset() { return 11; }
    public static int getFinalHeight()
    {
        return 31;
    }

    protected final void build()
    {
        if (!built)
        {
            built = true;

            Class<? extends Enum> enumClass = value.getClass().asSubclass(Enum.class);
            List<T> valuesList = new ArrayList<>();
            for (Enum test : enumClass.getEnumConstants()) if (isValueAllowed((T)test)) valuesList.add((T)test);

            this.dropdown = null;
            this.dropdown = new Dropdown<>(x, y + getEnumOffset() + 20, width, getMessage(), entry -> new StringTextComponent(AnnotationUtils.getEnumValueName(entry)), (entry, title) ->
            {
                setMessage(title);
                onSetValue(entry);
                this.value = entry;

                restoreWidgets.run();
                dropdown.visible = false;
            }, valuesList);
            this.dropdown.setSelectedEntry(this.value, false);
            setMessage(this.dropdown.getSelectedEntryTitle());
            addDropdown.accept(this.dropdown, true);
        }
    }

    protected boolean autoBuild() { return true; }
    protected boolean isValueAllowed(T value) { return true; }
    protected void onSetValue(T value) {  }

    @Override
    public int getHeight()
    {
        return getFinalHeight();
    }
    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        drawCenteredString(matrixStack, font, name, x + width / 2, y, 0xFFFFFF);
        matrixStack.pushPose();
        y += getEnumOffset();
        height -= getEnumOffset();
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        height += getEnumOffset();
        y -= getEnumOffset();
        matrixStack.popPose();
    }

    public T getValue() { return value; }
}
