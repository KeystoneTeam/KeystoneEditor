package keystone.core.gui.widgets.inputs;

import keystone.core.gui.widgets.WidgetList;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Dropdown<T> extends WidgetList
{
    private static final int PADDING = 0;

    public static record Option<T>(T value, Text label) { }
    public static class DropdownOptionButton<T> extends ButtonNoHotkey
    {
        private final Dropdown<T> dropdown;
        private final TextRenderer font;

        public DropdownOptionButton(Dropdown<T> dropdown, Option<T> option)
        {
            super(0, 0, dropdown.width - 2, 12, option.label, button ->
            {
                dropdown.hide();
                dropdown.selectedOption = option;
                dropdown.selectionChangedCallback.accept(option);
            });

            this.dropdown = dropdown;
            this.font = MinecraftClient.getInstance().textRenderer;
        }

        @Override
        public void renderButton(MatrixStack stack, int mouseX, int mouseY, float delta)
        {
            Text label = getMessage();

            if (isHovered())
            {
                fill(stack, this.x, this.y, this.x + this.width, this.y + 12, 0xFFFFFFFF);
                int color = (label.getStyle().getColor() != null) ? label.getStyle().getColor().getRgb() : 0x404040;
                font.draw(stack, label.getString(), this.x + 2, this.y + 2, color);
            }
            else
            {
                fill(stack, this.x, this.y, this.x + this.width, this.y + 12, 0xFF404040);
                int color = (label.getStyle().getColor() != null) ? label.getStyle().getColor().getRgb() : 0xFFFFFF;
                font.draw(stack, label.getString(), this.x + 2, this.y + 3, color);
            }
        }
    }

    private final List<Option<T>> options;
    private final Consumer<Option<T>> selectionChangedCallback;
    private Option<T> selectedOption;
    private boolean searchable;
    private Predicate<Option<T>> search = option -> true;

    public Dropdown(int x, int y, int width, Text title, Consumer<Option<T>> callback, Option<T>... options)
    {
        this(x, y, width, title, callback, List.of(options));
    }
    public Dropdown(int x, int y, int width, Text title, Consumer<Option<T>> callback, Collection<Option<T>> options)
    {
        super(x, y, width, MinecraftClient.getInstance().getWindow().getScaledHeight() - y, PADDING, title);

        this.options = List.copyOf(options);
        this.selectionChangedCallback = callback;
        this.selectedOption = this.options.get(0);

        build();
        hide();
    }
    public Dropdown<T> setSearchable(boolean searchable)
    {
        this.searchable = searchable;
        return this;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        if (visible)
        {
            stack.push();
            stack.translate(0, 0, 200);

            fill(stack, this.x, this.y, this.x + width, this.y + this.height + 2, 0xFFFFFFFF);
            super.render(stack, mouseX, mouseY, partialTicks);

            stack.pop();
        }
    }
    @Override
    protected void updateCurrentWidgets()
    {
        this.maxHeight = MinecraftClient.getInstance().getWindow().getScaledHeight() - y;
        super.updateCurrentWidgets();
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder)
    {
        
    }

    private void build()
    {
        clear();
        options.forEach(option -> { if (search.test(option)) add(new DropdownOptionButton<>(this, option)); });
        setElementsOffset(1, 1);
        bake();
    }

    public boolean isSearchable() { return searchable; }
    public Predicate<Option<T>> getSearch() { return search; }
    public int size() { return options.size(); }
    public Option<T> getOption(int index) { return options.get(index); }
    public Option<T> getSelectedOption() { return selectedOption; }
    public T getValue(int index) { return options.get(index).value; }
    public T getSelectedValue() { return selectedOption.value; }

    public void show()
    {
        visible = true;
        active = true;
    }
    public void hide()
    {
        visible = false;
        active = false;
    }
    public void search(Predicate<Option<T>> search)
    {
        this.search = search != null ? search : option -> true;
        build();
    }

    public void setSelectedOption(T entry, boolean raiseEvent) { setSelectedOption(entry, raiseEvent, T::equals); }
    public void setSelectedOption(T entry, boolean raiseEvent, BiFunction<T, T, Boolean> equalityFunction)
    {
        for (int i = 0; i < options.size(); i++)
        {
            Option<T> option = options.get(i);
            if (equalityFunction.apply(entry, option.value))
            {
                hide();
                selectedOption = option;
                if (raiseEvent) selectionChangedCallback.accept(selectedOption);
            }
        }
    }
}
