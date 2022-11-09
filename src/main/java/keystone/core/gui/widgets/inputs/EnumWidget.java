package keystone.core.gui.widgets.inputs;

import keystone.api.utils.StringUtils;
import keystone.api.variables.Hide;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.BiConsumer;

public class EnumWidget<T extends Enum<T>> extends LabeledDropdownWidget<T>
{
    public EnumWidget(Text name, int x, int y, int width, T value, BiConsumer<ClickableWidget, Boolean> addDropdown)
    {
        super(name, x, y, width, value, addDropdown);
    }

    @Override
    public void buildOptionsList(List<Dropdown.Option<T>> options)
    {
        Class<? extends Enum> enumClass = getValue().getClass().asSubclass(Enum.class);
        for (Enum<?> test : enumClass.getEnumConstants())
        {
            if (isValueAllowed((T)test))
            {
                Hide hide = AnnotationUtils.getEnumAnnotation(test, Hide.class);
                options.add(new Dropdown.Option<>((T)test, Text.literal(StringUtils.enumCaseToTitleCase(test.name())), hide != null));
            }
        }
    }
}
