package co.edu.uptc.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RouteConverterTest {

     @Test
     public void testCapitalizeWords_nullOrEmpty() {
          assertNull(RouteConverter.capitalizeWords(null));
          assertEquals("", RouteConverter.capitalizeWords(""));
     }

     @Test
     public void testCapitalizeWords_basic() {
          assertEquals("Hola Mundo", RouteConverter.capitalizeWords("hola mundo"));
          assertEquals("Hola Mundo", RouteConverter.capitalizeWords("  hola   mundo  "));
          assertEquals("Árbol N Ário", RouteConverter.capitalizeWords("árbol n ário"));
     } 
}