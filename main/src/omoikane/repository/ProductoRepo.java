package omoikane.repository;

import omoikane.producto.Articulo;
import org.synyx.hades.dao.GenericDao;
import org.synyx.hades.dao.Modifying;
import org.synyx.hades.dao.Query;
import org.synyx.hades.domain.Pageable;

import java.util.List;

/**
 * Por default éste repositorio opera con artículos "activos".
 * El campo/atributo "activo" funciona como un flag de productos en funcionamiento, como
 *  un mecanismo de soft-delete.
 * Los métodos que utilizan cualquier artículo o artículos inactivos están explícitamente mencionados.
 *
 * User: octavioruizcastillo
 * Date: 27/07/11
 * Time: 00:58
 */
public interface ProductoRepo extends GenericDao<Articulo, Long>
{
    @Query("FROM Articulo a WHERE codigo = ? AND activo = 1")
    List<Articulo> findByCodigo(String codigo);

    @Query("FROM Articulo a JOIN FETCH a.baseParaPrecio bp LEFT JOIN FETCH a.impuestos im WHERE a.codigo = ? AND a.activo = 1")
    List<Articulo> findCompleteByCodigo(String codigo);

    @Query("FROM Articulo a JOIN FETCH a.baseParaPrecio bp LEFT JOIN FETCH a.impuestos WHERE a.descripcion like ?1 AND a.activo = 1")
    List<Articulo> findByDescripcionLike(String descripcion, Pageable pageable);

    @Query("FROM Articulo a JOIN FETCH a.baseParaPrecio bp JOIN FETCH a.stock s WHERE a.idArticulo = ?1 AND a.activo = 1")
    Articulo findByIdIncludeStock(Long id);

    @Query("FROM Articulo a JOIN FETCH a.codigosAlternos s WHERE a.idArticulo = ?1 AND a.activo = 1")
    Articulo findByIdIncludeCodigos(Long id);

    @Query("SELECT cp.producto FROM CodigoProducto cp WHERE cp.codigo = ?1 AND cp.producto.activo = 1")
    List<Articulo> findByCodigoAlterno(String codigo);

    @Query("SELECT cp.producto FROM CodigoProducto cp JOIN FETCH cp.producto.baseParaPrecio bp LEFT JOIN FETCH cp.producto.impuestos im WHERE cp.codigo = ?1 AND cp.producto.activo = 1")
    List<Articulo> findCompleteByCodigoAlterno(String codigo);

    //Usar éste método en lugar de readAll para mayor eficiencia
    @Query("SELECT a FROM Articulo a JOIN FETCH a.baseParaPrecio WHERE a.activo = 1")
    List<Articulo> findAll();

    @Query("SELECT a FROM Articulo a JOIN FETCH a.baseParaPrecio JOIN FETCH a.stock LEFT JOIN FETCH a.impuestos WHERE a.activo = 1")
    List<Articulo> findAllIncludingStock();

    @Query("SELECT a FROM Articulo a JOIN FETCH a.stock JOIN FETCH a.baseParaPrecio WHERE (a.descripcion like ?1 OR a.codigo like ?1) AND a.activo = 1")
    List<Articulo> findByDescripcionLikeOrCodigoLike(String busqueda);

    @Query("SELECT a FROM Articulo a JOIN FETCH a.stock JOIN FETCH a.baseParaPrecio WHERE (a.descripcion like ?1 OR a.codigo like ?1) AND a.activo = 1")
    List<Articulo> findByDescripcionLikeOrCodigoLike(String busqueda, Pageable pageable);

    @Query("FROM Articulo a JOIN FETCH a.baseParaPrecio LEFT JOIN FETCH a.impuestos JOIN FETCH a.stock WHERE a.idArticulo = ?1 AND a.activo = 1 ")
    Articulo findByIdComplete(Long id);

    @Modifying
    @Query("UPDATE BaseParaPrecio bp SET bp.preciosAlternos = ? WHERE bp.idArticulo = ?")
    int setPreciosAlternosFor(String preciosAlternosText, Long id);

    @Modifying
    @Query("UPDATE Articulo a SET a.activo = 0 WHERE a.idArticulo = ?")
    void softDeleteByID(Long id);
}
