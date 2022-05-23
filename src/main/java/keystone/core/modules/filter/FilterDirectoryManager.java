package keystone.core.modules.filter;

import keystone.api.KeystoneDirectories;
import keystone.api.filters.KeystoneFilter;
import keystone.api.utils.StringUtils;
import keystone.core.gui.screens.filters.FilterSelectionScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardWatchEventKinds.*;

public class FilterDirectoryManager
{
    private Thread listenerThread;
    private WatchService watchService;
    private Map<WatchKey, Path> paths;
    private Map<File, KeystoneFilter> compiledFilters;

    //region Creation
    protected FilterDirectoryManager(WatchService watchService)
    {
        this.listenerThread = new Thread(this::startThread, "Filter Directory Manager");
        this.watchService = watchService;
        this.paths = new HashMap<>();
        this.compiledFilters = new HashMap<>();

        loadStockFilters();
        recompileAllFilters();

        this.listenerThread.start();
    }
    public static FilterDirectoryManager create(File... directories)
    {
        try
        {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            FilterDirectoryManager manager = new FilterDirectoryManager(watchService);

            for (File directory : directories)
            {
                Path path = directory.toPath();
                WatchKey key = path.register(watchService, ENTRY_DELETE, ENTRY_MODIFY);
                manager.paths.put(key, path);
            }

            return manager;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    //endregion
    //region Directory Watcher
    private void startThread()
    {
        while (true)
        {
            WatchKey key;
            try { key = watchService.take(); }
            catch (Exception e) { e.printStackTrace(); return; }

            Path directory = paths.get(key);
            for (WatchEvent<?> ev : key.pollEvents())
            {
                WatchEvent.Kind<?> kind = ev.kind();
                if (kind == OVERFLOW) continue;

                WatchEvent<Path> event = (WatchEvent<Path>) ev;
                Path filePath = directory.resolve(event.context());

                if (kind == ENTRY_DELETE) onFileDeleted(filePath);
                else if (kind == ENTRY_MODIFY) onFileModified(filePath);
            }

            boolean valid = key.reset();
            if (!valid) break;
        }
    }
    private void onFileDeleted(Path path)
    {
        compiledFilters.remove(path.toFile());
        FilterSelectionScreen.dirty();
    }
    private void onFileModified(Path path)
    {
        File file = path.toFile();

        VariableContainer variableContainer = new VariableContainer(compiledFilters.get(file));
        KeystoneFilter filter = FilterCompiler.compileFilter(file);
        variableContainer.apply(filter);
        compiledFilters.put(file, filter);

        FilterSelectionScreen.dirty();
    }
    //endregion
    //region Public Functions
    private List<File> stockFilters;
    private void loadStockFilters()
    {
        stockFilters = new ArrayList<>();
        File stockFilterCache = KeystoneDirectories.getStockFilterCache();
        for (File file : stockFilterCache.listFiles()) file.delete();

        try
        {
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
            Collection<Identifier> filterResources = resourceManager.findResources("stock_filters", path -> path.endsWith(".java") || path.endsWith(".filter"));

            for (Identifier filterIdentifier : filterResources)
            {
                Matcher matcher = Pattern.compile("[0-9a-z_\\.]+$").matcher(filterIdentifier.getPath());
                matcher.find();
                String fileName = matcher.group();
                fileName = StringUtils.titleCase(fileName.replace('_', ' '));

                File cacheFile = stockFilterCache.toPath().resolve(fileName).toFile();
                if (!cacheFile.exists()) cacheFile.createNewFile();

                try (Resource filterResource = resourceManager.getResource(filterIdentifier);
                     InputStream filterStream = filterResource.getInputStream();
                     FileOutputStream fileOutputStream = new FileOutputStream(cacheFile))
                {
                    int read;
                    byte[] bytes = new byte[8192];
                    while ((read = filterStream.read(bytes)) != -1) fileOutputStream.write(bytes, 0, read);
                }

                stockFilters.add(cacheFile);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public File[] getInstalledFilters()
    {
        if (stockFilters == null || stockFilters.size() == 0) loadStockFilters();

        List<File> filters = new ArrayList<>();
        File[] customFilters = KeystoneDirectories.getFilterDirectory().listFiles((dir, name) -> name.endsWith(".java") || name.endsWith(".filter"));
        if (customFilters != null) Collections.addAll(filters, customFilters);
        filters.addAll(stockFilters);
        filters.sort(Comparator.comparing(a -> KeystoneFilter.getFilterName(a, true)));

        File[] filtersArray = new File[filters.size()];
        for (int i = 0; i < filtersArray.length; i++) filtersArray[i] = filters.get(i);
        return filtersArray;
    }
    public KeystoneFilter getFilter(File file) { return compiledFilters.getOrDefault(file, null); }
    public void recompileAllFilters()
    {
        compiledFilters.clear();
        for (File file : getInstalledFilters()) compiledFilters.put(file, FilterCompiler.compileFilter(file));
    }
    //endregion
}