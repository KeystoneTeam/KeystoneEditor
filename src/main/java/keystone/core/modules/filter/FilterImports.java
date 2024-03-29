package keystone.core.modules.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterImports
{
    public static class Result
    {
        public String newCode;
        public int lineOffset;

        public Result(String newCode, int lineOffset)
        {
            this.newCode = newCode;
            this.lineOffset = lineOffset;
        }
    }

    private static final List<String> imports = new ArrayList<>();
    static
    {
        imports.add("keystone.api.*");
        imports.add("keystone.api.enums.*");
        imports.add("keystone.api.filters.*");
        imports.add("keystone.api.tools.*");
        imports.add("keystone.api.tools.interfaces.*");
        imports.add("keystone.api.utils.*");
        imports.add("keystone.api.variables.*");
        imports.add("keystone.api.wrappers.*");
        imports.add("keystone.api.wrappers.blocks.*");
        imports.add("keystone.api.wrappers.coordinates.*");
        imports.add("keystone.api.wrappers.entities.*");
        imports.add("keystone.api.wrappers.nbt.*");

        imports.add("java.util.*");
        imports.add("java.lang.*");

        imports.add("static keystone.core.utils.ProgressBar.nextStep");
    }

    public static Result getImports(String code)
    {
        int linesAdded = 0;
        StringBuilder codeBuilder = new StringBuilder();
        for (String importPackage : imports)
        {
            codeBuilder.append("import ");
            codeBuilder.append(importPackage);
            codeBuilder.append(';');
            codeBuilder.append(System.lineSeparator());
            linesAdded++;
        }
        
        codeBuilder.append(code);
        return new Result(codeBuilder.toString(), linesAdded);
        
        //StringBuilder codeBuilder = new StringBuilder();
        //for (String importPackage : imports)
        //{
        //    codeBuilder.append("import ");
        //    codeBuilder.append(importPackage);
        //    if (!importPackage.endsWith(";")) codeBuilder.append(".*;");
        //    codeBuilder.append(System.lineSeparator());
        //}
        //
        //try(Scanner scanner = new Scanner(code))
        //{
        //    Pattern importPattern = Pattern.compile("import (\\w+(?:\\.\\w+)*);");
        //    while (scanner.hasNext())
        //    {
        //        String codeLine = scanner.nextLine();
        //        StringBuffer codeLineBuffer = new StringBuffer(codeLine);
        //
        //        if (codeLine.contains("import"))
        //        {
        //            Matcher matcher = importPattern.matcher(codeLine);
        //            while (matcher.find())
        //            {
        //                String importStatement = matcher.group();
        //                boolean remove = false;
        //                for (String importPackage : imports)
        //                {
        //                    if (importStatement.contains(importPackage))
        //                    {
        //                        remove = true;
        //                        break;
        //                    }
        //                }
        //                if (remove)
        //                {
        //                    int start = matcher.start();
        //                    int end = matcher.end();
        //                    codeLineBuffer.replace(start, end, " ");
        //                }
        //            }
        //            codeLine = codeLineBuffer.toString().trim();
        //            if (codeLine.length() > 0)
        //            {
        //                codeBuilder.append(codeLine);
        //                if (!codeLine.endsWith(System.lineSeparator())) codeBuilder.append(System.lineSeparator());
        //            }
        //            else offset--;
        //        }
        //        else
        //        {
        //            codeBuilder.append(codeLine);
        //            if (!codeLine.endsWith(System.lineSeparator())) codeBuilder.append(System.lineSeparator());
        //        }
        //    }
        //}
        //
        //return new Result(codeBuilder.toString(), offset);
    }
}
