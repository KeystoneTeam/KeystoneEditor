package keystone.core.utils;

import keystone.api.Keystone;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

public final class FileUtils
{
    public static void deleteRecursively(File directory, boolean preserveRoot)
    {
        if (!directory.exists()) return;

        if (directory.isDirectory())
        {
            File[] files = directory.listFiles();
            for (File file : files) deleteRecursively(file, false);
            if (!preserveRoot) directory.delete();
        }
        else if (!preserveRoot) directory.delete();
    }
    public static void copyRecursively(Path source, Path target, CopyOption... options) throws IOException
    {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException
            {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    public static DownloadHandle download(URL url, File destination, Runnable finishedCallback)
    {
        Thread downloadThread = new Thread(() ->
        {
            try
            {
                org.apache.commons.io.FileUtils.copyURLToFile(url, destination);
                if (finishedCallback != null) finishedCallback.run();
            }
            catch (IOException e)
            {
                String error = "Failed to download '" + url + "'! " + e.getMessage();
                Keystone.LOGGER.error(error);
                if (MinecraftClient.getInstance().player != null) MinecraftClient.getInstance().player.sendMessage(Text.literal(error).styled(style -> style.withColor(Formatting.RED)));
                e.printStackTrace();
            }
        });
        downloadThread.setName("Keystone File Download");
        downloadThread.start();
        
        return new DownloadHandle(downloadThread);
    }
    public static void extract(File zipFile, Path destinationDirectory, String... folderPath)
    {
        String uri = "jar:" + zipFile.toURI();
        URI zipURI = URI.create(uri);
        try (FileSystem zipFs = FileSystems.newFileSystem(zipURI, new HashMap<>()))
        {
            
            Path pathInZip = zipFs.getPath("/", folderPath);
            Files.walkFileTree(pathInZip, new SimpleFileVisitor<>()
            {
                @Override
                public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException
                {
                    // Make sure that we conserve the hierarchy of files and folders inside the zip
                    Path relativePathInZip = pathInZip.relativize(filePath);
                    Path targetPath = destinationDirectory.resolve(relativePathInZip.toString());
                    Files.createDirectories(targetPath.getParent());
            
                    // And extract the file
                    Files.copy(filePath, targetPath);
            
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e)
        {
            StringBuilder localPath = new StringBuilder("/");
            for (String folder : folderPath) localPath.append(folder).append("/");
            
            String error = "Failed to extract folder '" + localPath + "' from zip file '" + zipFile + "'! " + e.getMessage();
            Keystone.LOGGER.error(error);
            if (MinecraftClient.getInstance().player != null) MinecraftClient.getInstance().player.sendMessage(Text.literal(error).styled(style -> style.withColor(Formatting.RED)));
            e.printStackTrace();
        }
    }
    
    public static class DownloadHandle
    {
        private final Thread downloadThread;
        
        public DownloadHandle(Thread thread)
        {
            this.downloadThread = thread;
        }
        
        public void complete()
        {
            try
            {
                downloadThread.join();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
