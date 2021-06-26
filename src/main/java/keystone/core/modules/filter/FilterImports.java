package keystone.core.modules.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        imports.add("keystone.api");
        imports.add("keystone.api.enums");
        imports.add("keystone.api.filters");
        imports.add("keystone.api.tools");
        imports.add("keystone.api.tools.interfaces");
        imports.add("keystone.api.utils");
        imports.add("keystone.api.variables");
        imports.add("keystone.api.wrappers");

        imports.add("java.util");
        imports.add("java.lang");
    }

    public static Result getImports(String code)
    {
        int offset = 0;
        StringBuilder codeBuilder = new StringBuilder();
        for (String importPackage : imports)
        {
            codeBuilder.append("import ");
            codeBuilder.append(importPackage);
            codeBuilder.append(".*;");
            codeBuilder.append(System.lineSeparator());
            offset++;
        }

        try(Scanner scanner = new Scanner(code))
        {
            Pattern importPattern = Pattern.compile("import (\\w+(?:\\.\\w+)*);");
            while (scanner.hasNext())
            {
                String codeLine = scanner.nextLine();
                StringBuffer codeLineBuffer = new StringBuffer(codeLine);

                if (codeLine.contains("import"))
                {
                    Matcher matcher = importPattern.matcher(codeLine);
                    while (matcher.find())
                    {
                        String importStatement = matcher.group();
                        boolean remove = false;
                        for (String importPackage : imports)
                        {
                            if (importStatement.contains(importPackage))
                            {
                                remove = true;
                                break;
                            }
                        }
                        if (remove)
                        {
                            int start = matcher.start();
                            int end = matcher.end();
                            codeLineBuffer.replace(start, end, " ");
                        }
                    }
                    codeLine = codeLineBuffer.toString().trim();
                    if (codeLine.length() > 0)
                    {
                        codeBuilder.append(codeLine);
                        if (!codeLine.endsWith(System.lineSeparator())) codeBuilder.append(System.lineSeparator());
                    }
                    else offset--;
                }
                else
                {
                    codeBuilder.append(codeLine);
                    if (!codeLine.endsWith(System.lineSeparator())) codeBuilder.append(System.lineSeparator());
                }
            }
        }

        return new Result(codeBuilder.toString(), offset);
    }
}
