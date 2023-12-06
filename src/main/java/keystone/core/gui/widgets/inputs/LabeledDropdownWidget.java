package keystone.core.gui.widgets.inputs;

import keystone.api.Keystone;
import keystone.core.gui.WidgetDisabler;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class LabeledDropdownWidget<T> extends ButtonNoHotkey
{
    private class LabeledDropdown extends Dropdown<T>
    {
        public LabeledDropdown(int x, int y, int width, Text title, Collection<Option<T>> options)
        {
            super(x, y, width, title, option ->
            {
                LabeledDropdownWidget.this.setMessage(option.label());
                LabeledDropdownWidget.this.onSetValue(option.value());
                LabeledDropdownWidget.this.value = option.value();
                LabeledDropdownWidget.this.widgetDisabler.restoreAll();
                LabeledDropdownWidget.this.dropdown.hide();
            }, options);
        }

        @Override
        public void show()
        {
            super.show();
            if (searchable)
            {
                searchBar.visible = true;
                searchBar.active = true;
                searchBar.setFocused(true);
            }
        }

        @Override
        public void hide()
        {
            super.hide();
            searchBar.visible = false;
            searchBar.active = false;
        }
    }

    private final MinecraftClient mc;
    private final TextRenderer font;
    private final BiConsumer<ClickableWidget, ClickableWidget> addDropdown;
    private final Text name;
    private WidgetDisabler widgetDisabler;

    private boolean searchable;
    private boolean built;
    private T value;

    private TextFieldWidget searchBar;
    private Dropdown<T> dropdown;

    public LabeledDropdownWidget(Text name, int x, int y, int width, T value, BiConsumer<ClickableWidget, ClickableWidget> addDropdown)
    {
        super(x, y, width, getFinalHeight(), name, (button) ->
        {
            LabeledDropdownWidget<?> widget = (LabeledDropdownWidget<?>)button;
            widget.widgetDisabler.disableAll();
            widget.dropdown.setY(widget.getY() + widget.getDropdownOffset() + (widget.searchable ? 12 : 20));

            widget.dropdown.show();
        });

        this.mc = MinecraftClient.getInstance();
        this.font = mc.textRenderer;
        this.value = value;
        this.addDropdown = addDropdown;
        this.name = name;
        this.widgetDisabler = new WidgetDisabler();

        if (autoBuild()) build();
    }
    public LabeledDropdownWidget<T> setSearchable(boolean searchable)
    {
        if (built) Keystone.LOGGER.warn("Trying to call LabeledDropdownWidget.setSearchable after building! This will lead to unused widgets in the queuedWidgets list and should be avoided!");
        this.searchable = searchable;
        built = false;
        build();
        return this;
    }

    protected abstract void buildOptionsList(List<Dropdown.Option<T>> options);
    protected Comparator<Dropdown.Option<T>> getOptionsListComparator() { return null; }
    protected void configureDropdown(Dropdown<T> dropdown) { }

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
            Comparator<Dropdown.Option<T>> comparator = getOptionsListComparator();
            if (comparator != null) optionsList.sort(comparator);

            widgetDisabler.restoreAll();

            searchBar = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, getX() + 1, getY() + 12, width, 12, Text.translatable("keystone.search"));
            searchBar.setMaxLength(256);
            searchBar.setDrawsBackground(true);
            searchBar.setText("");
            searchBar.setChangedListener(str -> dropdown.search(option -> option.label().getString().toLowerCase().contains(str.toLowerCase())));

            this.dropdown = new LabeledDropdown(getX(), getY() + getDropdownOffset() + (searchable ? 12 : 20), width, getMessage(), optionsList);
            this.dropdown.setSelectedOption(this.value, false);
            configureDropdown(dropdown);

            widgetDisabler = new WidgetDisabler(dropdown, searchBar);

            setMessage(this.dropdown.getSelectedOption().label());
            addDropdown.accept(this.dropdown, this);
            if (searchable) addDropdown.accept(this.searchBar, this);
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
    public void renderButton(DrawContext context, int mouseX, int mouseY, float partialTicks)
    {
        context.drawCenteredTextWithShadow(font, name, getX() + width / 2, getY(), 0xFFFFFF);
        context.getMatrices().push();
        setY(getY() + getDropdownOffset());
        height -= getDropdownOffset();
        super.renderButton(context, mouseX, mouseY, partialTicks);
        height += getDropdownOffset();
        setY(getY() - getDropdownOffset());
        context.getMatrices().pop();
    }

    public T getValue() { return value; }
}
