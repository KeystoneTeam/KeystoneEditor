package keystone.core.modules.filter;

import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.core.modules.filter.remapper.FilterRemapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.InternalCompilerException;
import org.codehaus.janino.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilterCompiler
{
    public static KeystoneFilter compileFilter(File filterFile)
    {
        // Randomize Class Name
        String oldClassName = KeystoneFilter.getFilterName(filterFile, true);
        String newClassName = createRandomClassName();

        try
        {
            // Read the filter code and remap any calls to Minecraft code
//            String filterCode = new FilterRemapper(filterFile).remap();
            String filterCode = String.join(System.lineSeparator(), Files.readAllLines(filterFile.toPath()));
            
            // Replace filter name with randomized name
            filterCode = filterCode.replaceAll(oldClassName, newClassName);
            
            // Add default filter imports
            FilterImports.Result imports = FilterImports.getImports(filterCode);
            filterCode = imports.newCode;

            try
            {
                // Create a Janino scanner and compiler
                Scanner scanner = new Scanner(filterFile.getName(), new StringReader(filterCode));
                SimpleCompiler compiler = new SimpleCompiler();
                compiler.setTargetVersion(8);
                compiler.setParentClassLoader(KeystoneFilter.class.getClassLoader());
                
                // Compile the filter code
                compiler.cook(scanner);
                ClassLoader classLoader = compiler.getClassLoader();

                try
                {
                    // Get the raw compiled filter class
                    Class<?> loadedClass = Class.forName(newClassName, true, classLoader);
                    try
                    {
                        // Get the compiled filter class as a KeystoneFilter subclass
                        Class<? extends KeystoneFilter> filterClass = loadedClass.asSubclass(KeystoneFilter.class);
                        
                        // Instantiate and return an instance of the filter
                        return filterClass.newInstance().setName(KeystoneFilter.getFilterName(filterFile, false)).compiledSuccessfully();
                    }
                    catch (ClassCastException e)
                    {
                        String error = "Class '" + oldClassName + "' does not extend KeystoneFilter!";
                        Keystone.LOGGER.error(error);
                        sendErrorMessage(error);
                        return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterFile, false)).setCompilerException(e);
                    }
                    catch (IllegalAccessException e)
                    {
                        String error = "Cannot access filter constructor! Ensure the filter has a public zero-argument constructor.";
                        Keystone.LOGGER.error(error);
                        sendErrorMessage(error);
                        return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterFile, false)).setCompilerException(e);
                    }
                    catch (InstantiationException e)
                    {
                        String error = "Error instantiating filter '" + oldClassName + "'!";
                        Keystone.LOGGER.error(error);
                        e.printStackTrace();
                        sendErrorMessage(error);
                        return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterFile, false)).setCompilerException(e);
                    }
                }
                catch (ClassNotFoundException e)
                {
                    String error = "Unable to find class '" + oldClassName + "'! Make sure your filter class and file share the same name.";
                    Keystone.LOGGER.error(error);
                    sendErrorMessage(error);
                    return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterFile, false)).setCompilerException(e);
                }
            }
            catch (CompileException | InternalCompilerException e)
            {
                Pattern lineNumberPattern = Pattern.compile("Line ([0-9]+)");
                
                // Error Logging
                String error = "Unable to compile filter '" + oldClassName + "': " + e.getLocalizedMessage();
                Matcher matcher = lineNumberPattern.matcher(error);
                String fixedError = error;
                while (matcher.find())
                {
                    String group = matcher.group();
                    int line = Integer.parseInt(group.split(" ")[1]) - imports.lineOffset;
                    fixedError = fixedError.replace(matcher.group(), "Line " + line);
                }
                Keystone.LOGGER.error(fixedError);
                sendErrorMessage(fixedError);
                
                // Remap Exception Line
                String exceptionMessage = e.getMessage();
                matcher = lineNumberPattern.matcher(error);
                while (matcher.find())
                {
                    String group = matcher.group();
                    int line = Integer.parseInt(group.split(" ")[1]) - imports.lineOffset;
                    exceptionMessage = exceptionMessage.replace(matcher.group(), "Line " + line);
                }
                
                return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterFile, false)).setCompilerException(new IllegalArgumentException(exceptionMessage));
            }
        }
        catch (FileNotFoundException e)
        {
            String error = "Filter file '" + filterFile.getPath() + "' not found!";
            Keystone.LOGGER.error(error);
            sendErrorMessage(error);
            return new KeystoneFilter().setName("File Not Found");
        }
        catch (IOException e)
        {
            String error = "Could not read filter file '" + filterFile.getPath() + "'!";
            Keystone.LOGGER.error(error);
            e.printStackTrace();
            sendErrorMessage(error);
            return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterFile, false)).setCompilerException(e);
        }
    }

    private static void sendErrorMessage(String message)
    {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) player.sendMessage(Text.literal(message).styled(style -> style.withColor(Formatting.RED)), false);
    }
    private static String createRandomClassName()
    {
        StringBuilder sb = new StringBuilder();
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        sb.append("Filter_");
        for (int i = 0; i < 32; i++) sb.append(chars[Keystone.RANDOM.nextInt(chars.length)]);

        return sb.toString();
    }
}
