package keystone.core.modules.schematic_import;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.gui.screens.file_browser.OpenFilesScreen;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.screens.schematic_import.ImportScreen;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.ghost_blocks.GhostBlocksModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.ImportBoxesHistoryEntry;
import keystone.core.modules.schematic_import.boxes.ImportBoundingBox;
import keystone.core.modules.schematic_import.providers.ImportBoxProvider;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.Coords;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.SchematicLoader;
import net.minecraft.util.Direction;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImportModule implements IKeystoneModule
{
    private List<ImportBoundingBox> importBoxes;
    private HistoryModule historyModule;
    private BlocksModule blocksModule;
    private GhostBlocksModule ghostBlocksModule;

    public ImportModule()
    {
        importBoxes = new ArrayList<>();
        MinecraftForge.EVENT_BUS.addListener(this::onSlotChanged);
        MinecraftForge.EVENT_BUS.addListener(this::onKeyPressed);
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
        this.blocksModule = Keystone.getModule(BlocksModule.class);
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
    private void onKeyPressed(final InputEvent.KeyInputEvent event)
    {
        if (event.getAction() == GLFW.GLFW_PRESS)
        {
            if (event.getKey() == GLFW.GLFW_KEY_ESCAPE) resetModule();
            else if (importBoxes.size() > 0)
            {
                if (event.getKey() == GLFW.GLFW_KEY_ENTER || event.getKey() == GLFW.GLFW_KEY_KP_ENTER) placeAll();
                else if (event.getKey() == GLFW.GLFW_KEY_R) rotateAll();
                else if (event.getKey() == GLFW.GLFW_KEY_M) mirrorAll();
            }
        }
    }
    //endregion
    //region Import Calls
    public void promptImportSchematic(Coords minPosition)
    {
        OpenFilesScreen.openFiles(new StringTextComponent("Import Schematics"), SchematicLoader.getExtensions(),
                KeystoneDirectories.getSchematicsDirectory(), true, (files) ->
        {
            for (File schematic : files) importSchematic(schematic, Player.getHighlightedBlock());
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
        historyModule.tryBeginHistoryEntry();
        historyModule.pushToEntry(new ImportBoxesHistoryEntry(importBoxes));
        historyModule.tryEndHistoryEntry();

        this.importBoxes.add(ImportBoundingBox.create(minPosition, schematic));
        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.IMPORT);
        ImportScreen.open();
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
    public void resetModule()
    {
        clearImportBoxes(true);
        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
    }

    public void rotateAll()
    {
        if (importBoxes.size() == 0) return;

        historyModule.tryBeginHistoryEntry();
        historyModule.pushToEntry(new ImportBoxesHistoryEntry(importBoxes));
        historyModule.tryEndHistoryEntry();

        for (ImportBoundingBox importBox : importBoxes) importBox.cycleRotate();
    }
    public void mirrorAll()
    {
        if (importBoxes.size() == 0) return;

        historyModule.tryBeginHistoryEntry();
        historyModule.pushToEntry(new ImportBoxesHistoryEntry(importBoxes));
        historyModule.tryEndHistoryEntry();

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

        historyModule.tryBeginHistoryEntry();
        historyModule.pushToEntry(new ImportBoxesHistoryEntry(importBoxes));
        historyModule.tryEndHistoryEntry();

        for (ImportBoundingBox importBox : importBoxes) importBox.setScale(scale);
    }
    public void placeAll()
    {
        if (importBoxes.size() == 0) return;

        Keystone.runOnMainThread(() ->
        {
            SelectionModule selectionModule = Keystone.getModule(SelectionModule.class);

            historyModule.tryBeginHistoryEntry();
            importBoxes.forEach(importBox -> importBox.place());

            List<BoundingBox> boxes = new ArrayList<>(importBoxes.size());
            importBoxes.forEach(box -> boxes.add(box.getBoundingBox()));
            selectionModule.setSelections(boxes);

            resetModule();
            historyModule.tryEndHistoryEntry();

            KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
        });
    }
    public void clearImportBoxes(boolean createHistoryEntry)
    {
        if (importBoxes.size() == 0) return;

        if (createHistoryEntry)
        {
            historyModule.tryBeginHistoryEntry();
            historyModule.pushToEntry(new ImportBoxesHistoryEntry(importBoxes));
            historyModule.tryEndHistoryEntry();
        }

        importBoxes.forEach(importBox -> ghostBlocksModule.releaseWorld(importBox.getGhostBlocks()));
        importBoxes.clear();
    }
    //endregion
}
