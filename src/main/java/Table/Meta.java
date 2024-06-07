package Table;

import com.google.gson.Gson;
import org.apache.pdfbox.text.TextPosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Meta {
    public static final String[] dataKeys = new String[]{
            "EMPRESA", "INSCRIÇÃO", "COMP", "COD REC",
            "COD GPS", "FPAS", "OUTRAS ENT", "SIMPLES",
            "RAT", "FAP", "RAT AJUSTADO", "TOMADOR/OBRA",
            "INSCRIÇÃO"
    };
    private float lastY = 0;
    public HashMap<String, String> values = new HashMap<>();
    Value lastDataValue;
    String lastDataKey;

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

    boolean isComplete () {
        for (String key: dataKeys) {
            if (!values.containsKey(key))
                return false;
        }
        return true;
    }

    private float[][] getPos (List<TextPosition> textPositions) {
        float minX = 999999;
        float minY = 999999;
        float maxX = 0;
        float maxY = 0;

        for (TextPosition pos: textPositions) {
            float x = pos.getXDirAdj();
            float y = pos.getYDirAdj();
            minX = Math.min(x, minX);
            minY = Math.min(y, minY);
            maxX = Math.max(x + pos.getWidth(), maxX);
            maxY = Math.max(y + pos.getHeightDir(), maxY);
        }

        return new float[][]{
                {minX, minY}, {maxX, maxY}
        };
    }

    private Boolean valueCollides (Value value) {
        return (
                lastDataValue != null &&
                        Math.max(value.y, lastDataValue.y) <= Math.min(value.ye, lastDataValue.ye) &&
                        Math.abs(value.x - lastDataValue.xe) < 20
        );
    }

    void addValue (Value value) {
        for (String key: Meta.dataKeys) {
            if (value.value.startsWith(key + ":")) {
                lastDataValue = value;
                lastDataKey = key;
            }
        }

        if (valueCollides(value)) {
            values.putIfAbsent(lastDataKey, value.value.trim());
            lastDataValue = null;
            lastDataKey = null;
        }
    }

    public String toJson () {
        return new Gson().toJson(values);
    }

    private String list2Csv (List<String> list) {
        return String.join(",",
                list.stream().map(value ->
                        String.format("\"%s\"", value)
                ).collect(Collectors.toList())
        );
    }

    public String toCsv () {
        List<String> _values = new ArrayList<>();
        for (String key: dataKeys) {
            _values.add(values.getOrDefault(key, ""));
        }
        List<String> lines = new ArrayList<>();
        lines.add(list2Csv(Arrays.stream(dataKeys).toList()));
        lines.add(list2Csv(_values));
        return String.join("\n", lines);
    }
}
