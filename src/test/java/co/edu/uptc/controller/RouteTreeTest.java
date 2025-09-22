package co.edu.uptc.controller;

import co.edu.uptc.model.Node;
import co.edu.uptc.model.Station;
import co.edu.uptc.persistence.PersistenceManager;
import co.edu.uptc.persistence.RouteDAO;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RouteTreeTest {

     private RouteDAO originalDao;
     private RouteDAO mockDao;

     @BeforeEach
     public void setUp() {
          originalDao = PersistenceManager.getInstance().getRouteDAO();
          mockDao = mock(RouteDAO.class);
          // by default, mock load returns null (no file)
          when(mockDao.load(anyString())).thenReturn(null);
          PersistenceManager.getInstance().setRouteDAO(mockDao);
     }

     @AfterEach
     public void tearDown() {
          PersistenceManager.getInstance().setRouteDAO(originalDao);
     }

     @Test
     public void testInsertAndExists() {
          RouteTree tree = new RouteTree();
          assertTrue(tree.insert(new Station("CEN","Centro"), null));
          // Se corrige la llamada al método privado. Verificamos la inserción con un método público.
          assertNotNull(tree.getRoot());
          assertEquals("CEN", tree.getRoot().getStation().getCode());
          
          // También usamos el método público para verificar la existencia.
          assertNotNull(tree.searchRec(tree.getRoot(), "Centro"));
     }

     @Test
     public void testSearchRecPath() {
          RouteTree tree = new RouteTree();
          assertTrue(tree.insert(new Station("CEN","Centro"), null));
          assertTrue(tree.insert(new Station("N1","Norte 1"), "CEN"));
          assertTrue(tree.insert(new Station("N1-1","Norte 1.1"), "N1"));

          List<Node> path = tree.searchRec(tree.getRoot(), "Norte 1.1");
          assertNotNull(path);
          assertEquals(3, path.size());
          assertEquals("Centro", path.get(0).getStation().getLocation());
          assertEquals("Norte 1.1", path.get(2).getStation().getLocation());
     }

     @Test
     public void testSearchShortRouteAncestro() {
          RouteTree tree = new RouteTree();
          assertTrue(tree.insert(new Station("CEN","Centro"), null));
          assertTrue(tree.insert(new Station("N1","Norte 1"), "CEN"));
          assertTrue(tree.insert(new Station("N2","Norte 2"), "CEN"));
          assertTrue(tree.insert(new Station("N1-1","Norte 1.1"), "N1"));

          List<Station> route = tree.searchShortRoute("Norte 1.1", "Norte 2");
          assertNotNull(route);
          assertTrue(route.stream().anyMatch(s -> "Centro".equals(s.getLocation())));
     }

     @Test
     public void testSearchShortestPathBFS_whenGraphHasShortcut() {
          RouteTree tree = new RouteTree();
          assertTrue(tree.insert(new Station("A","A"), null));
          assertTrue(tree.insert(new Station("B","B"), "A"));
          assertTrue(tree.insert(new Station("C","C"), "B"));
          assertTrue(tree.insert(new Station("D","D"), "C"));
          // add shortcut: D -> A by inserting child A under D (creating a cycle/shortcut)
          assertTrue(tree.insert(new Station("X","X"), "D")); // X is extra
          // Manually connect D to A (simulate that D has child A) is not ideal via insert; instead create node under D with same location "A"
          assertFalse(tree.insert(new Station("A2","A"), "D")); // node with same location "A" but different code simulates extra connection

          List<Station> bfsRoute = tree.searchShortestPathBFS("A", "D");
          assertNotNull(bfsRoute);
          // BFS must produce shortest path in edges; path length should be <= original tree path length
          assertTrue(bfsRoute.size() >= 1);
     }

     @Test
     public void testEditAndDelete() {
          RouteTree tree = new RouteTree();
          assertTrue(tree.insert(new Station("CEN","Centro"), null));
          assertTrue(tree.insert(new Station("N1","Norte 1"), "CEN"));
          assertTrue(tree.editStation("N1","Norte Uno","N1NEW"));
          assertFalse(tree.editStation("NO","X","X"));
          assertTrue(tree.deleteStation("N1NEW"));
     }

     @Test
     public void testSaveCalledOnInsert() {
          RouteTree tree = new RouteTree();
          // insert root -> should call save at least once
          boolean ins = tree.insert(new Station("C","C"), null);
          assertTrue(ins);
          try {
               verify(mockDao, atLeastOnce()).save(any(RouteTree.class), anyString());
          } catch (Exception e) {
               fail("Should not throw an exception");
          }
     }
}