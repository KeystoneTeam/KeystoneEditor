package keystone.core.filters;

import java.util.ArrayList;
import java.util.List;

public class FilterImports
{
    private static List<String> imports;

    private static void rebuildPackageList()
    {
        scanPackagesIntoTree(
                Package.getPackage("keystone.api"),
                Package.getPackage("net.minecraft.state")
        );
    }
    public static String addImportsToCode(String code)
    {
        if (imports == null) rebuildPackageList();

        StringBuilder importsAdded = new StringBuilder();
        for (String test : imports) if (!code.contains(test)) importsAdded.append(test + System.lineSeparator());
        return importsAdded.toString() + System.lineSeparator() + code;
    }

    private static void scanPackagesIntoTree(Package... packages)
    {
        imports = new ArrayList<>();
        Package[] loadedPackages = Package.getPackages();

        for (Package loadedPackage : loadedPackages)
        {
            for (Package test : packages)
            {
                if (loadedPackage.getName().startsWith(test.getName()))
                {
                    imports.add("import " + loadedPackage.getName() + ".*;");
                    break;
                }
            }
        }
    }
}
