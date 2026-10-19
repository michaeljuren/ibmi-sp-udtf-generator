package com.example.spgen;

import java.util.List;
import java.util.stream.Collectors;

import com.example.spgen.Models.ColumnInfo;
import com.example.spgen.Models.ParamInfo;

/** Template for an SQL user defined table function (SQLUDF) returning the query result. */
public final class FunctionGenerator {
    private FunctionGenerator() {}

    public static String generate(String library, String name, List<ColumnInfo> columns,
                                  List<ParamInfo> params, String query) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE FUNCTION ").append(library.toUpperCase()).append('.')
            .append(name.toUpperCase());
        if (params.isEmpty()) {
            sb.append(" ()\n");
        } else {
            sb.append(" (\n");
            sb.append(params.stream().map(p -> "    " + p.name + " " + p.type)
                .collect(Collectors.joining(",\n")));
            sb.append("\n)\n");
        }
        sb.append("    RETURNS TABLE (\n");
        sb.append(columns.stream().map(c -> "        " + c.name + " " + c.type)
            .collect(Collectors.joining(",\n")));
        sb.append("\n    )\n");
        sb.append("    LANGUAGE SQL\n");
        sb.append("    READS SQL DATA\n");
        sb.append("    NOT DETERMINISTIC\n");
        sb.append("    DISALLOW PARALLEL\n");
        sb.append("RETURN\n");
        sb.append(QuerySourceFormatter.indentAndWrap(query, "    ")).append(";\n");
        return sb.toString();
    }
}
