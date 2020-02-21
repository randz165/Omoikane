package omoikane.principal

import com.google.common.base.Stopwatch
import omoikane.entities.Paquete
import omoikane.formularios.ImpuestosTableModel
import omoikane.formularios.OmJInternalFrame
import omoikane.inventarios.Stock
import omoikane.inventarios.StockLevelsController
import omoikane.moduloreportes.CatalogoArticulosHandler
import omoikane.multisucursal.ArticuloMultisucursalController
import omoikane.principal.*
import omoikane.producto.CodigosController
import omoikane.producto.Impuesto
import omoikane.producto.PaqueteController
import omoikane.producto.PrecioOmoikaneLogic
import omoikane.producto.compras.ComprasProductoController
import omoikane.producto.departamento.Departamento
import omoikane.producto.listadeprecios.ListaDePreciosProductoController
import omoikane.repository.ProductoRepo
import omoikane.sistema.*
import groovy.sql.*;
import groovy.swing.*
import org.apache.log4j.Logger
import org.springframework.transaction.TransactionStatus
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.EntityTransaction
import javax.persistence.Query;
import javax.swing.*
import javax.swing.table.DefaultTableModel
import java.awt.event.WindowListener;
import javax.swing.event.*;
import groovy.inspect.swingui.*;
import javax.swing.table.TableColumn;
import java.awt.event.*;
import groovy.swing.*;
import static omoikane.sistema.Usuarios.*;
import static omoikane.sistema.Permisos.*
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.application.Platform

/**
 * Clase legada del primer sistema MOL, actúa como un Controller.
 */
public class Articulos
{
    static def IDAlmacen = Principal.IDAlmacen
    static def escritorio = omoikane.principal.Principal.escritorio
    public static Logger logger = Logger.getLogger(Articulos.class);

    static def getArticulo(where) { new Articulo(where) }

    static ProductoRepo productoRepo;


    static ProductoRepo getRepo() {
        if(productoRepo == null)
            productoRepo = omoikane.principal.Principal.applicationContext.getBean(ProductoRepo.class);
        else
            return productoRepo;
    }

    static JpaTransactionManager getTransactionManager() {
        return omoikane.principal.Principal.applicationContext.getBean(JpaTransactionManager.class);
    }

    static EntityManager getEntityManager() {
            EntityManagerFactory emf = omoikane.principal.Principal.applicationContext.getBean(EntityManagerFactory.class);
            return emf.createEntityManager();
        }

