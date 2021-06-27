package keystone.core.gui.screens.file_browser;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class FileBrowserScreen extends Screen
{
    private static final int MARGINS = 2;
    private static final int PADDING = 5;
    private static final int BUTTON_HEIGHT = 20;
    private static TranslationTextComponent CANCEL_LABEL = new TranslationTextComponent("keystone.cancel");
    private static TranslationTextComponent OPEN_LABEL = new TranslationTextComponent("keystone.open");

    //region Data Types
    private class FileStructure
    {
        public File directory;
        public FileStructure parent;
        public List<FileStructure> directories;
        public List<File> files;

        public FileStructure(FileStructure parent, File directory, boolean recursive, Set<String> fileExtensions)
        {
            this.directory = directory;
            this.parent = parent;
            File[] contents = directory.listFiles();
            Arrays.sort(contents, File::compareTo);

            this.directories = new ArrayList<>();
            this.files = new ArrayList<>();
            for (File file : contents)
            {
                if (file.isDirectory() && recursive) directories.add(new FileStructure(this, file, true, fileExtensions));
                else if (file.isFile() && fileExtensions.contains(FilenameUtils.getExtension(file.getName()).toLowerCase())) files.add(file);
            }
        }

        public File getThisDirectory() { return this.directory; }
    }
    private class IndexedButton extends ButtonNoHotkey
    {
        protected int index;

        public IndexedButton(int index, int width, ITextComponent label, Button.IPressable pressable)
        {
            super(MARGINS, MARGINS + index * (BUTTON_HEIGHT + PADDING), width, BUTTON_HEIGHT, label, pressable);
            this.index = index;
        }

        public void setScrollIndex(int minIndex, int maxIndex)
        {
            if (index < minIndex || index > maxIndex)
            {
                this.active = false;
                this.visible = false;
            }
            else
            {
                this.x = panelX + MARGINS;
                this.y = panelY + MARGINS + (index - minIndex) * (BUTTON_HEIGHT + PADDING);
                this.active = true;
                this.visible = true;
            }
        }
    }
    private class FileButton extends IndexedButton
    {
        public File file;
        public boolean selected;

        public FileButton(FileBrowserScreen screen, int index, int width, File file, boolean selected)
        {
            super(index, width, new StringTextComponent(file.getName()), (button) -> screen.selectFile((FileButton) button));

            this.file = file;
            this.selected = selected;
        }

        @Override
        protected int getYImage(boolean hovered)
        {
            hovered = hovered || selected;
            return super.getYImage(hovered);
        }
    }
    private class DirectoryButton extends IndexedButton
    {
        public DirectoryButton(FileBrowserScreen screen, int index, int width, FileStructure fileStructure, boolean parent)
        {
            super(index, width, new StringTextComponent(parent ? "../" : fileStructure.getThisDirectory().getName() + "/"), (button) -> screen.moveToDirectory(fileStructure));
        }
    }
    //endregion

    private FileStructure currentFileStructure;
    private Consumer<File[]> callback;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int filePanelHeight;
    private int scroll;
    private int maxFilesOnScreen;
    private List<IndexedButton> allButtons;
    private List<File> selectedFiles;

    private FileBrowserScreen(ITextComponent prompt, Set<String> fileExtensions, File root, boolean recursive, Consumer<File[]> callback)
    {
        super(prompt);

        this.currentFileStructure = new FileStructure(null, root, recursive, fileExtensions);
        this.callback = callback;
        this.allButtons = new ArrayList<>();
        this.selectedFiles = new ArrayList<>();
        this.scroll = 0;
    }
    public static void openFiles(ITextComponent prompt, Set<String> fileExtensions, File root, boolean recursive, Consumer<File[]> callback)
    {
        Minecraft.getInstance().setScreen(new FileBrowserScreen(prompt, fileExtensions, root, recursive, callback));
    }

    @Override
    protected void init()
    {
        this.panelWidth = minecraft.getWindow().getGuiScaledWidth() / 2;
        int idealPanelHeight = minecraft.getWindow().getGuiScaledHeight() - 100 - 20 - PADDING;
        this.maxFilesOnScreen = idealPanelHeight / (BUTTON_HEIGHT + PADDING);
        this.filePanelHeight = this.maxFilesOnScreen * (BUTTON_HEIGHT + PADDING) - PADDING + 2 * MARGINS;
        this.panelHeight = this.filePanelHeight + MARGINS + 20;
        this.panelX = (minecraft.getWindow().getGuiScaledWidth() - this.panelWidth) / 2;
        this.panelY = 50;
        int buttonWidth = this.panelWidth - 2 * MARGINS;

        this.allButtons.clear();
        int i = 0;
        if (this.currentFileStructure.parent != null) allButtons.add(new DirectoryButton(this, i++, buttonWidth, this.currentFileStructure.parent, true));
        for (FileStructure dir : currentFileStructure.directories) allButtons.add(new DirectoryButton(this, i++, buttonWidth, dir, false));
        for (File file : currentFileStructure.files) allButtons.add(new FileButton(this, i++, buttonWidth, file, selectedFiles.contains(file)));

        Button cancel = new ButtonNoHotkey(this.panelX, this.panelY + this.panelHeight - 20, (buttonWidth - PADDING) / 2, 20, CANCEL_LABEL, (button) ->
        {
            this.callback.accept(new File[0]);
            onClose();
        });
        Button open = new ButtonNoHotkey(this.panelX + cancel.getWidth() + PADDING, this.panelY + this.panelHeight - 20, (buttonWidth - PADDING) / 2, 20, OPEN_LABEL, (button) ->
        {
            File[] files = new File[selectedFiles.size()];
            files = selectedFiles.toArray(files);
            this.callback.accept(files);
            onClose();
        });

        for (IndexedButton button : allButtons) addButton(button);
        addButton(cancel);
        addButton(open);

        updateScroll();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        drawCenteredString(stack, font, title, panelX + panelWidth / 2, panelY - 14, 0xFFFFFFFF);

        fill(stack, panelX, panelY, panelX + panelWidth, panelY + filePanelHeight, 0x80000000);
        if (maxFilesOnScreen < allButtons.size())
        {
            int scrollbarY = panelY + (int)(scroll * (filePanelHeight / (float)allButtons.size()));
            int scrollbarHeight = (int)(maxFilesOnScreen * (filePanelHeight / (float)allButtons.size()));
            fill(stack, panelX + panelWidth, scrollbarY, panelX + panelWidth + 2, scrollbarY + scrollbarHeight, 0xFF808080);
        }
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (maxFilesOnScreen < allButtons.size())
        {
            scroll += (delta < 0) ? 1 : -1;
            int maxScrollSteps = allButtons.size() - maxFilesOnScreen;
            scroll = Math.min(maxScrollSteps, Math.max(0, scroll));
            updateScroll();
            return true;
        }
        else return false;
    }

    private void selectFile(FileButton fileButton)
    {
        fileButton.selected = !fileButton.selected;
        if (fileButton.selected) this.selectedFiles.add(fileButton.file);
        else this.selectedFiles.remove(fileButton.file);
    }
    private void moveToDirectory(FileStructure directory)
    {
        this.scroll = 0;
        this.currentFileStructure = directory;
        init(minecraft, width, height);
    }
    private void updateScroll()
    {
        if (maxFilesOnScreen < buttons.size())
        {
            int maxScrollSteps = allButtons.size() - maxFilesOnScreen;
            scroll = Math.min(maxScrollSteps, Math.max(0, scroll));
        }
        else scroll = 0;

        int minIndex = scroll;
        int maxIndex = scroll + maxFilesOnScreen - 1;
        for (IndexedButton button : allButtons) button.setScrollIndex(minIndex, maxIndex);
    }
}
