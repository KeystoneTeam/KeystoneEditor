package keystone.core.gui.widgets.inputs;

import keystone.api.wrappers.Biome;
import keystone.core.utils.WorldRegistries;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public class BiomeWidget extends LabeledDropdownWidget<Biome>
{
    public BiomeWidget(Text name, int x, int y, int width, Biome value, BiConsumer<ClickableWidget, ClickableWidget> addDropdown)
    {
        super(name, x, y, width, value, addDropdown);
        setSearchable(true);
    }

    @Override
    protected boolean autoBuild()
    {
        return false;
    }

    @Override
    public void buildOptionsList(List<Dropdown.Option<Biome>> options)
    {
        Registry<net.minecraft.world.biome.Biome> biomeRegistry = WorldRegistries.biomes();
        biomeRegistry.getKeys().forEach(biomeKey ->
        {
            Biome biome = new Biome(biomeRegistry.entryOf(biomeKey));
            Text registryName = Text.literal(biomeKey.getValue().toString());
            options.add(new Dropdown.Option<>(biome, registryName, false));
        });
    }

    @Override
    protected Comparator<Dropdown.Option<Biome>> getOptionsListComparator()
    {
        return Comparator.comparing(o -> o.label().getString());
    }
}
