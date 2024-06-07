package Table;

import org.apache.pdfbox.text.TextPosition;
import java.util.ArrayList;
import java.util.List;

public class Value {
    public float x;
    public float y;
    public float xe = 0;
    public float ye = 0;
    public String value = "";
    public int pageId;
    private List<TextPosition> textCache = new ArrayList<>();

    public Value() {}

    public void bakeText () {
        float minX = 99999;
        float minY = 99999;
        float maxX = 0;
        float maxY = 0;

        for (TextPosition text: textCache) {
            value += text.getUnicode();
            float cx = text.getXDirAdj();
            float cy = text.getYDirAdj();
            float cxe = cx + text.getWidthDirAdj();
            float cye = cy + text.getHeightDir();

            if (cx < minX)
                minX = cx;
            if (cy < minY)
                minY = cy;
            if (cxe > maxX)
                maxX = cxe;
            if (cye > maxY)
                maxY = cye;
        }

        x = minX;
        y = minY;
        xe = maxX;
        ye = maxY;
    }

    public void cacheText (TextPosition pos) {
        textCache.add(pos);
    }
    public float[] getYPos () {
        return new float[]{y, ye};
    }
    public float[] getXPos () {
        return new float[]{x, xe};
    }
}
