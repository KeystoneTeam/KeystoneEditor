package keystone.core.modules.schematic_import.providers;

import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.schematic_import.boxes.ImportBoundingBox;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;

public class ImportBoxProvider implements IBoundingBoxProvider<ImportBoundingBox>
{
    private ImportModule importModule;

    public ImportBoxProvider(ImportModule importModule)
    {
        this.importModule = importModule;
    }

    @Override
    public Iterable<ImportBoundingBox> get(DimensionId dimensionId)
    {
        return importModule.getImportBoxes();
    }
}
