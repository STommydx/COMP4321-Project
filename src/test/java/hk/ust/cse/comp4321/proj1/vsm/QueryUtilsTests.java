package hk.ust.cse.comp4321.proj1.vsm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class QueryUtilsTests {

    @Test
    void simpleVectorL2Norm() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("janice", 1.);
        v1.put("girl", 3.);
        double l2norm = QueryUtils.l2Norm(v1);
        assertEquals(l2norm, Math.sqrt(10.));
    }

    @Test
    void simpleVectorDotProduct() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("janice", 1.);
        v1.put("girl", 3.);
        Map<String, Double> v2 = new HashMap<>();
        v2.put("beautiful", 5.);
        v2.put("girl", 4.);
        double dotProduct = QueryUtils.dot(v1, v2);
        assertEquals(dotProduct, 12., 1e-6);
    }

    @Test
    void simpleCosineSimilarity() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("janice", 1.);
        v1.put("girl", 3.);
        Map<String, Double> v2 = new HashMap<>();
        v2.put("beautiful", 5.);
        v2.put("girl", 4.);
        double similarity = QueryUtils.cosineSimilarity(v1, v2);
        assertEquals(similarity, 12. / Math.sqrt(10) / Math.sqrt(41), 1e-6);
    }
}
