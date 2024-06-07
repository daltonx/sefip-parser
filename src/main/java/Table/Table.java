package Table;

import org.apache.pdfbox.text.TextPosition;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class Table {
    public final String checkHeadSignature = "MDBOT01FIFRSQUJBTEhBRE9SMDFQSVMvUEFTRVAvQ0kwMkFETUlTU8ODTzAzQ0FUMDRPQ09SMDVEQVRBL0NPRCBNT1ZJTUVOVEHDh8ODTzA2Q0JPMTBSRU0gU0VNIDEzwrogU0FMMTFSRU0gMTPCulNBTDEyQkFTRSBDw4FMIDEzwrpTQUwgUFJFViBTT0MxM0NPTlRSSUIgU0VHIERFVklEQTE0REVQw5NTSVRPMTVKQU0yMEJBU0UgQ8OBTCBQUkVWIFNPQ0lBTA==";
    List<Row> headRows = new ArrayList<>();
    public List<HashMap<String, String>> entries = new ArrayList<>();
    private float lastY = 0;
    private float lastBodyY = 0;
    private int entryId = 0;
    private int entryRowId = 0;

    private String getHeadSignature () {
        String signature = "";
        for (int i = 0; i < headRows.size(); i++) {
            Row row = headRows.get(i);
            for (int j = 0; j < row.values.size(); j++) {
                signature += String.format("%d%d%s", i, j, row.values.get(j).value);
            }
        }
        return Base64.getEncoder().encodeToString(signature.getBytes());
    }

    public Boolean isHeadComplete () {
        return getHeadSignature().equals(checkHeadSignature);
    }

    public List<String> getColumnNames () {
        List<String> columns = new ArrayList<>();
        for (Row row: headRows) {
            for (Value col: row.values) {
                columns.add(col.value);
            }
        }
        return columns;
    }

    public void addText (List<TextPosition> textPositions) {
        List<Value> values = new ArrayList<Value>();
        values.add(new Value());

        float lastX = 0;
        int currGroup = 0;
        for (TextPosition text: textPositions) {
            float currY = text.getYDirAdj() + text.getHeightDir();
            if (lastY != 0 && Math.abs(currY - lastY) > 50) {
                return;
            }
            lastY = text.getYDirAdj() + text.getHeightDir();

            float currX = text.getXDirAdj();
            if (lastX != 0 && currX > (lastX + 50))  {
                values.get(currGroup).bakeText();
                currGroup++;
                values.add(new Value());
            }

            values.get(currGroup).cacheText(text);
            lastX = text.getXDirAdj();
        }

        values.get(currGroup).bakeText();

        for (Value value: values) {
            addValue(value);
        }
    }

    private void addValue (Value value) {
        if (!isHeadComplete()) {
            boolean collided = false;
            for (int i = 0; i < headRows.size(); i++) {
                if (headRows.get(i).collides(value)) {
                    headRows.get(i).addColumn(value);
                    collided = true;
                }
            }

            if (headRows.size() == 0 || !collided) {
                Row row = new Row();
                row.addColumn(value);
                headRows.add(row);
            }
        } else {
            if (lastBodyY != 0) {
                float yDiff = Math.abs(value.y - lastBodyY);
                if (yDiff >= 5) {
                    if (
                            entryRowId >= headRows.size() - 1 ||
                            yDiff >= (value.ye - value.y) + 10
                    ) {
                        entryId++;
                        entryRowId = 0;
                    } else {
                        entryRowId++;
                    }
                }
            }

            if (entryRowId == 2) {
                float[] colPos = value.getXPos();

                float[] checkPos = headRows.get(2).values.get(0).getXPos();
                if (Math.max(colPos[0], checkPos[0]) >= Math.min(colPos[1], checkPos[1] + 1)) {
                    entryRowId = 0;
                    entryId++;
                }
            }

            if (entryId > entries.size() - 1)
                entries.add(new HashMap<>());

            HashMap<String, String> entry = entries.get(entryId);
            float[] colPos = value.getXPos();

            String key = "";
            for (Value headCol: headRows.get(entryRowId).values) {
                key = headCol.value;
                if (entry.containsKey(key))
                    continue;

                float[] headColPos = headCol.getXPos();
                if (Math.max(colPos[0], headColPos[0]) <= Math.min(colPos[1], headColPos[1] + 1))
                    break;
            }

            entry.put(key, value.value);
            lastBodyY = value.y;
        }
    }
}
