package co.edu.uptc.controller;

import co.edu.uptc.model.Node;
import co.edu.uptc.model.Station;
import co.edu.uptc.persistence.PersistenceManager;
import co.edu.uptc.persistence.RouteDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests que validan la interacción de RouteTree con el DAO de persistencia
 * usando un mock inyectado en PersistenceManager.
 */
public class RouteTreePersistenceTest {

    private RouteDAO originalDao;
    private RouteDAO mockDao;

    @BeforeEach
    public void setup() {
        // Guardar DAO original y reemplazar por mock
        originalDao = PersistenceManager.getInstance().getRouteDAO();
        mockDao = mock(RouteDAO.class);
        PersistenceManager.getInstance().setRouteDAO(mockDao);
    }

    @AfterEach
    public void teardown() {
        // Restaurar DAO original para no afectar otras pruebas
        PersistenceManager.getInstance().setRouteDAO(originalDao);
    }

    @Test
    public void constructor_loadsFromDao_whenDaoReturnsTree() {
        // Preparar un RouteTree que el DAO devolverá
        RouteTree loaded = new RouteTree();
        // configurar una raíz simple en loaded para que sea comprobable
        loaded.setRoot(new Node(new Station("T1", "TestRoot")));

        when(mockDao.load(ArgumentMatchers.anyString())).thenReturn(loaded);

        // Construir una nueva instancia -> constructor debe llamar load(...) y setear root
        RouteTree tree = new RouteTree();
        assertNotNull(tree.getRoot(), "La raíz debería haberse cargado desde el DAO mockeado");
        assertEquals("TestRoot", tree.getRoot().getStation().getLocation());
        verify(mockDao, atLeastOnce()).load(ArgumentMatchers.anyString());
    }

    @Test
    public void insert_triggersSaveOnDao() {
        // Simular que load devuelve null (archivo no existe)
        when(mockDao.load(ArgumentMatchers.anyString())).thenReturn(null);

        RouteTree tree = new RouteTree(); // root inicial nulo

        // Insertar raíz (parentCode == null) -> debe llamar a save
        boolean inserted = tree.insert(new Station("CEN", "Centro"), null);
        assertTrue(inserted);

        // Verificar que save fue invocado con cualquier RouteTree y algún path
        verify(mockDao, atLeastOnce()).save(ArgumentMatchers.any(RouteTree.class), ArgumentMatchers.anyString());
    }
}