package Table;

import java.util.ArrayList;
import java.util.List;

public class Row {
    public float[] pos;
    private int pageId;
    public List<Value> values = new ArrayList();
    public void addColumn (Value value) {
        values.add(value);
        pageId = value.pageId;
        bakePos(new float[]{value.y, value.ye});
    }

    public void bakePos (float[] pos2) {
        if (pos == null) {
            pos = pos2;
        } else {
            pos = new float[]{
                    Math.min(pos[0], pos2[0]),
                    Math.max(pos[1], pos2[1])
            };
        }
    }

    public boolean collides (Value targetValue) {
        float[] target = new float[]{targetValue.y, targetValue.ye};
        return pageId == targetValue.pageId && (
                (target[0] >= pos[0] && target[0] <= pos[1]) ||
                (pos[0] >= target[0] && pos[0] <= target[1])
        );
    }
}
