package keystone.core.modules.schematic_import;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.gui.screens.file_browser.OpenFilesScreen;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.screens.schematics.ImportScreen;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.ghost_blocks.GhostBlocksModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.IHistoryEntry;
import keystone.core.modules.history.entries.CloneImportBoxesHistoryEntry;
import keystone.core.modules.history.entries.ImportBoxesHistoryEntry;
import keystone.core.modules.schematic_import.boxes.ImportBoundingBox;
import keystone.core.modules.schematic_import.providers.ImportBoxProvider;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.Coords;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.SchematicLoader;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ImportModule implements IKeystoneModule
{
    public static final Supplier<IHistoryEntry> IMPORT_HISTORY_SUPPLIER = () ->
    {
        ImportModule importModule = Keystone.getModule(ImportModule.class);
        return new ImportBoxesHistoryEntry(importModule.getImportBoxes());
    };

    private Supplier<IHistoryEntry> historyEntrySupplier;
    private List<ImportBoundingBox> importBoxes;
    private HistoryModule historyModule;
    private GhostBlocksModule ghostBlocksModule;

    public ImportModule()
    {
        historyEntrySupplier = IMPORT_HISTORY_SUPPLIER;
        importBoxes = Collections.synchronizedList(new ArrayList<>());
        MinecraftForge.EVENT_BUS.addListener(this::onSlotChanged);
    }

    public IHistoryEntry makeHistoryEntry() { return historyEntrySupplier.get(); }
    public void setHistoryEntrySupplier(@Nonnull Supplier<IHistoryEntry> historyEntrySupplier) { this.historyEntrySupplier = historyEntrySupplier; }

    //region Module Implementation
    @Override
    public boolean isEnabled()
    {
        return KeystoneHotbar.getSelectedSlot() == KeystoneHotbarSlot.IMPORT;
    }
    @Override
    public void postInit()
    {
        this.historyModule = Keystone.getModule(HistoryModule.class);
        this.ghostBlocksModule = Keystone.getModule(GhostBlocksModule.class);
    }
    @Override
    public IBoundingBoxProvider[] getBoundingBoxProviders()
    {
        return new IBoundingBoxProvider[] { new ImportBoxProvider(this)};
    }
    //endregion
    //region Event Handlers
    private void onSlotChanged(final KeystoneHotbarEvent event)
    {
        if (event.previousSlot == KeystoneHotbarSlot.IMPORT && event.slot != KeystoneHotbarSlot.IMPORT) clearImportBoxes(true);
    }
    //endregion
    //region Import Calls
    public void promptImportSchematic(Coords minPosition)
    {
        OpenFilesScreen.openFiles(new StringTextComponent("Import Schematics"), SchematicLoader.getExtensions(),
                KeystoneDirectories.getSchematicsDirectory(), true, (files) ->
        {
            for (File schematic : files) importSchematic(schematic, minPosition);
        });
    }
    public void importSchematic(String path, Coords minPosition)
    {
        KeystoneSchematic schematic = SchematicLoader.loadSchematic(path);
        importSchematic(schematic, minPosition);
    }
    public void importSchematic(File file, Coords minPosition)
    {
        KeystoneSchematic schematic = SchematicLoader.loadSchematic(file);
        importSchematic(schematic, minPosition);
    }
    public void importSchematic(KeystoneSchematic schematic, Coords minPosition)
    {
        IHistoryEntry historyEntry = historyEntrySupplier.get();
        if (historyEntry != null)
        {
            historyModule.tryBeginHistoryEntry();
            historyModule.pushToEntry(historyEntry);
            historyModule.tryEndHistoryEntry();
        }

        this.importBoxes.add(ImportBoundingBox.create(minPosition, schematic));
        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.IMPORT);
        ImportScreen.open();
    }
    public void setCloneImportBoxes(SelectionBoundingBox selection, KeystoneSchematic schematic, Coords minPosition, Rotation rotation, Mirror mirror, Vector3i offset, int repeat, int scale)
    {
        historyModule.tryBeginHistoryEntry();
        historyModule.pushToEntry(new CloneImportBoxesHistoryEntry(selection.getBoundingBox(), schematic, minPosition, rotation, mirror, offset, repeat, scale, false));
        restoreCloneImportBoxes(schematic, minPosition, rotation, mirror, offset, repeat, scale);
        historyModule.tryEndHistoryEntry();
    }
    //endregion
    //region Import Box Functions
    public List<ImportBoundingBox> getImportBoxes() { return importBoxes; }
    public List<ImportBoundingBox> restoreImportBoxes(List<ImportBoundingBox> boxes)
    {
        List<ImportBoundingBox> old = new ArrayList<>();
        importBoxes.forEach(box -> old.add(box));

        clearImportBoxes(false);
        boxes.forEach(box -> importBoxes.add(box));

        if (importBoxes.size() > 0) KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.IMPORT);
        else KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);

        return old;
    }
    public void restoreCloneImportBoxes(KeystoneSchematic schematic, Coords minPosition, Rotation rotation, Mirror mirror, Vector3i offset, int repeat, int scale)
    {
        importBoxes.forEach(importBox -> ghostBlocksModule.releaseWorld(importBox.getGhostBlocks()));
        this.importBoxes.clear();

        int dx = offset.x;
        int dy = offset.y;
        int dz = offset.z;
        for (int i = 1; i <= repeat; i++)
        {
            ImportBoundingBox box = ImportBoundingBox.create(minPosition.add(dx, dy, dz), schematic, rotation, mirror, scale);
            // TODO: Implement Clone Dragging
            //if (i != 1) box.setSelectable(false);
            box.setSelectable(false);
            this.importBoxes.add(box);

            dx += offset.x * scale;
            dy += offset.y * scale;
            dz += offset.z * scale;
        }
    }
    public void rotateAll()
    {
        if (importBoxes.size() == 0) return;

        IHistoryEntry historyEntry = historyEntrySupplier.get();
        if (historyEntry != null)
        {
            historyModule.tryBeginHistoryEntry();
            historyModule.pushToEntry(historyEntry);
            historyModule.tryEndHistoryEntry();
        }

        for (ImportBoundingBox importBox : importBoxes) importBox.cycleRotate();
    }
    public void mirrorAll()
    {
        if (importBoxes.size() == 0) return;

        IHistoryEntry historyEntry = historyEntrySupplier.get();
        if (historyEntry != null)
        {
            historyModule.tryBeginHistoryEntry();
            historyModule.pushToEntry(historyEntry);
            historyModule.tryEndHistoryEntry();
        }

        for (ImportBoundingBox importBox : importBoxes) importBox.cycleMirror();
    }
    public void nudgeAll(Direction direction, int amount)
    {
        if (importBoxes.size() == 0) return;
        for (ImportBoundingBox importBox : importBoxes) importBox.nudgeBox(direction, amount);
    }
    public void setScaleAll(int scale)
    {
        if (importBoxes.size() == 0) return;

        IHistoryEntry historyEntry = historyEntrySupplier.get();
        if (historyEntry != null)
        {
            historyModule.tryBeginHistoryEntry();
            historyModule.pushToEntry(historyEntry);
            historyModule.tryEndHistoryEntry();
        }

        for (ImportBoundingBox importBox : importBoxes) importBox.setScale(scale);
    }
    public void placeAll(Map<ResourceLocation, Boolean> extensionsToPlace)
    {
        if (importBoxes.size() == 0) return;

        Keystone.runOnMainThread(() ->
        {
            SelectionModule selectionModule = Keystone.getModule(SelectionModule.class);

            historyModule.tryBeginHistoryEntry();
            importBoxes.forEach(importBox -> importBox.place(extensionsToPlace));

            List<BoundingBox> boxes = new ArrayList<>(importBoxes.size());
            importBoxes.forEach(box -> boxes.add(box.getBoundingBox()));
            selectionModule.setSelections(boxes);

            clearImportBoxes(true);
            historyModule.tryEndHistoryEntry();

            KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
        });
    }
    public void clearImportBoxes(boolean createHistoryEntry)
    {
        if (importBoxes.size() == 0) return;

        if (createHistoryEntry)
        {
            IHistoryEntry historyEntry = historyEntrySupplier.get();
            if (historyEntry != null)
            {
                historyModule.tryBeginHistoryEntry();
                historyModule.pushToEntry(historyEntry);
                historyModule.tryEndHistoryEntry();
            }
        }

        importBoxes.forEach(importBox -> ghostBlocksModule.releaseWorld(importBox.getGhostBlocks()));
        importBoxes.clear();
    }
    //endregion
}
