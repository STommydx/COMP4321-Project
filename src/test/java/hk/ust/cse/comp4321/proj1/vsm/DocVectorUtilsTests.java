package hk.ust.cse.comp4321.proj1.vsm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class DocVectorUtilsTests {

    @Test
    void simpleVectorAddition() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("janice", 1.);
        v1.put("girl", 3.);
        Map<String, Double> v2 = new HashMap<>();
        v2.put("beautiful", 5.);
        v2.put("girl", 4.);
        Map<String, Double> vSum = DocVectorUtils.add(v1, v2);
        assertEquals(vSum.size(), 3);
        assertEquals(vSum.get("janice"), 1., 1e-6);
        assertEquals(vSum.get("beautiful"), 5., 1e-6);
        assertEquals(vSum.get("girl"), 7., 1e-6);
    }

    @Test
    void simpleVectorElementwiseMultiplication() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("janice", 1.);
        v1.put("girl", 3.);
        Map<String, Double> v2 = new HashMap<>();
        v2.put("beautiful", 5.);
        v2.put("girl", 4.);
        Map<String, Double> vSum = DocVectorUtils.elementwiseProduct(v1, v2);
        assertEquals(vSum.size(), 1);
        assertEquals(vSum.get("girl"), 12., 1e-6);
    }

}
