package co.edu.uptc.controller;

import co.edu.uptc.model.Station;
import co.edu.uptc.persistence.PersistenceManager;
import co.edu.uptc.persistence.RouteDAO;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RouteTreeEdgeCasesTest {

     private RouteDAO originalDao;
     private RouteDAO mockDao;

     @BeforeEach
     public void setUp() {
          originalDao = PersistenceManager.getInstance().getRouteDAO();
          mockDao = mock(RouteDAO.class);
          when(mockDao.load(anyString())).thenReturn(null); // default no persisted tree
          PersistenceManager.getInstance().setRouteDAO(mockDao);
     }

     @AfterEach
     public void tearDown() {
          PersistenceManager.getInstance().setRouteDAO(originalDao);
     }

     @Test
     public void testInsertDuplicateCodeFails() {
          RouteTree t = new RouteTree();
          assertTrue(t.insert(new Station("A", "A"), null));
          assertFalse(t.insert(new Station("A", "A duplicate"), "A")); // duplicate code
     }

     @Test
     public void testInsertWithNonExistingParentFails() {
          RouteTree t = new RouteTree();
          assertFalse(t.insert(new Station("B", "B"), "NON_EXISTENT"));
     }

     @Test
     public void testSearchShortestPathBFS_nodesMissingReturnNull() {
          RouteTree t = new RouteTree();
          t.insert(new Station("A", "A"), null);
          assertNull(t.searchShortestPathBFS("A","Z")); // Z not present
     }

     @Test
     public void testSaveThrowsPropagation_whenDaoThrows() {
          RouteDAO failingDao = mock(RouteDAO.class);
          doThrow(new RuntimeException("io error")).when(failingDao).save(any(RouteTree.class), anyString());
          PersistenceManager.getInstance().setRouteDAO(failingDao);

          RouteTree t = new RouteTree();
          // insert root should call save and cause RuntimeException to bubble up
          assertThrows(RuntimeException.class, () -> t.insert(new Station("X","X"), null));
     }
}