    static OmJInternalFrame getCatalogoFrameInstance()
    {
        if(cerrojo(PMA_ABRIRARTICULO)){
            StopWatch timer = new StopWatch().start();
            OmJInternalFrame cat = (new omoikane.formularios.CatalogoArticulos());

            cat.setVisible(true);

            cat.txtBusqueda.keyReleased = { if(it.keyCode == it.VK_ESCAPE) cat.btnCerrar.doClick() }
            cat.toFront()

            try { cat.setSelected(true) } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario catálogo de artículos", Herramientas.getStackTraceString(e)) }
            cat.txtBusqueda.requestFocus()
            logger.trace("Tiempo de apertra de catálogo: "+timer.stop());

            return cat
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }

    static def lanzarCatalogo() {
        try {
            if(cerrojo(PMA_ABRIRARTICULO)) {
                return _lanzarCatalogo()
            } else {
                Dialogos.lanzarAlerta("Acceso Denegado")
            }
        } catch (Exception e) {
            return _lanzarCatalogo();
        }
    }

    private static def _lanzarCatalogo()
    {

        def cat = (new omoikane.formularios.CatalogoArticulos());

        cat.setVisible(true);
        escritorio.getPanelEscritorio().add(cat)

        cat.txtBusqueda.keyReleased = { if(it.keyCode == it.VK_ESCAPE) cat.btnCerrar.doClick() }
        //Herramientas.iconificable(cat)
        cat.toFront()

        try { cat.setSelected(true) } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario catálogo de artículos", Herramientas.getStackTraceString(e)) }
        cat.txtBusqueda.requestFocus()

        return cat

    }

    public static String lanzarDialogoCatalogo()
    {
        def foco=new Object()
        def cat = lanzarCatalogo()
        cat.setModoDialogo()
        cat.internalFrameClosed = {synchronized(foco){foco.notifyAll()} }
        cat.txtBusqueda.keyReleased = {
          if(it.keyCode == it.VK_ENTER) {
              cat.btnAceptar.doClick()
          }
        }
        def retorno
        cat.btnAceptar.actionPerformed = {

            JTable catTab = cat.jTable1;

            Boolean isSelected = catTab.getSelectionModel().getMinSelectionIndex() >= 0;
            if(isSelected) {
              retorno = catTab.getModel().getValueAt(catTab.getSelectedRow(), 0) as String;
              cat.btnCerrar.doClick();
            }
        }
         synchronized(foco){foco.wait()}
        retorno
    }

    static def lanzarImprimir(form)
    {
        int confirm = JOptionPane.showConfirmDialog(Principal.getEscritorio().getFrameEscritorio(), "¿Imprimir el catálogo filtrado?", "Impresión de catálogo de artículos", JOptionPane.YES_NO_CANCEL_OPTION);
        if(confirm == JOptionPane.YES_OPTION) {
            def reporte = new Reporte('omoikane/reportes/ArticulosTodos.jrxml',[txtQuery:form.getReportFilteredQuery()]);
            reporte.lanzarPreview()
        } else if(confirm == JOptionPane.NO_OPTION) {
            //Versión anterior del catálogo con todos los productos
            //CatalogoArticulosHandler articulosHandler = Principal.applicationContext.getBean( CatalogoArticulosHandler );
            //articulosHandler.handle();
            def reporte = new Reporte('omoikane/reportes/ArticulosTodos.jrxml',[txtQuery: "SELECT a.id_articulo AS xID, a.`codigo` AS xCodigo, p.porcentajeUtilidad as `xCoeficienteUtilidad`, p.porcentajeDescuentoProducto as `xCoeficienteDescuentoProducto`, p.porcentajeDescuentoLinea as `xCoeficienteDescuentoLinea`, p.porcentajeDescuentoGrupo as `xCoeficienteDescuentoGrupo`, CONCAT( IF(porcentajeDescuentoLinea > 0, CONCAT('Linea: ',porcentajeDescuentoLinea,'%'), ''), IF(porcentajeDescuentoGrupo > 0, CONCAT('Grupo: ',porcentajeDescuentoGrupo,'%'), ''), IF(porcentajeDescuentoProducto > 0, CONCAT('Producto: ',porcentajeDescuentoProducto,'%'), '')) xDescuentos, a.descripcion AS xDescripcion, a.`unidad` AS xUnidad, (SELECT GROUP_CONCAT(i.descripcion) FROM articulos_Impuesto ai JOIN Impuesto i ON ai.impuestos_id = i.id WHERE a.id_articulo = ai.articulos_id_articulo) xImpuestos, (SELECT @sumImpuestos := SUM(i.porcentaje) FROM articulos_Impuesto ai JOIN Impuesto i ON ai.impuestos_id = i.id WHERE a.id_articulo = ai.articulos_id_articulo) sumImpuestos, lineas.`descripcion` AS xLinea, lineas.`descripcion` AS xLinea, p.`costo` AS xCosto, @bpp := p.costo + (p.costo*(p.porcentajeUtilidad/100)) as bpp, @coefDesc := 1-(((p.porcentajeDescuentoProducto/100)-1)*((p.porcentajeDescuentoLinea/100)-1)*((p.porcentajeDescuentoGrupo/100)-1)*-1) as coefDesc, @montoImpuestos := (@bpp-(@bpp*@coefDesc))*(@sumImpuestos/100) montoImpuestos, @bpp-(@bpp*@coefDesc)+@montoImpuestos xPrecio FROM articulos a,base_para_precios p,lineas WHERE a.id_articulo = p.id_articulo and a.id_linea = lineas.id_linea and a.activo = 1 AND p.id_articulo = a.id_articulo"]);
            reporte.lanzarPreview()
        }

    }

    /**
     * Elimina un artículo seleccionado mediante soft-delete, siempre y cuando haya confirmación y permiso para el usuario.
     * Soft-delete no elimina realmente el artículo si no que lo desactiva.
     * @param parent JInternalFrame donde se estaciona el cuadro de confirmación
     * @param ID ID del artículo a eliminar
     * @return
     */
    static def eliminarArticulo( JInternalFrame parent, ID )
    {
        if(cerrojo(PMA_ELIMINARARTICULO)){
            int confirm = JOptionPane.showConfirmDialog(parent, "¿Está seguro de eliminar éste artículo?", "Eliminación suave de artículo", JOptionPane.YES_NO_CANCEL_OPTION)
            if( confirm == JOptionPane.YES_OPTION)
            {
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                Object result = transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    public void doInTransactionWithoutResult(TransactionStatus status) {
                        try {
                            getRepo().softDeleteByID(ID as Long)
                            status.flush()
                            Dialogos.lanzarAlerta("Artículo " + ID + " eliminado (soft-deleted)")
                        } catch(Exception e) {
                            logger.error("No registrado por causa de un error.", e);
                        }
                    }
                })
            }

        } else { Dialogos.lanzarAlerta("Acceso Denegado") }
    }

    static def onCloseFocus ( JInternalFrame parent, JInternalFrame child ) {
        child.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            void internalFrameClosed(InternalFrameEvent e) {
                super.internalFrameClosed(e)
                parent.setSelected(true)
                parent.requestDefaultFocus()
                parent.requestFocus()

            }
        })
    }

    static def lanzarDetallesArticulo(JInternalFrame parent, ID)
    {
        if(cerrojo(PMA_DETALLESARTICULO)){

            omoikane.formularios.Articulo formArticulo = new omoikane.formularios.Articulo()
            onCloseFocus(parent, formArticulo);

            def formateador = new java.text.DecimalFormat("#.00");
            formArticulo.setVisible(true)
            escritorio.getPanelEscritorio().add(formArticulo)
            Herramientas.panelFormulario(formArticulo)
            formArticulo.toFront()
            try { formArticulo.setSelected(true)

            //TODO aquí inyectar nuevo comportamiento para obtener artículo, sus subdatos y su precio generado
            omoikane.producto.Articulo art;
            JpaTransactionManager tm = (JpaTransactionManager) Principal.applicationContext.getBean("transactionManager");

            TransactionTemplate transactionTemplate = new TransactionTemplate(tm);
            Object result = transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    art = getRepo().findByIdComplete(ID as Long);

                    //Inicializar hijos
                    art.getPrecio();
                    art.getImpuestos();
                    art.getCodigosAlternos();
                    art.getRenglonesPaquete();
                    if(art.getEsPaquete()) for(Paquete paq : art.getRenglonesPaquete()) paq.getProductoContenido().getPrecio().getPrecio();
                }
            });

            PrecioOmoikaneLogic precio = art.getPrecio();
            //Inicializo los impuestos
            art.getImpuestos();

            // -- Cargar desde la BD el nombre del grupo, línea y las notas (anotación)
            Query q = getEntityManager().createNativeQuery(
                    """SELECT an.texto as texto, l.descripcion as linea, g.descripcion as grupo, cus.clave as unidadClave, cus.nombre as unidad, cps.clave as articuloClave, cps.descripcion as claveProductoSat 
                            FROM articulos a 
                                JOIN lineas l ON a.id_linea = l.id_linea 
                                JOIN grupos g ON a.id_grupo = g.id_grupo 
                                JOIN CLAVE_UNIDAD_SAT cus ON a.CLAVE_UNIDAD_SAT_CLAVE = cus.clave
                                JOIN CLAVE_PRODUCTO_SAT cps ON a.CLAVE_PRODUCTO_SAT_CLAVE = cps.clave
                                LEFT JOIN anotaciones an ON an.id_articulo = a.id_articulo 
                            WHERE a.id_articulo = """ + art.idArticulo);
            List rs = q.getResultList();
            def lin = ["id": art.getIdLinea(), "descripcion": rs.get(0)[1]];
            def gru = ["id": art.getIdGrupo(), "descripcion": rs.get(0)[2]];
            def cus = ["clave": rs.get(0)[3]  , "unidad": rs.get(0)[4]];
            def cps = ["clave": rs.get(0)[5]  , "descripcion": rs.get(0)[6]];
            def notas = rs.get(0)[0];

            formArticulo.setTxtIDArticulo    art.idArticulo           as String
            formArticulo.setTxtCodigo        art.codigo
            formArticulo.setTxtIDLinea       art.idLinea              as String
            formArticulo.setTxtIDGrupo       art.idGrupo              as String
            formArticulo.setTxtIDLineaDes    lin.descripcion           as String
            formArticulo.setTxtIDGrupoDes    gru.descripcion           as String
            formArticulo.setTxtUnidad        art.unidad                as String

            formArticulo.getTxtClaveUnidadSat().setText( cus.clave as String )
            formArticulo.getTxtClaveUnidadSatDes().setText( cus.unidad as String )
            formArticulo.getTxtClaveProductoSat().setText( cps.clave as String )
            formArticulo.getTxtClaveProductoSatDes().setText( cps.descripcion as String )

            formArticulo.setTxtDescripcion   art.descripcion
            formArticulo.setTxtImpuestos     art.precio.impuestos   as String
            formArticulo.setTxtUModificacion art.uModificacion         as String
            formArticulo.setTxtDescuento     precio.descuento  as String
            formArticulo.setTxtCosto         formateador.format( art.getBaseParaPrecio().costo  )
            formArticulo.setTxtUtilidadPorc  formateador.format( art.getBaseParaPrecio().porcentajeUtilidad )
            //formArticulo.setTxtExistencias   art.cantidad              as String
            formArticulo.setTxtPrecio        art.precio          as String
            formArticulo.setTxtComentarios   notas                     as String
            formArticulo.getTxtDesctoPorcentaje().text = formateador.format( art.getBaseParaPrecio().getPorcentajeDescuentoProducto() )
            formArticulo.getTxtDescuento2().text       = (precio.getDescuento()) as String
            formArticulo.getTxtPrecioTotal().text      = precio.precio as String
            //formArticulo.getTxtImpuestosPorc().text    = formateador.format( precio.impuestos )
            //formArticulo.getTxtImpuestos().text        = precio.impuestos  as String

            //Se cargan los impuestos aplicados al artículo
            precio.listaImpuestos.each { Impuesto im ->
                ((DefaultTableModel)formArticulo.getImpuestosTable().getModel()).addRow([
                                im
                ].toArray());
            }

            //Se establece en la GUI el departamento del artículo
            JComboBox<Departamento> depBox = formArticulo.getComboDepartamento();
            for(int i = 0; i < depBox.getModel().getSize(); i++) {
                if(depBox.getModel().getElementAt(i).getId().equals(art.getDepartamentoId())) {
                    depBox.setSelectedIndex(i);
                }
            }
            //-----

            formArticulo.getTxtUtilidad().text         = precio.utilidad   as String
            formArticulo.ID                   = ID
            formArticulo.setModoDetalles();
            omoikane.principal.Articulos.recalcularCampos(formArticulo);

            Platform.setImplicitExit(false);

            addJFXCodigosPanel(art, formArticulo);
            addJFXStockPanel(art, formArticulo);
            addJFXPaquetePanel(art, formArticulo);

            addJFXComprasProductoPanel(art, formArticulo);
            addJFXListasDePreciosPanel(art, formArticulo);
            addJFXArticuloMultisucursal(art, formArticulo);

            return formArticulo
            
            } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario detalles artículo", Herramientas.getStackTraceString(e)) }
        }else{ Dialogos.lanzarAlerta("Acceso Denegado")}
    }

    static def guardar(omoikane.formularios.Articulo formArticulo)
    {
        if(cerrojo(PMA_MODIFICARARTICULO)){
            Herramientas.verificaCampos {
                def codigo           = formArticulo.getTxtCodigo()
                def IDLinea          = formArticulo.getTxtIDLinea()
                def IDGrupo          = formArticulo.getTxtIDGrupo()
                def descripcion      = formArticulo.getTxtDescripcion()
                def unidad           = formArticulo.getTxtUnidad()
                def costo            = formArticulo.getTxtCosto()
                def descuento        = formArticulo.getTxtDesctoPorcentaje().text
                def utilidad         = formArticulo.getTxtUtilidadPorc().text
                def nombreUnidadSat  = formArticulo.getTxtClaveUnidadSatDes().text
                def claveUnidadSat   = formArticulo.getTxtClaveUnidadSat().text
                def claveProductoSat = formArticulo.getTxtClaveProductoSat().text
                def existencias   = 0;
                Departamento departamento  = formArticulo.getComboDepartamento().getSelectedItem();

                def notas   = formArticulo.getTxtComentarios()
                Herramientas.verificaCampo(codigo           ,Herramientas.texto,"codigo"+Herramientas.error1)
                Herramientas.verificaCampo(IDLinea          ,Herramientas.numero,"ID linea"+Herramientas.error2)
                Herramientas.verificaCampo(IDGrupo          ,Herramientas.numero,"ID Grupo"+Herramientas.error2)
                Herramientas.verificaCampo(claveUnidadSat   ,Herramientas.texto,"Clave unidad"+Herramientas.error1)
                Herramientas.verificaCampo(claveProductoSat ,Herramientas.texto,"Clave producto"+Herramientas.error1)
                Herramientas.verificaCampo(descripcion      ,Herramientas.texto,"descripcion"+Herramientas.error1)
                Herramientas.verificaCampo(costo            ,Herramientas.numeroReal,"costos"+Herramientas.error3)
                Herramientas.verificaCampo(descuento        ,Herramientas.numeroReal,"descuento"+Herramientas.error3)
                Herramientas.verificaCampo(utilidad         ,Herramientas.numeroReal,"utilidad"+Herramientas.error3)

                IDLinea       = java.lang.Integer.valueOf(IDLinea)
                IDGrupo       = java.lang.Integer.valueOf(IDGrupo)
                costo         = costo as Double
                descuento     = descuento as Double
                utilidad      = utilidad as Double
                existencias   = existencias as Double
                try {
                    def serv   = Nadesico.conectar()
                    if(!serv.getLinea(IDLinea))                     throw new Exception("Campo ID Línea inválida")
                    if(!serv.getGrupo(IDGrupo))                     throw new Exception("Campo ID Grupo inválida")
                    if(!serv.getClaveUnidadSat(claveUnidadSat))     throw new Exception("Campo Clave Unidad SAT es inválido")
                    if(!serv.getClaveProductoSat(claveProductoSat)) throw new Exception("Campo Clave Producto SAT es inválido")
                    def datAdd = serv.addArticulo(
                            IDAlmacen,
                            IDLinea,
                            IDGrupo,
                            departamento.getId(),
                            unidad,
                            claveUnidadSat,
                            claveProductoSat,

                            codigo,
                            descripcion,

                            costo,
                            descuento,
                            utilidad,
                            existencias)
                    def notasAdd = serv.addAnotacion(IDAlmacen, datAdd.ID, notas )
                    Dialogos.lanzarAlerta(datAdd.mensaje)
                    serv.desconectar()
                    guardarImpuestos(datAdd.ID, ((ImpuestosTableModel)formArticulo.getImpuestosTable().getModel()).getImpuestoList());
                    //if( formArticulo.getModo() == formArticulo.modo.NUEVO) { stockAdd(datAdd.ID) }

                    formArticulo.dispose()
                } catch(e) { Dialogos.error("Error, el artículo no se registró: "+e.getMessage(), e) }
                
            }
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }
    static def addJFXComprasProductoPanel(omoikane.producto.Articulo a, omoikane.formularios.Articulo form) {
        JFXPanel panel = new JFXPanel();
        form.tabbedPane.addTab("Compras", panel);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                SceneOverloaded scene = (SceneOverloaded) Principal.applicationContext.getBean("comprasProductoView");
                ((ComprasProductoController)scene.getController()).setProducto(a);
                panel.setScene(scene);
            }
        });
    }
    static def addJFXListasDePreciosPanel(omoikane.producto.Articulo a, omoikane.formularios.Articulo form) {
        JFXPanel panel = new JFXPanel();
        form.tabbedPane.addTab("Listas de precios", panel);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                SceneOverloaded scene = (SceneOverloaded) Principal.applicationContext.getBean("listaDePreciosProductoView");
                ((ListaDePreciosProductoController)scene.getController()).setProducto(a);
                panel.setScene(scene);
            }
        });
    }
    static def addJFXStockPanel(omoikane.producto.Articulo art,  omoikane.formularios.Articulo a) {

        JFXPanel panel = new JFXPanel();
        a.tabbedPane.addTab("Stock", panel);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                SceneOverloaded scene = (SceneOverloaded) Principal.applicationContext.getBean("stockLevelsView");
                ((StockLevelsController)scene.getController()).setProducto(art);
                panel.setScene(scene);
            }
        });
    }
    static def addJFXPaquetePanel(omoikane.producto.Articulo art, omoikane.formularios.Articulo a) {

        JFXPanel panel = new JFXPanel();
        a.tabbedPane.addTab("Paquete", panel);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {


                SceneOverloaded scene = (SceneOverloaded) Principal.applicationContext.getBean("paqueteView");
                ((PaqueteController)scene.getController()).setProducto(art);
                panel.setScene(scene);
            }
        });
    }

    static def addJFXCodigosPanel(omoikane.producto.Articulo art, omoikane.formularios.Articulo a) {

        JFXPanel panel = new JFXPanel();
        a.tabbedPane.addTab("Códigos", panel);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                SceneOverloaded scene = (SceneOverloaded) Principal.applicationContext.getBean("codigosView");
                ((CodigosController)scene.getController()).setProducto(art);
                panel.setScene(scene);
            }
        });
    }

    static def addJFXArticuloMultisucursal(omoikane.producto.Articulo art, omoikane.formularios.Articulo a) {

        JFXPanel panel = new JFXPanel();
        a.tabbedPane.addTab("Sucursales", panel);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                SceneOverloaded scene = (SceneOverloaded) Principal.applicationContext.getBean("articuloMultisucursalView");
                ((ArticuloMultisucursalController)scene.getController()).loadData(art.getCodigo());
                panel.setScene(scene);
            }
        });
    }

    static omoikane.formularios.Articulo lanzarFormNuevoArticulo(JInternalFrame parent)
    {
        if(cerrojo(PMA_MODIFICARARTICULO)){
            try{
            def form = new omoikane.formularios.Articulo()
            onCloseFocus(parent, form);

            form.setVisible(true)
            Herramientas.panelFormulario(form)
            escritorio.getPanelEscritorio().add(form)

            form.toFront()
            SwingBuilder.build {
                //Al presionar F1: (lanzarCatalogoDialogo)
                def serv        = Nadesico.conectar()
                form.getCampoID()   .keyReleased = { if(it.keyCode == it.VK_F1) Thread.start {form.txtIDLinea= Lineas.lanzarCatalogoDialogo() as String; form.getIDLinea().requestFocus();form.setTxtIDLineaDes((serv.getLinea(form.getIDLinea().text))?.descripcion)}  }
                form.getCampoID()   .focusLost   = { if(form.getCampoID().text != "") Thread.start { form.setTxtIDLineaDes((serv.getLinea(form.getIDLinea().text))?.descripcion?:"")} }
                form.getCampoGrupo().keyReleased = { if(it.keyCode == it.VK_F1) Thread.start {form.txtIDGrupo= Grupos.lanzarCatalogoDialogo() as String; form.getIDGrupo().requestFocus();form.setTxtIDGrupoDes((serv.getGrupo(form.getIDGrupo().text))?.descripcion)}  }
                form.getCampoGrupo().focusLost   = { if(form.getIDGrupo().text != "") Thread.start { form.setTxtIDGrupoDes((serv.getGrupo(form.getIDGrupo().text))?.descripcion?:"")}  }
                form.getTxtClaveUnidadSat().keyReleased = { if(it.keyCode == it.VK_F1) Thread.start {form.getTxtClaveUnidadSat().setText( ClaveUnidadSat.lanzarCatalogoDialogo() as String ); form.getTxtClaveUnidadSat().requestFocus(); form.getTxtClaveUnidadSatDes().setText((serv.getClaveUnidadSat(form.getTxtClaveUnidadSat().text))?.descripcion)}  }
                form.getTxtClaveUnidadSat().focusLost   = { if(form.getTxtClaveUnidadSat().text != "") Thread.start { form.getTxtClaveUnidadSatDes().setText ((serv.getClaveUnidadSat(form.getTxtClaveUnidadSat().text))?.descripcion?:"")}  }
                form.getTxtClaveProductoSat().keyReleased = { if(it.keyCode == it.VK_F1) Thread.start {form.getTxtClaveProductoSat().setText( ClaveProductoSat.lanzarCatalogoDialogo() as String ); form.getTxtClaveProductoSat().requestFocus(); form.getTxtClaveProductoSatDes().setText((serv.getClaveProductoSat(form.getTxtClaveProductoSat().text))?.descripcion)}  }
                form.getTxtClaveProductoSat().focusLost   = { if(form.getTxtClaveProductoSat().text != "") Thread.start { form.getTxtClaveProductoSatDes().setText ((serv.getClaveProductoSat(form.getTxtClaveProductoSat().text))?.descripcion?:"")}  }
                serv.desconectar()
            }
            try { form.setSelected(true) } catch(Exception e)
            {
                Dialogos.lanzarDialogoError(null, "Error al iniciar formulario detalles artículo", Herramientas.getStackTraceString(e)) }
                form.setEditable(true);

                form.setModoNuevo();
                //addJFXStockPanel(1l, form);
                //addJFXPaquetePanel(1l, form);
                return form;
            } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario nuevo artículo", Herramientas.getStackTraceString(e)) }

        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }

    static def lanzarModificarArticulo(parent, ID)
    {
        def formArticulo = lanzarDetallesArticulo(parent, ID)
        //Dialogos.lanzarAlerta("Eliminar codigo viejo de lanzarModificarArticulo")
        SwingBuilder.build {
                //Al presionar F1: (lanzarCatalogoDialogo)
                def serv        = Nadesico.conectar()
                formArticulo.getCampoID()   .keyReleased = { if(it.keyCode == it.VK_F1) Thread.start {formArticulo.txtIDLinea= Lineas.lanzarCatalogoDialogo() as String; formArticulo.getIDLinea().requestFocus();formArticulo.setTxtIDLineaDes((serv.getLinea(formArticulo.getIDLinea().text)).descripcion)}  }
                formArticulo.getCampoID()   .focusLost   = { if(formArticulo.getCampoID().text != "") Thread.start { formArticulo.setTxtIDLineaDes((serv.getLinea(formArticulo.getIDLinea().text))?.descripcion?:"")} }
                formArticulo.getCampoGrupo().keyReleased = { if(it.keyCode == it.VK_F1) Thread.start {formArticulo.txtIDGrupo= Grupos.lanzarCatalogoDialogo() as String; formArticulo.getIDGrupo().requestFocus();formArticulo.setTxtIDGrupoDes((serv.getGrupo(formArticulo.getIDGrupo().text)).descripcion)}  }
                formArticulo.getCampoGrupo().focusLost   = { if(formArticulo.getIDGrupo().text != "") Thread.start { formArticulo.setTxtIDGrupoDes((serv.getGrupo(formArticulo.getIDGrupo().text))?.descripcion?:"")}  }
                formArticulo.getTxtClaveUnidadSat().keyReleased = { if(it.keyCode == it.VK_F1) Thread.start {formArticulo.getTxtClaveUnidadSat().setText( ClaveUnidadSat.lanzarCatalogoDialogo() as String ); formArticulo.getTxtClaveUnidadSat().requestFocus(); formArticulo.getTxtClaveUnidadSatDes().setText((serv.getClaveUnidadSat(formArticulo.getTxtClaveUnidadSat().text))?.descripcion)}  }
                formArticulo.getTxtClaveUnidadSat().focusLost   = { if(formArticulo.getTxtClaveUnidadSat().text != "") Thread.start { formArticulo.getTxtClaveUnidadSatDes().setText ((serv.getClaveUnidadSat(formArticulo.getTxtClaveUnidadSat().text))?.descripcion?:"")}  }
                formArticulo.getTxtClaveProductoSat().keyReleased = { if(it.keyCode == it.VK_F1) Thread.start {formArticulo.getTxtClaveProductoSat().setText( ClaveProductoSat.lanzarCatalogoDialogo() as String ); formArticulo.getTxtClaveProductoSat().requestFocus(); formArticulo.getTxtClaveProductoSatDes().setText((serv.getClaveProductoSat(formArticulo.getTxtClaveProductoSat().text))?.descripcion)}  }
                formArticulo.getTxtClaveProductoSat().focusLost   = { if(formArticulo.getTxtClaveProductoSat().text != "") Thread.start { formArticulo.getTxtClaveProductoSatDes().setText ((serv.getClaveProductoSat(formArticulo.getTxtClaveProductoSat().text))?.descripcion?:"")}  }
                serv.desconectar()
         }
        formArticulo.setModoModificar();

        formArticulo
    }

    static def modificar(omoikane.formularios.Articulo formArticulo)
    {
        if(cerrojo(PMA_MODIFICARARTICULO)){
            def f = formArticulo
            Departamento departamento = f.getComboDepartamento().getSelectedItem();
            def c = [cod:f.getTxtCodigo(), lin:f.getTxtIDLinea(),gru:f.getTxtIDGrupo(), dep: departamento.getId(),des:f.getTxtDescripcion(), cos:f.getTxtCosto(),
            dto:f.getTxtDesctoPorcentaje().text, uti:f.getTxtUtilidadPorc().text, art:f.getTxtIDArticulo(), uni:f.getTxtUnidad(), notas:f.getTxtComentarios()]
            def claveUnidadSat   = formArticulo.getTxtClaveUnidadSat().text
            def nombreUnidadSat   = formArticulo.getTxtClaveUnidadSatDes().text
            def claveProductoSat = formArticulo.getTxtClaveProductoSat().text

            Herramientas.verificaCampos {
                Herramientas.verificaCampo(c.cod,Herramientas.texto,"codigo"+Herramientas.error1)
                Herramientas.verificaCampo(c.lin,Herramientas.numero,"ID linea"+Herramientas.error2)
                Herramientas.verificaCampo(c.gru,Herramientas.numero,"ID Grupo"+Herramientas.error2)
                Herramientas.verificaCampo(c.des,Herramientas.texto,"descripcion"+Herramientas.error1)
                Herramientas.verificaCampo(c.cos,Herramientas.numeroReal,"costos"+Herramientas.error3)
                Herramientas.verificaCampo(c.dto,Herramientas.numeroReal,"descuento"+Herramientas.error3)
                Herramientas.verificaCampo(c.uti,Herramientas.numeroReal,"utilidad"+Herramientas.error3)
                Herramientas.verificaCampo(claveUnidadSat   ,Herramientas.texto,"Clave unidad"+Herramientas.error1)
                Herramientas.verificaCampo(claveProductoSat ,Herramientas.texto,"Clave producto"+Herramientas.error1)

                def serv = Nadesico.conectar()
                if(!serv.getLinea(c.lin)) throw new Exception("Campo ID Línea inválida")
                if(!serv.getGrupo(c.gru)) throw new Exception("Campo ID Grupo inválida")
                if(!serv.getClaveUnidadSat(claveUnidadSat))     throw new Exception("Campo Clave Unidad SAT es inválido")
                if(!serv.getClaveProductoSat(claveProductoSat)) throw new Exception("Campo Clave Producto SAT es inválido")

                Dialogos.lanzarAlerta(serv.modArticulo(IDAlmacen, c.art, c.cod, c.lin, c.gru, c.dep, c.uni, claveUnidadSat, claveProductoSat, c.des, c.cos, c.uti, c.dto))
                serv.modAnotacion(IDAlmacen, c.art, c.notas)
                serv.desconectar()
                guardarImpuestos(Long.parseLong( c.art ), ((ImpuestosTableModel)formArticulo.getImpuestosTable().getModel()).getImpuestoList());
                //PuertoNadesico.workIn() { it.CacheArticulos.actualizar(c.art) }
            }
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }

    static void guardarImpuestos(Long idArticulo, List<Impuesto> impuestos) {

        EntityManagerFactory emf = (EntityManagerFactory) Principal.applicationContext.getBean("entityManagerFactory");
        EntityManager em = emf.createEntityManager();

        omoikane.producto.Articulo articulo = (omoikane.producto.Articulo) em.find(omoikane.producto.Articulo.class, idArticulo);

        articulo.setImpuestos(impuestos);

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(articulo);
        tx.commit();

    }

    static def recalcularCampos(omoikane.formularios.Articulo formArticulo) {
        omoikane.formularios.Articulo f = formArticulo
        //def c = [ imp:Double.parseDouble(f.getTxtImpuestosPorc().text), cos:Double.parseDouble(f.getTxtCosto()),
        //        dto:Double.parseDouble(f.getTxtDesctoPorcentaje().text), uti:Double.parseDouble(f.getTxtUtilidadPorc().text)]
        def c =[imp:0,cos:0,dto:0,uti:0]
        for(Vector renglon : ((DefaultTableModel)f.getImpuestosTable().getModel()).getDataVector()) {
            c.imp += ((Impuesto)renglon.get(0)).getPorcentaje().doubleValue();
        }

        if(f.getTxtCosto()==""){c.cos=0}else{c.cos=Double.parseDouble(f.getTxtCosto())}
        if(f.getTxtDesctoPorcentaje().text==""){c.dto=0}else{c.dto=Double.parseDouble(f.getTxtDesctoPorcentaje().text)}
        if(f.getTxtUtilidadPorc().text==""){c.uti=0}else{c.uti=Double.parseDouble(f.getTxtUtilidadPorc().text)}


        def utilidad = c.uti * c.cos * 0.01;
        def descuento = (utilidad + c.cos) * c.dto * 0.01;
        for(Vector renglon : ((DefaultTableModel)f.getImpuestosTable().getModel()).getDataVector()) {
            def porcImpRenglon = ((Impuesto)renglon.get(0)).getPorcentaje().doubleValue();
            ((Impuesto)renglon.get(0)).setImpuesto( ( c.cos + utilidad - descuento ) * porcImpRenglon * 0.01 )
        }
        def impuesto = ( c.cos + utilidad - descuento ) * c.imp * 0.01;
        def precio = (c.cos + utilidad - descuento + impuesto);
        def formateador = new java.text.DecimalFormat("#0.00");

        f.setTxtDescuento3(formateador.format(descuento));
        f.setTxtDescuento(formateador.format(c.dto));
        f.setTxtUtilidad2(formateador.format(utilidad));
        f.setTxtPrecio(formateador.format(precio));
        f.setTxtPrecioTotal(formateador.format(precio))
        ((DefaultTableModel)f.getImpuestosTable().getModel()).fireTableDataChanged();

    }

    static def recalcularUtilidad(formArticulo) {
        def f = formArticulo
        //def c = [cos:(f.getTxtCosto()) as Double                  ,pre:(f.getTxtPrecioTotal().text) as Double,
        //         poi:(f.getTxtImpuestosPorc().text) as Double     ,pod:(f.getTxtDesctoPorcentaje().text) as Double]
        def c =[cos:0,poi:0,pod:0,pre:0]
        if (f.getTxtCosto()==""){c.cos=0}else{c.cos=(f.getTxtCosto()) as Double}
        for(Vector renglon : ((DefaultTableModel)f.getImpuestosTable().getModel()).getDataVector()) {
            c.poi += ((Impuesto)renglon.get(0)).getPorcentaje().doubleValue();
        }
        if(f.getTxtDesctoPorcentaje().text==""){c.pod=0}else{c.pod=(f.getTxtDesctoPorcentaje().text) as Double}
        if(f.getTxtPrecioTotal().text==""){c.pre=0}else{c.pre=(f.getTxtPrecioTotal().text) as Double}
        def formateador = new java.text.DecimalFormat("#0.00");
        c.poi=c.poi/100
        c.pod=c.pod/100

        def porcentajeUtilidad  =   (c.pre/(c.cos*(1+c.poi)*(1-c.pod)))-1
        def utilidad            =   c.cos*porcentajeUtilidad
        def descuento           =   (c.cos+utilidad)*c.pod
        for(Vector renglon : ((DefaultTableModel)f.getImpuestosTable().getModel()).getDataVector()) {
            def porcImpRenglon = ((Impuesto)renglon.get(0)).getPorcentaje().doubleValue();
            ((Impuesto)renglon.get(0)).setImpuesto( (c.cos+utilidad-descuento) * porcImpRenglon * 0.01 );
        }
        def impuesto            =   (c.cos+utilidad-descuento)*c.poi
        porcentajeUtilidad      =   porcentajeUtilidad*100

        f.setTxtDescuento3(formateador.format(descuento));
        f.setTxtUtilidad2(formateador.format(utilidad))
        f.setTxtUtilidadPorcText(formateador.format(porcentajeUtilidad))
        f.setTxtPrecio(formateador.format(c.pre))
        ((DefaultTableModel)f.getImpuestosTable().getModel()).fireTableDataChanged();

    }


}
