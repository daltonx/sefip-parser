package Table;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Extractor extends PDFTextStripper {
    public List<Table> tables = new ArrayList<>();
    private Table table;
    private Boolean writing = false;
    public HashMap<String, String> data = new HashMap<>();
    Meta meta = new Meta();

    public Extractor() throws IOException {
        setSortByPosition(true);
    }

    @Override
    protected void startPage(PDPage page) throws IOException {
        writing = false;
        table = new Table();
        super.startPage(page);
    }

    @Override
    protected void endPage(PDPage page) throws IOException {
        if (table.isHeadComplete())
            tables.add(table);
        super.endPage(page);
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        if(text.equals("NOME TRABALHADOR"))
            writing = true;

        if (writing)
            table.addText(textPositions);
        else if (!meta.isComplete())
            meta.addText(textPositions);
    }
}
