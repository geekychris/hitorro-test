package com.hitorro.analysis.text;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins how EmitterElemMapper's and DumpLoadTest's String/Integer/Boolean/Long property keys read
 * JSON values of every type to what hitorro-test 3.0.2 read on Jackson 2. The golden file was
 * produced by running the 3.0.2 classes with the same inputs (generator: hitorro-j3
 * tools/Jackson2HitorroTestGolden.java.txt).
 */
public class Jackson2ParamsCompatTest {

  static final Map<String, String> out = new LinkedHashMap<>();
  static void p(String k, Object v){ out.put(k, String.valueOf(v).replace("\\","\\\\").replace("\n","\\n").replace("\r","\\r")); }
  static String err(Throwable t){ while (t instanceof InvocationTargetException && t.getCause() != null) t = t.getCause(); String n = t.getClass().getName(); return "!" + (n.startsWith("com.fasterxml.jackson") || n.startsWith("tools.jackson") ? "jackson" : t.getClass().getSimpleName()); }
  static final String[] CLASSES = {"com.hitorro.analysis.text.EmitterElemMapper", "com.hitorro.test.typesystem.DumpLoadTest"};
  static final String[] VALUES = {"\"abc\"", "\"42\"", "\"true\"", "\"\"", "7", "-3", "12345678901", "3.7", "1e300", "true", "false", "null", "{\"x\":1}", "[1,2]", "[]", "\"\\u00e9\""};
  static Map<String, String> compute() throws Exception {
    out.clear();
    ObjectMapper om = com.hitorro.util.json.JsonMappers.json();
    for (String cn : CLASSES) {
      Class<?> c = Class.forName(cn, false, Jackson2ParamsCompatTest.class.getClassLoader());
      Field[] fs = c.getDeclaredFields();
      Arrays.sort(fs, Comparator.comparing(Field::getName));
      for (Field f : fs) {
        if (!Modifier.isStatic(f.getModifiers()) || !f.getType().getSimpleName().matches("(String|Integer|Boolean|Long)Property")) continue;
        Object prop;
        try { f.setAccessible(true); prop = f.get(null); } catch (Throwable t) { p(c.getSimpleName() + "." + f.getName(), err(t)); continue; }
        Method apply = null;
        for (Method m : prop.getClass().getMethods()) if (m.getName().equals("apply") && m.getParameterCount() == 1 && m.getParameterTypes()[0].getSimpleName().equals("JsonNode")) apply = m;
        String path = String.valueOf(prop.getClass().getMethod("getPath").invoke(prop));
        String k = c.getSimpleName() + "." + f.getName() + ".";
        p(k + "missing", call(apply, prop, om.readTree("{}")));
        for (int i = 0; i < VALUES.length; i++) {
          p(k + i, call(apply, prop, om.readTree("{\"" + path + "\":" + VALUES[i] + "}")));
        }
      }
    }
    return out;
  }
  static String call(Method m, Object prop, JsonNode n) {
    try { Object v = m.invoke(prop, n); return v == null ? "null" : v.getClass().getSimpleName() + ":" + v; } catch (Throwable t) { return err(t); }
  }

    @Test
    public void paramsReadAsOnJackson2() throws Exception {
        Map<String, String> golden = new LinkedHashMap<>();
        try (InputStream in = getClass().getResourceAsStream("jackson2-params.txt")) {
            for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                if (line.isEmpty()) continue;
                int eq = line.indexOf('=');
                golden.put(line.substring(0, eq), line.substring(eq + 1));
            }
        }
        Map<String, String> got = compute();
        for (Map.Entry<String, String> e : golden.entrySet()) {
            assertEquals(e.getValue(), got.get(e.getKey()), e.getKey());
        }
        assertEquals(golden.keySet(), got.keySet());
    }
}
