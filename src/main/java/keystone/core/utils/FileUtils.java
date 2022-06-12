package keystone.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

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
}
