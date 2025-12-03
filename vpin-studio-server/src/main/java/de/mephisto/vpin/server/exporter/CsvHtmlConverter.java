package de.mephisto.vpin.server.exporter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CsvHtmlConverter {

    public static String convertCsvToEnhancedHtml(String csv) throws Exception {
        CSVParser parser = CSVParser.parse(
                new StringReader(csv),
                CSVFormat.DEFAULT
                        .withDelimiter(';')                     // <- important: your CSV uses ';'
                        .withFirstRecordAsHeader()
                        .withIgnoreEmptyLines()
                        .withTrim()
                        .withQuote('"')
                        .withEscape('\\')
                        .withAllowMissingColumnNames()
                        .withIgnoreSurroundingSpaces()
        );

        // Get headers in order
        List<String> headers = parser.getHeaderNames();

        StringBuilder html = new StringBuilder(32_000);
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n");
        html.append("<title>Export</title>\n");
        // DataTables CSS (CDN) - change to embedded if you need offline
        html.append("<link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.13.6/css/jquery.dataTables.min.css\">\n");
        html.append("<link rel=\"stylesheet\" href=\"https://cdn.datatables.net/buttons/2.4.1/css/buttons.dataTables.min.css\">\n");
        html.append("<link rel=\"stylesheet\" href=\"https://cdn.datatables.net/colreorder/1.6.2/css/colReorder.dataTables.min.css\">\n");
        html.append("<style>th{position:sticky;top:0;background:#f2f2f2;}td,th{padding:6px;border:1px solid #ddd;}</style>\n");
        html.append("</head>\n<body>\n");

        // Table start
        html.append("<table id=\"pinballTable\" class=\"display nowrap\" style=\"width:100%\">\n<thead>\n<tr>\n");
        for (String header : headers) {
            html.append("<th>").append(escapeHtml(header)).append("</th>\n");
        }
        html.append("</tr>\n</thead>\n<tbody>\n");

        // Rows: iterate records and write cell by index relative to headers length
        for (CSVRecord record : parser) {
            html.append("<tr>");
            int cols = headers.size();
            for (int i = 0; i < cols; i++) {
                String cell = "";
                if (i < record.size()) {
                    // record.get(i) is safe (index-based)
                    cell = record.get(i);
                }
                html.append("<td>").append(escapeHtml(cell)).append("</td>");
            }
            html.append("</tr>\n");
        }

        html.append("</tbody>\n</table>\n");

        // DataTables + buttons JS (CDN)
        html.append("<script src=\"https://code.jquery.com/jquery-3.7.0.min.js\"></script>\n");
        html.append("<script src=\"https://cdn.datatables.net/1.13.6/js/jquery.dataTables.min.js\"></script>\n");
        html.append("<script src=\"https://cdn.datatables.net/colreorder/1.6.2/js/dataTables.colReorder.min.js\"></script>\n");
        html.append("<script src=\"https://cdn.datatables.net/buttons/2.4.1/js/dataTables.buttons.min.js\"></script>\n");
        html.append("<script src=\"https://cdn.datatables.net/buttons/2.4.1/js/buttons.colVis.min.js\"></script>\n");
        html.append("<script src=\"https://cdn.datatables.net/buttons/2.4.1/js/buttons.html5.min.js\"></script>\n");
        html.append("<script src=\"https://cdn.datatables.net/buttons/2.4.1/js/buttons.print.min.js\"></script>\n");
        html.append("<script src=\"https://cdnjs.cloudflare.com/ajax/libs/jszip/3.10.1/jszip.min.js\"></script>\n");


        // Initialize DataTables
        html.append("<script>\n");
        html.append("$(document).ready(function(){\n");
        html.append("  $('#pinballTable').DataTable({\n");
        html.append("    dom: 'Bfrtip',\n");
        html.append("    buttons: ['csv','excel','print', {extend: 'colvis',text: 'Show/Hide Columns'}],\n");
        html.append("    pageLength: 20,\n");
        html.append("    lengthMenu: [10,25,50,100],\n");
        html.append("    scrollX: true,\n");
        html.append("    colReorder: true\n");
        html.append("  });\n");
        html.append("});\n");
        html.append("</script>\n");

        html.append("</body>\n</html>\n");

        return html.toString();
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&': out.append("&amp;"); break;
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '"': out.append("&quot;"); break;
                case '\'': out.append("&#39;"); break;
                default: out.append(c);
            }
        }
        return out.toString();
    }
}

