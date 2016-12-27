package cz.uhk.stex;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;
import junit.framework.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cz.uhk.stex.util.Function;
import cz.uhk.stex.util.ObservableList;

/**
 *
 */

public class ObservableListTest {
    public static Test suite() {
        return ListTestSuiteBuilder
                .using(new TestStringListGenerator() {
                    @Override
                    public List<String> create(String[] elements) {
                        return new ObservableList<>(MinimalCollection.of(elements));
                    }
                })
                .named("ObservableList")
                .withFeatures(
                        ListFeature.GENERAL_PURPOSE,
                        CollectionFeature.SERIALIZABLE,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionSize.ANY)
                .createTestSuite();
    }

    @org.junit.Test
    public void testMapAdd() {
        ObservableList<Float> base = new ObservableList<>(Arrays.asList(1.0f, 2.0f, 3.0f));
        ObservableList<String> mapped = base.map(new Function<Float, String>() {
            @Override
            public String apply(Float aFloat) {
                return Float.toString(aFloat);
            }
        });
        base.add(4.0f);
        org.junit.Assert.assertEquals(base.get(3).toString(), mapped.get(3));
    }

    @org.junit.Test
    public void testMapRemove() {
        ObservableList<Float> base = new ObservableList<>(Arrays.asList(1.0f, 2.0f, 3.0f));
        ObservableList<String> mapped = base.map(new Function<Float, String>() {
            @Override
            public String apply(Float aFloat) {
                return Float.toString(aFloat);
            }
        });
        base.remove(0);
        org.junit.Assert.assertEquals(mapped.size(), 2);
        org.junit.Assert.assertEquals(mapped.get(0), base.get(0).toString());
        org.junit.Assert.assertEquals(mapped.get(1), base.get(1).toString());
    }

    @org.junit.Test
    public void testFlatMapAdd() {
        ObservableList<Float> base = new ObservableList<>(Arrays.asList(1.0f, 2.0f, 3.0f));
        ObservableList<Integer> mapped = base.flatMap(new Function<Float, ObservableList<Integer>>() {
            @Override
            public ObservableList<Integer> apply(Float aFloat) {
                return new ObservableList<>(Arrays.asList((int) (float) aFloat, (int) (float) aFloat));
            }
        });
        base.add(4.0f);
        org.junit.Assert.assertEquals(base.size() * 2, mapped.size());
        for (int i = 0; i < base.size(); i++) {
            org.junit.Assert.assertEquals((int)(float)base.get(i), (int)mapped.get(i * 2));
        }
    }

    @org.junit.Test
    public void testFlatMapRemove() {
        ObservableList<Float> base = new ObservableList<>(Arrays.asList(1.0f, 2.0f, 3.0f));
        ObservableList<Integer> mapped = base.flatMap(new Function<Float, ObservableList<Integer>>() {
            @Override
            public ObservableList<Integer> apply(Float aFloat) {
                return new ObservableList<>(Arrays.asList((int) (float) aFloat, (int) (float) aFloat));
            }
        });
        base.remove(0);
        org.junit.Assert.assertEquals(base.size() * 2, mapped.size());
        org.junit.Assert.assertEquals(mapped.size(), 4);
        org.junit.Assert.assertEquals((int)mapped.get(0), 2);
        org.junit.Assert.assertEquals((int)mapped.get(1), 2);
        org.junit.Assert.assertEquals((int)mapped.get(2), 3);
        org.junit.Assert.assertEquals((int)mapped.get(3), 3);
    }

    @org.junit.Test
    public void testFlatMapAddInner() {
        ObservableList<Float> base = new ObservableList<>(Arrays.asList(1.0f, 2.0f, 3.0f));
        final List<ObservableList<Integer>> inners = new ArrayList<>();
        ObservableList<Integer> mapped = base.flatMap(new Function<Float, ObservableList<Integer>>() {
            @Override
            public ObservableList<Integer> apply(Float aFloat) {
                ObservableList<Integer> x = new ObservableList<>(Collections.singletonList(7));
                inners.add(x);
                return x;
            }
        });
        for (ObservableList<Integer> inner : inners) {
            inner.add(8);
        }
        org.junit.Assert.assertEquals(base.size() * 2, mapped.size());
        for (int i = 0; i < mapped.size(); i++) {
            org.junit.Assert.assertEquals((int)mapped.get(i), i < mapped.size() / 2 ? 7 : 8);
        }
    }

    @org.junit.Test
    public void testFlatMapRemoveInner() {
        ObservableList<Float> base = new ObservableList<>(Arrays.asList(1.0f, 2.0f, 3.0f));
        final List<ObservableList<Integer>> inners = new ArrayList<>();
        ObservableList<Integer> mapped = base.flatMap(new Function<Float, ObservableList<Integer>>() {
            @Override
            public ObservableList<Integer> apply(Float aFloat) {
                ObservableList<Integer> x = new ObservableList<>(Arrays.asList(7, 8));
                inners.add(x);
                return x;
            }
        });
        for (ObservableList<Integer> inner : inners) {
            inner.remove(0);
        }
        org.junit.Assert.assertEquals(base.size(), mapped.size());
        for (int i = 0; i < mapped.size(); i++) {
            org.junit.Assert.assertEquals((int)mapped.get(i), 8);
        }
    }
}
