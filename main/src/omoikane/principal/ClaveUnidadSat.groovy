/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package omoikane.principal

import omoikane.formularios.CatalogoClaveUnidadSat
import omoikane.sistema.Dialogos
import omoikane.sistema.Herramientas
import omoikane.sistema.Nadesico

import static omoikane.sistema.Permisos.*
import static omoikane.sistema.Usuarios.cerrojo;

/**
 *
 * @author Octavio
 */
class ClaveUnidadSat {
    static def queryGrupos  = ""
    static def escritorio = omoikane.principal.Principal.escritorio

      
    static def lanzarCatalogo()
    {
        if(cerrojo(PMA_ABRIRGRUPO)){
            def cat = (new CatalogoClaveUnidadSat())
            cat.setVisible(true);
            cat.jProgressBar1.setIndeterminate(true);
            escritorio.getPanelEscritorio().add(cat)
            cat.txtBusqueda.keyReleased = { if(it.keyCode == it.VK_ESCAPE) cat.btnCerrar.doClick() }
            Herramientas.iconificable(cat)
            cat.toFront()
            try { cat.setSelected(true) } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario catálogo de unidades SAT", Herramientas.getStackTraceString(e)) }
            cat.txtBusqueda.requestFocus()

            Thread.start {
                poblar(cat.getTablaGrupos(),"")
                cat.jProgressBar1.setIndeterminate(false);
            }
            return cat
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}

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



    static def lanzarDetallesGrupo(ID)
    {
        if(cerrojo(PMA_DETALLESGRUPO)){
            def formGrupo = new omoikane.formularios.Grupo()
            formGrupo.setVisible(true)
            escritorio.getPanelEscritorio().add(formGrupo)
            Herramientas.iconificable(formGrupo)
            formGrupo.toFront()
            try { formGrupo.setSelected(true) } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario detalles Grupo", Herramientas.getStackTraceString(e)) }
            def gru         = Nadesico.conectar().getGrupo(ID)
            formGrupo.setTxtIDGrupo        gru.id_grupo      as String
            formGrupo.setTxtDescripcion    gru.descripcion
            formGrupo.setTxtDescuento      gru.descuento     as String
            formGrupo.setTxtUModificacion  gru.uModificacion as String
            formGrupo.setModoDetalles();
            return formGrupo
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }

    static def poblar(tablaMovs,txtBusqueda)
    {

        def dataTabMovs = tablaMovs.getModel()
         try {
            def movimientos = Nadesico.conectar().getRows(queryGrupos =("SELECT * FROM CLAVE_UNIDAD_SAT WHERE (CLAVE LIKE '%"+txtBusqueda+"%' OR NOMBRE LIKE '%"+txtBusqueda+"%' OR DESCRIPCION LIKE '%\"+txtBusqueda+\"%' OR SIMBOLO LIKE '%\"+txtBusqueda+\"%')") )
            def filaNva = []

            movimientos.each {
                filaNva = [it.CLAVE, it.NOMBRE, it.DESCRIPCION, it.SIMBOLO]
                dataTabMovs.addRow(filaNva.toArray())
            }
        } catch(Exception e) {
            Dialogos.lanzarDialogoError(null, "Error grave. No hay conexion con la base de datos!", omoikane.sistema.Herramientas.getStackTraceString(e))
        }
    }


}