package keystone.core.gui.widgets.inputs;

import keystone.core.gui.WidgetDisabler;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class LabeledDropdownWidget<T> extends ButtonNoHotkey
{
    private final MinecraftClient mc;
    private final TextRenderer font;
    private final BiConsumer<ClickableWidget, Boolean> addDropdown;
    private final Text name;
    private WidgetDisabler widgetDisabler;

    private boolean built;
    private T value;
    private Dropdown<T> dropdown;

    public LabeledDropdownWidget(Text name, int x, int y, int width, T value, BiConsumer<ClickableWidget, Boolean> addDropdown)
    {
        super(x, y, width, getFinalHeight(), name, (button) ->
        {
            LabeledDropdownWidget<?> widget = (LabeledDropdownWidget<?>)button;
            widget.widgetDisabler.disableAll();
            widget.dropdown.y = widget.y + widget.getDropdownOffset() + 20;
            widget.dropdown.visible = true;
        });

        this.mc = MinecraftClient.getInstance();
        this.font = mc.textRenderer;
        this.value = value;
        this.addDropdown = addDropdown;
        this.name = name;
        this.widgetDisabler = new WidgetDisabler();

        if (autoBuild()) build();
    }

    public abstract void buildOptionsList(List<Dropdown.Option<T>> options);

    public int getDropdownOffset() { return 11; }
    public static int getFinalHeight()
    {
        return 31;
    }

    protected final void build()
    {
        if (!built)
        {
            built = true;

            List<Dropdown.Option<T>> optionsList = new ArrayList<>();
            buildOptionsList(optionsList);

            widgetDisabler.restoreAll();
            this.dropdown = new Dropdown<>(x, y + getDropdownOffset() + 20, width, getMessage(), option ->
            {
                setMessage(option.label());
                onSetValue(option.value());
                this.value = option.value();

                widgetDisabler.restoreAll();
                dropdown.visible = false;
            }, optionsList);
            this.dropdown.setSelectedOption(this.value, false);
            widgetDisabler = new WidgetDisabler(dropdown);

            setMessage(this.dropdown.getSelectedOption().label());
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
        drawCenteredText(matrixStack, font, name, x + width / 2, y, 0xFFFFFF);
        matrixStack.push();
        y += getDropdownOffset();
        height -= getDropdownOffset();
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        height += getDropdownOffset();
        y -= getDropdownOffset();
        matrixStack.pop();
    }

    public T getValue() { return value; }
}
