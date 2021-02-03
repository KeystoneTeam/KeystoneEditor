package keystone.core.filters;

import java.util.*;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

public class FilterImports
{
    private static Map<String, List<String>> importMap;

    private static void rebuildPackageList()
    {
        importMap = new HashMap<>();
        scanPackagesIntoTree(true,
                Package.getPackage("keystone.api")
        );
    }
    public static String getImports(String code)
    {
        if (importMap == null) rebuildPackageList();

        StringBuilder importsAdded = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : importMap.entrySet())
        {
            if (code.contains(entry.getKey()))
            {
                for (String _import : entry.getValue())
                {
                    if (!code.contains(_import)) importsAdded.append(_import + System.lineSeparator());
                }
            }
        }
        return importsAdded.toString();
    }

    private static void scanPackagesIntoTree(boolean scanSubPackages, Package... packages)
    {
        List<Class<?>> denestBuffer = new ArrayList<>();

        for (Package loadedPackage : packages)
        {
            Reflections reflections = new Reflections(loadedPackage.getName(), new SubTypesScanner(false));
            Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
            Set<Class<? extends Enum>> enumClasses = reflections.getSubTypesOf(Enum.class);
            for (Class<? extends Enum> enumClass : enumClasses) if (!classes.contains(enumClass)) classes.add(enumClass);

            for (Class<?> clazz : classes)
            {
                // De-nest class name
                denestBuffer.clear();
                Class<?> current = clazz;
                while (current != null)
                {
                    denestBuffer.add(current);
                    current = current.getEnclosingClass();
                }
                StringBuffer denested = new StringBuffer();
                for (int i = denestBuffer.size() - 1; i > 0; i--) denested.append(denestBuffer.get(i).getSimpleName() + ".");
                denested.append(denestBuffer.get(0).getSimpleName());

                // Check if it is in root package
                if (!scanSubPackages)
                {
                    String check = loadedPackage.getName().replace('$', '.') + "." + denested;
                    if (!clazz.getName().contains(check)) continue;
                }

                // Add to map
                if (!importMap.containsKey(clazz.getSimpleName())) importMap.put(clazz.getSimpleName(), new ArrayList<>());
                importMap.get(clazz.getSimpleName()).add("import " + clazz.getPackage().getName() + "." + denested + ";");
            }
        }
    }
}
