package Table;

import com.google.gson.Gson;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Processor {
    private List<String> headColumns;
    private List<HashMap<String, String>> entries = new ArrayList<>();
    private Meta meta;
    private String pdfPath;
    private PDDocument pdf;
    public Processor (String path) {
        pdfPath = path;
    }

    public void loadPdf () throws IOException {
        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists())
            throw new RuntimeException("File not found");
        pdf = Loader.loadPDF(pdfFile);
    }

    public void parseTable () throws IOException {
        Extractor extractor = new Extractor();
        extractor.setSortByPosition(true);

        extractor.writeText(pdf, new OutputStreamWriter(new ByteArrayOutputStream()));
        headColumns = extractor.tables.get(0).getColumnNames();

        meta = extractor.meta;
        for(Table table: extractor.tables) {
            entries.addAll(table.entries);
        }
    }

    public List<String> map2List (HashMap<String, String> entry) {
        List<String> list = new ArrayList<>();
        for(String headColumn: headColumns) {
            list.add(entry.getOrDefault(headColumn, ""));
        }
        return list;
    }

    private String list2Csv (List<String> list) {
        return String.join(",",
                list.stream().map(value ->
                        String.format("\"%s\"", value)
                ).collect(Collectors.toList())
        );
    }

    public String toCsv () {
        List<String> lines = new ArrayList<>();
        lines.add(list2Csv(headColumns));
        for (HashMap<String, String> entry: entries) {
            lines.add(list2Csv(map2List(entry)));
        }
        return String.join("\n", lines);
    }

    private String toJson () {
        return new Gson().toJson(entries);
    }

    public void saveFile (String pathName, boolean json) throws IOException {
        String inputPath = new File(pdfPath).getName();
        File outputPath = new File(Paths.get(pathName, "output_" + inputPath.substring(0, inputPath.length() - 4)).toString());
        outputPath.mkdirs();

        BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(outputPath.getAbsolutePath(), "result." + (json ? "json" : "csv")).toString()));
        writer.write(json ? toJson() : toCsv());
        writer.close();

        BufferedWriter writer2 = new BufferedWriter(new FileWriter(Paths.get(outputPath.getAbsolutePath(), "meta." + (json ? "json" : "csv")).toString()));
        writer2.write(json ? meta.toJson() : meta.toCsv());
        writer2.close();
    }
}
