package keystone.core.modules.schematic_import;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.gui.hotbar.KeystoneHotbar;
import keystone.core.gui.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.overlays.file_browser.OpenFilesScreen;
import keystone.core.gui.overlays.schematics.ImportScreen;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.ImportBoxesHistoryEntry;
import keystone.core.modules.rendering.ghost_blocks.GhostBlocksModule;
import keystone.core.modules.selection.SelectableCuboid;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.SchematicLoader;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ImportModule implements IKeystoneModule
{
    private List<ImportBoundingBox> importBoxes;
    private ImportBoxesHistoryEntry revertHistoryEntry;
    private HistoryModule historyModule;
    private GhostBlocksModule ghostBlocksModule;
    private ImportBoxRenderer renderer;

    public ImportModule()
    {
        importBoxes = Collections.synchronizedList(new ArrayList<>());
        revertHistoryEntry = new ImportBoxesHistoryEntry(importBoxes);
        KeystoneHotbarEvents.CHANGED.register(this::onSlotChanged);
        KeystoneLifecycleEvents.IMPORTS_CHANGED.register(this::onImportsChanged);
    }

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
        clearImportBoxes(false, false);
        revertHistoryEntry = new ImportBoxesHistoryEntry(importBoxes);
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
        if (previous == KeystoneHotbarSlot.IMPORT && slot != KeystoneHotbarSlot.IMPORT && slot != KeystoneHotbarSlot.CLONE) clearImportBoxes(true, true);
    }
    private void onImportsChanged(List<ImportBoundingBox> importBoxes, boolean createHistoryEntry)
    {
        if (createHistoryEntry) addHistoryEntry();
    }
    //endregion
    //region Import Calls
    public void promptImportSchematic(Vec3i minPosition)
    {
        OpenFilesScreen.openFiles(Text.literal("Import Schematics"), SchematicLoader.getExtensions(),
                KeystoneDirectories.getSchematicsDirectory(), true, (files) ->
        {
            for (File schematic : files) importSchematic(schematic, minPosition, false, false);
            KeystoneLifecycleEvents.IMPORTS_CHANGED.invoker().importsChanged(this.importBoxes, files.length > 0);
            if (files.length == 0) KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
        });
    }
    public void importSchematic(String path, Vec3i minPosition, boolean raiseEvent, boolean createHistoryEntry)
    {
        KeystoneSchematic schematic = SchematicLoader.loadSchematic(path);
        importSchematic(schematic, minPosition, raiseEvent, createHistoryEntry);
    }
    public void importSchematic(File file, Vec3i minPosition, boolean raiseEvent, boolean createHistoryEntry)
    {
        KeystoneSchematic schematic = SchematicLoader.loadSchematic(file);
        importSchematic(schematic, minPosition, raiseEvent, createHistoryEntry);
    }
    public void importSchematic(KeystoneSchematic schematic, Vec3i minPosition, boolean raiseEvent, boolean createHistoryEntry)
    {
        this.importBoxes.add(ImportBoundingBox.create(minPosition, schematic));
        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.IMPORT);
        ImportScreen.open();
        if (raiseEvent) KeystoneLifecycleEvents.IMPORTS_CHANGED.invoker().importsChanged(this.importBoxes, createHistoryEntry);
    }
    //endregion
    //region Import Box Functions
    public List<ImportBoundingBox> getImportBoxes() { return importBoxes; }
    public void setImportBoxes(List<ImportBoundingBox> boxes, boolean raiseEvent, boolean createHistoryEntry)
    {
        clearImportBoxes(false, false);
        boxes.forEach(box -> importBoxes.add(box));
        if (raiseEvent) KeystoneLifecycleEvents.IMPORTS_CHANGED.invoker().importsChanged(this.importBoxes, createHistoryEntry);

        if (importBoxes.size() > 0) KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.IMPORT);
        else KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
    }
    public void addCloneImportBoxes(KeystoneSchematic schematic, Vec3i minPosition, BlockRotation rotation, BlockMirror mirror, Vector3i offset, int repeat, int scale)
    {
        clearImportBoxes(false, false);

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

        KeystoneLifecycleEvents.IMPORTS_CHANGED.invoker().importsChanged(this.importBoxes, false);
    }
    public void rotateAll()
    {
        if (importBoxes.size() == 0) return;

        for (ImportBoundingBox importBox : importBoxes) importBox.cycleRotate();
        KeystoneLifecycleEvents.IMPORTS_CHANGED.invoker().importsChanged(importBoxes, true);
    }
    public void mirrorAll()
    {
        if (importBoxes.size() == 0) return;

        for (ImportBoundingBox importBox : importBoxes) importBox.cycleMirror();
        KeystoneLifecycleEvents.IMPORTS_CHANGED.invoker().importsChanged(importBoxes, true);
    }
    public void nudgeAll(Direction direction, int amount)
    {
        if (importBoxes.size() == 0) return;

        for (ImportBoundingBox importBox : importBoxes) importBox.nudgeBox(direction, amount);
    }
    public void setScaleAll(int scale)
    {
        if (importBoxes.size() == 0) return;

        for (ImportBoundingBox importBox : importBoxes) importBox.setScale(scale);
        KeystoneLifecycleEvents.IMPORTS_CHANGED.invoker().importsChanged(importBoxes, true);
    }
    public void placeAll(Map<Identifier, Boolean> extensionsToPlace, boolean copyAir, boolean raiseEvent, boolean createHistoryEntry)
    {
        if (importBoxes.size() == 0) return;
        SelectionModule selectionModule = Keystone.getModule(SelectionModule.class);

        historyModule.tryBeginHistoryEntry();
        {
            // Place Import Boxes
            importBoxes.forEach(importBox -> importBox.place(extensionsToPlace, copyAir));

            // Create selection box for each import box
            List<BoundingBox> boxes = new ArrayList<>(importBoxes.size());
            importBoxes.forEach(box -> boxes.add(box.getBoundingBox()));
            selectionModule.setSelections(boxes);

            // Clear import boxes
            clearImportBoxes(raiseEvent, createHistoryEntry);
        }
        historyModule.tryEndHistoryEntry();

        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
    }
    public void clearImportBoxes(boolean raiseEvent, boolean createHistoryEntry)
    {
        if (importBoxes.size() == 0) return;

        importBoxes.forEach(importBox -> ghostBlocksModule.releaseWorld(importBox.getGhostBlocks()));
        importBoxes.clear();

        if (raiseEvent) KeystoneLifecycleEvents.IMPORTS_CHANGED.invoker().importsChanged(this.importBoxes, createHistoryEntry);
    }
    public void addHistoryEntry()
    {
        historyModule.tryBeginHistoryEntry();

        ImportBoxesHistoryEntry entry = new ImportBoxesHistoryEntry(importBoxes);
        historyModule.pushToEntry(entry, revertHistoryEntry);
        revertHistoryEntry = entry;

        historyModule.tryEndHistoryEntry();
    }
    //endregion
}
