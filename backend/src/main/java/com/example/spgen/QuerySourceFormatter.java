package com.example.spgen;

/** Formats the embedded input query in generated source. */
final class QuerySourceFormatter {
    private static final int MAX_LINE_LENGTH = 100;

    private QuerySourceFormatter() {}

    static String indentAndWrap(String query, String indent) {
        StringBuilder formatted = new StringBuilder();
        String[] lines = query.split("\\R", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                formatted.append('\n');
            }
            appendWrappedLine(formatted, lines[i], indent);
        }
        return formatted.toString();
    }

    private static void appendWrappedLine(StringBuilder output, String line, String indent) {
        String leadingWhitespace = line.substring(0, leadingWhitespaceLength(line));
        String remaining = line;
        String currentIndent = indent;
        while (currentIndent.length() + remaining.length() > MAX_LINE_LENGTH) {
            int available = MAX_LINE_LENGTH - currentIndent.length();
            BreakPoint breakPoint = lastBreakPoint(remaining, available);
            if (breakPoint.index < 0) {
                break;
            }
            output.append(currentIndent)
                .append(remaining, 0, breakPoint.index).append('\n');
            remaining = remaining.substring(breakPoint.index).stripLeading();
            currentIndent = indent + leadingWhitespace
                + (breakPoint.inLineComment ? "-- " : "");
        }
        output.append(currentIndent).append(remaining);
    }

    private static BreakPoint lastBreakPoint(String line, int limit) {
        char quote = 0;
        boolean inBlockComment = false;
        boolean inLineComment = false;
        BreakPoint last = new BreakPoint(-1, false);
        int end = Math.min(line.length(), limit);
        for (int i = 0; i < end; i++) {
            char ch = line.charAt(i);
            if (inLineComment) {
                if (Character.isWhitespace(ch)) {
                    last = new BreakPoint(i, true);
                }
            } else if (quote != 0) {
                if (ch == quote) {
                    if (i + 1 < line.length() && line.charAt(i + 1) == quote) {
                        i++;
                    } else {
                        quote = 0;
                    }
                }
            } else if (inBlockComment) {
                if (ch == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
                    inBlockComment = false;
                    i++;
                } else if (Character.isWhitespace(ch)) {
                    last = new BreakPoint(i, false);
                }
            } else if (ch == '-' && i + 1 < line.length() && line.charAt(i + 1) == '-') {
                inLineComment = true;
                i++;
            } else if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
                inBlockComment = true;
                i++;
            } else if (ch == '\'' || ch == '"') {
                quote = ch;
            } else if (Character.isWhitespace(ch)) {
                last = new BreakPoint(i, false);
            }
        }
        return last;
    }

    private static int leadingWhitespaceLength(String value) {
        int length = 0;
        while (length < value.length() && Character.isWhitespace(value.charAt(length))) {
            length++;
        }
        return length;
    }

    private static final class BreakPoint {
        private final int index;
        private final boolean inLineComment;

        private BreakPoint(int index, boolean inLineComment) {
            this.index = index;
            this.inLineComment = inLineComment;
        }
    }
}
