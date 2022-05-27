package keystone.core.modules.schematic_import;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.core.events.keystone.KeystoneHotbarEvents;
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
import keystone.core.modules.selection.SelectableCuboid;
import keystone.core.modules.selection.SelectionBoundingBox;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.SchematicLoader;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
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
    private ImportBoxRenderer renderer;

    public ImportModule()
    {
        historyEntrySupplier = IMPORT_HISTORY_SUPPLIER;
        importBoxes = Collections.synchronizedList(new ArrayList<>());
        KeystoneHotbarEvents.CHANGED.register(this::onSlotChanged);
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
        this.renderer = new ImportBoxRenderer();
    }
    @Override
    public void resetModule()
    {
        clearImportBoxes(false);
    }
    @Override
    public Collection<? extends SelectableCuboid> getSelectableBoxes() { return this.importBoxes; }
    @Override
    public void alwaysRender(WorldRenderContext context)
    {
        KeystoneHotbarSlot slot = KeystoneHotbar.getSelectedSlot();
        if (slot == KeystoneHotbarSlot.CLONE || slot == KeystoneHotbarSlot.IMPORT) this.importBoxes.forEach(box -> renderer.render(context, box));
    }
    //endregion
    //region Event Handlers
    private void onSlotChanged(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot)
    {
        if (previous == KeystoneHotbarSlot.IMPORT && slot != KeystoneHotbarSlot.IMPORT) clearImportBoxes(true);
    }
    //endregion
    //region Import Calls
    public void promptImportSchematic(Vec3i minPosition)
    {
        OpenFilesScreen.openFiles(Text.literal("Import Schematics"), SchematicLoader.getExtensions(),
                KeystoneDirectories.getSchematicsDirectory(), true, (files) ->
        {
            for (File schematic : files) importSchematic(schematic, minPosition);
        });
    }
    public void importSchematic(String path, Vec3i minPosition)
    {
        KeystoneSchematic schematic = SchematicLoader.loadSchematic(path);
        importSchematic(schematic, minPosition);
    }
    public void importSchematic(File file, Vec3i minPosition)
    {
        KeystoneSchematic schematic = SchematicLoader.loadSchematic(file);
        importSchematic(schematic, minPosition);
    }
    public void importSchematic(KeystoneSchematic schematic, Vec3i minPosition)
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
    public void setCloneImportBoxes(SelectionBoundingBox selection, KeystoneSchematic schematic, Vec3i minPosition, BlockRotation rotation, BlockMirror mirror, Vector3i offset, int repeat, int scale)
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
    public void restoreCloneImportBoxes(KeystoneSchematic schematic, Vec3i minPosition, BlockRotation rotation, BlockMirror mirror, Vector3i offset, int repeat, int scale)
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
    public void placeAll(Map<Identifier, Boolean> extensionsToPlace) { placeAll(extensionsToPlace, true); }
    public void placeAll(Map<Identifier, Boolean> extensionsToPlace, boolean copyAir)
    {
        if (importBoxes.size() == 0) return;

        Keystone.runOnMainThread(() ->
        {
            SelectionModule selectionModule = Keystone.getModule(SelectionModule.class);

            historyModule.tryBeginHistoryEntry();
            importBoxes.forEach(importBox -> importBox.place(extensionsToPlace, copyAir));

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
