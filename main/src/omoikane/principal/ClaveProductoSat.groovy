/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package omoikane.principal

import omoikane.formularios.CatalogoClaveProductoSat
import omoikane.formularios.CatalogoClaveUnidadSat
import omoikane.sistema.Dialogos
import omoikane.sistema.Herramientas
import omoikane.sistema.Nadesico

import static omoikane.sistema.Permisos.getPMA_ABRIRGRUPO
import static omoikane.sistema.Permisos.getPMA_DETALLESGRUPO
import static omoikane.sistema.Usuarios.cerrojo

/**
 *
 * @author Octavio
 */
class ClaveProductoSat {
    static def queryGrupos  = ""
    static def escritorio = omoikane.principal.Principal.escritorio

      
    static def lanzarCatalogo()
    {
        def cat = (new CatalogoClaveProductoSat())
        cat.setVisible(true);
        cat.jProgressBar1.setIndeterminate(true);
        escritorio.getPanelEscritorio().add(cat)
        cat.txtBusqueda.keyReleased = { if(it.keyCode == it.VK_ESCAPE) cat.btnCerrar.doClick() }
        Herramientas.iconificable(cat)
        cat.toFront()
        try { cat.setSelected(true) } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario catálogo de productos y servicios del SAT", Herramientas.getStackTraceString(e)) }
        cat.txtBusqueda.requestFocus()

        Thread.start {
            poblar(cat.getTablaGrupos(),"")
            cat.jProgressBar1.setIndeterminate(false);
        }
        return cat
    }

    static def lanzarCatalogoDialogo()
    {
        def foco=new Object()
        def cat = lanzarCatalogo()
        cat.setModoDialogo()
        cat.internalFrameClosed = {synchronized(foco){foco.notifyAll()} }
        cat.txtBusqueda.keyReleased = { if(it.keyCode == it.VK_ENTER) cat.btnAceptar.doClick() }
        def retorno
        cat.btnAceptar.actionPerformed = { def catTab = cat.tablaGrupos; retorno = catTab.getModel().getValueAt(catTab.getSelectedRow(), 0) as String; cat.btnCerrar.doClick(); }
        synchronized(foco){foco.wait()}
        retorno
    }

    static def poblar(tablaMovs,txtBusqueda)
    {

        def dataTabMovs = tablaMovs.getModel()
         try {
            def movimientos = Nadesico.conectar().getRows(queryGrupos =("SELECT * FROM CLAVE_PRODUCTO_SAT WHERE (CLAVE LIKE '%"+txtBusqueda+"%' OR DESCRIPCION LIKE '%"+txtBusqueda+"%')") )
            def filaNva = []

            movimientos.each {
                filaNva = [it.CLAVE, it.DESCRIPCION]
                dataTabMovs.addRow(filaNva.toArray())
            }
        } catch(Exception e) {
            Dialogos.lanzarDialogoError(null, "Error grave. No hay conexion con la base de datos!", omoikane.sistema.Herramientas.getStackTraceString(e))
        }
    }


}