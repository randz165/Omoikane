/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CatalogoArticulos.java
 *
 * Created on 17/08/2008, 05:43:22 PM
 */

package omoikane.formularios;

import java.math.BigDecimal;
import java.sql.*;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.table.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.*;
import java.awt.*;
import java.awt.event.*;

import com.google.common.base.Stopwatch;
import com.jhlabs.image.*;
import omoikane.principal.Principal;
import omoikane.producto.BaseParaPrecio;
import omoikane.producto.Impuesto;
import omoikane.producto.PrecioOmoikaneLogic;
import omoikane.sistema.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jdesktop.swingx.graphics.GraphicsUtilities;
import org.jdesktop.swingx.image.GaussianBlurFilter;

/** ////////////////////////////////////////////////////////////////////////////////////////////////
 * /////////////////////////////////////////////////////////////////////////////////////////////////
 * @author Octavio
 */
public class CatalogoArticulos extends OmJInternalFrame {

    TimerBusqueda          timerBusqueda;
    BufferedImage          fondo;
    public int             IDSeleccionado;
    public String          codigoSeleccionado;
    public int IDAlmacen = omoikane.principal.Principal.IDAlmacen;
    public String          txtQuery;
    public ArticulosTableModel modelo;
    public boolean modal = false;

    public void mostrar() { this.setVisible(true);  }
    public void ocultar() { this.setVisible(false); }

    class TimerBusqueda extends Thread
    {
        CatalogoArticulos ca;
        boolean busquedaActiva = true;

        TimerBusqueda(CatalogoArticulos ca) { this.ca = ca; }
        public void run()
        {
            synchronized(this)
            {
                busquedaActiva = true;
                try { this.wait(500); } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error en el timer de búsqueda automática", Herramientas.getStackTraceString(e)); }
                if(busquedaActiva && ca.modelo != null) { ca.buscar(); }
            }
        }
        void cancelar()
        {
            busquedaActiva = false;
            try { this.notify(); } catch(Exception e) {}
        }
    }

    private String mainQuery;
    public String getMainQuery() { return mainQuery; }
    public void setMainQuery(String mainQuery) { this.mainQuery = mainQuery; }
    /** Creates new form CatalogoArticulos */
    public CatalogoArticulos() {
        //Conectar a MySQL
        try {
            initComponents();
            programarShortcuts();
            cargaProgressBar.setIndeterminate(true);

            new Thread() {
                public void run() {
                    String[]  columnas = {"Código", "Línea", "Grupo", "Concepto", "Unidad", "Precio", "Existencias"};
                    ArrayList cols     = new ArrayList<String>(Arrays.asList(columnas));
                    Class[]   clases   = {String.class, String.class, String.class, String.class, String.class, String.class, String.class};
                    ArrayList cls      = new ArrayList<Class>(Arrays.asList(clases));
                    double[]  widths   = {0.16,0.15,0.07,0.40,0.05,0.08,0.08};

                    ArticulosTableModel modeloTabla = new ArticulosTableModel(cols, cls);
                    //jTable1.enableInputMethods(false);
                    
                    modelo = modeloTabla;
                    //Notas:
                    //(1) Favor de no alterar el orden  de los campos, añadir nuevos campos al final. Por defecto ArticulosTableModel depende de éste orden
                    //(2) Por defecto select, distinct y from deben estár escritos con minúsculas. Ver omoikane.sistema.ScrollableTableModel.getRowCount(ScrollableTableModel.groovy).
                    setMainQuery("select a.id_articulo as xID, a.codigo as xCodigo, l.descripcion as xLinea, g.descripcion as xGrupo, a.descripcion as xDescripcion, a.unidad as xUnidad, 0 as xPrecio, 0 as xExistencias, " +
                            "bp.costo as xCosto, IFNULL(bp.porcentajeImpuestos, 0), bp.porcentajeDescuentoLinea, bp.porcentajeDescuentoGrupo, bp.porcentajeDescuentoProducto, bp.porcentajeUtilidad, " +
                            "s.enTienda + s.enBodega, IFNULL(sum(i.porcentaje), 0) as totalImpuestos " +
                            "from articulos a " +
                            "JOIN base_para_precios bp ON a.id_articulo = bp.id_articulo " +
                            "JOIN Stock s ON s.idArticulo = a.id_articulo " +
                            "JOIN lineas l ON l.id_linea = a.id_linea " +
                            "JOIN grupos g ON g.id_grupo = a.id_grupo " +
                            "LEFT JOIN articulos_Impuesto ai ON a.id_articulo = ai.articulos_id_articulo " +
                            "LEFT JOIN Impuesto i ON ai.impuestos_id = i.id " +
                            "WHERE a.activo = 1 ");
                    String query = getMainQuery() + "GROUP BY a.id_articulo ";
                    setQueryTable( query );
                    
                    jTable1.setModel(modeloTabla);
                    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
                    rightRenderer.setHorizontalAlignment( DefaultTableCellRenderer.RIGHT );
                    jTable1.getColumnModel().getColumn(5).setCellRenderer( rightRenderer );
                    jTable1.getColumnModel().getColumn(6).setCellRenderer( rightRenderer );

                    cargaProgressBar.setIndeterminate(false);

                    Herramientas.setColumnsWidth(jTable1, 960, widths);

                }
            }.start();

            //Instrucciones para el funcionamiento del fondo semistransparente
            this.setOpaque(false);

            this.generarFondo();
            this.btnEliminar.setVisible(true);
            Herramientas.centrarVentana(this);
            this.btnAceptar.setVisible(false);
       } catch(Exception e) {
           Dialogos.error("Error al iniciar catálogo", e);
           this.dispose();
       }

    }
    public void programarShortcuts() {

        Action buscar = new AbstractAction() { public void actionPerformed(ActionEvent e) { txtBusqueda.requestFocusInWindow(); } };
        getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "buscar");
        getActionMap().put("buscar"                 , buscar  );

        Action detalles = new AbstractAction() { public void actionPerformed(ActionEvent e) { btnDetalles.doClick(); } };
        getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "detalles");
        getActionMap().put("detalles"                 , detalles  );

        Action nuevo = new AbstractAction() { public void actionPerformed(ActionEvent e) { btnNuevo.doClick(); } };
        getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "nuevo");
        getActionMap().put("nuevo"                 , nuevo  );

        Action modificar = new AbstractAction() { public void actionPerformed(ActionEvent e) { btnModificar.doClick(); } };
        getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "modificar");
        getActionMap().put("modificar"                 , modificar  );

        Action eliminar = new AbstractAction() { public void actionPerformed(ActionEvent e) { btnEliminar.doClick(); } };
        getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        getActionMap().put("eliminar"                 , eliminar  );

        Action imprimir = new AbstractAction() { public void actionPerformed(ActionEvent e) { btnImprimir.doClick(); } };
        getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "imprimir");
        getActionMap().put("imprimir"                 , imprimir  );
    }
    public void setModoDialogo()
    {
        modal=true;

        this.btnAceptar.setVisible(true);
        Action aceptar = new AbstractAction() { public void actionPerformed(ActionEvent e) {
            System.out.println("Print desde catálogo articulos setModoDialogo()");
            ((CatalogoArticulos)e.getSource()).btnAceptar.doClick();
        } };
        getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "aceptar");
        getActionMap().put("aceptar"                 , aceptar  );

    }

    public void setQueryTable(String query) {
        if(Principal.DEBUG) System.out.println( "==="+query);

        txtQuery = query;

        /** Ejecuto la consulta del catálogo **/
        modelo.setQuery(query);
        /** /fin ejecución de consulta de catálogo **/

        //Selecciona la primera fila luego de una búsqueda
        //jTable1.getSelectionModel().setSelectionInterval(0,0);

    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        txtBusqueda = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnCerrar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnNuevo = new javax.swing.JButton();
        btnDetalles = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        btnAceptar = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        chkCodigo = new javax.swing.JCheckBox();
        chkLineas = new javax.swing.JCheckBox();
        chkGrupos = new javax.swing.JCheckBox();
        cargaProgressBar = new javax.swing.JProgressBar();

        setIconifiable(true);
        setTitle("Catálogo de artículos");
        setPreferredSize(new java.awt.Dimension(1000, 590));



        jScrollPane1.setAutoscrolls(true);

        jTable1.setFont(new java.awt.Font("Tahoma", 0, 17)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Línea", "Grupo", "Concepto", "Unidad", "Precio", "Existencias"
            }
        ));
        jTable1.setFocusable(false);
        jTable1.setRowHeight(20);
        jTable1.setShowHorizontalLines(false);
        jScrollPane1.setViewportView(jTable1);

        txtBusqueda.setNextFocusableComponent(btnDetalles);
        txtBusqueda.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtBusquedaKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtBusquedaKeyTyped(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Buscar [F3]:");

        jLabel2.setFont(new java.awt.Font("Arial", 1, 36)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("<html><head><style type='text/css'>body { font-family: 'Roboto Thin'; font-size: 36px; }</style></head>\n<body>\nCatálogo de Artículos\n</body></html>");

        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/32x32/back.png"))); // NOI18N
        btnCerrar.setText("Cerrar [Esc]");
        btnCerrar.setRequestFocusEnabled(false);
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/32x32/page_remove.png"))); // NOI18N
        btnEliminar.setText("Eliminar [Supr]");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        btnModificar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/32x32/page_edit.png"))); // NOI18N
        btnModificar.setText("Modificar [F6]");
        btnModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarActionPerformed(evt);
            }
        });

        btnNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/32x32/page_add.png"))); // NOI18N
        btnNuevo.setText("Nuevo [F5]");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnDetalles.setIcon(new javax.swing.ImageIcon(getClass().getResource("/32x32/page_search.png"))); // NOI18N
        btnDetalles.setText("Detalles [F4]");
        btnDetalles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDetallesActionPerformed(evt);
            }
        });

        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/32x32/printer.png"))); // NOI18N
        btnImprimir.setText("<html><center>Imprimir [F8]</center></html>");
        btnImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimirActionPerformed(evt);
            }
        });

        btnAceptar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/32x32/accept.png"))); // NOI18N
        btnAceptar.setText("Aceptar [Enter]");
        btnAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarActionPerformed(evt);
            }
        });

        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Critério:");

        chkCodigo.setForeground(new java.awt.Color(255, 255, 255));
        chkCodigo.setSelected(true);
        chkCodigo.setText("Cód, Desc");
        chkCodigo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkCodigoActionPerformed(evt);
            }
        });

        chkLineas.setForeground(new java.awt.Color(255, 255, 255));
        chkLineas.setText("Líneas");
        chkLineas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkLineasActionPerformed(evt);
            }
        });

        chkGrupos.setForeground(new java.awt.Color(255, 255, 255));
        chkGrupos.setText("Grupos");
        chkGrupos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkGruposActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAceptar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDetalles)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNuevo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnModificar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEliminar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImprimir))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 557, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkLineas, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkGrupos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(cargaProgressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel6)
                    .addComponent(txtBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkCodigo)
                    .addComponent(chkLineas)
                    .addComponent(chkGrupos))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cargaProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDetalles)
                    .addComponent(btnNuevo)
                    .addComponent(btnModificar)
                    .addComponent(btnEliminar)
                    .addComponent(btnAceptar)
                    .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents



    private void txtBusquedaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBusquedaKeyTyped
        // TODO add your handling code here:
        //System.out.println("······KeyEvent>"+evt.getKeyChar());
        //System.out.println("······KeyEvent>"+evt.getKeyCode());
        preBuscar(); 
    }//GEN-LAST:event_txtBusquedaKeyTyped

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        // TODO add your handling code here:

        
        new Thread() {
            public void run() {
                dispose();
                ArticulosTableModel tabModelo = modelo;

                if(tabModelo != null) { tabModelo.destroy(); }
                modelo = null;

                if(!modal && Principal.getMenuPrincipal() != null && Principal.getMenuPrincipal().getMenuPrincipal() != null){
                    Principal.getMenuPrincipal().getMenuPrincipal().requestFocusInWindow();
                }
            }
        }.start();

}//GEN-LAST:event_btnCerrarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        // TODO add your handling code here:
        int IDArticulo = ((ScrollableTableModel)jTable1.getModel()).getIDArticuloFila(this.jTable1.getSelectedRow());
        if(IDArticulo != -1) {
            String descripcion = ((ScrollableTableModel)jTable1.getModel()).getDescripcion(jTable1.getSelectedRow());
            omoikane.principal.Articulos.eliminarArticulo(this, IDArticulo);
            ((ScrollableTableModel)jTable1.getModel()).refrescar();
        }
}//GEN-LAST:event_btnEliminarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        // TODO add your handling code here:
        int IDArticulo = ((ScrollableTableModel)jTable1.getModel()).getIDArticuloFila(this.jTable1.getSelectedRow());

        //Lanzar la ventana de detalles:
        if(IDArticulo != -1) {
            JInternalFrame wnd = (JInternalFrame)omoikane.principal.Articulos.lanzarModificarArticulo(this, IDArticulo);
            wnd.addInternalFrameListener(iframeAdapter);
        }
}//GEN-LAST:event_btnModificarActionPerformed

    public InternalFrameAdapter iframeAdapter = new InternalFrameAdapter() {
        public void internalFrameClosed(InternalFrameEvent e) {
            ((ScrollableTableModel)jTable1.getModel()).refrescar();
        }
    };

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        // TODO add your handling code here:
        omoikane.formularios.Articulo articuloForm = omoikane.principal.Articulos.lanzarFormNuevoArticulo(this);
        articuloForm.addInternalFrameListener(iframeAdapter);
}//GEN-LAST:event_btnNuevoActionPerformed

    private void btnDetallesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetallesActionPerformed
        // TODO add your handling code here:
        int IDArticulo = ((ScrollableTableModel)jTable1.getModel()).getIDArticuloFila(this.jTable1.getSelectedRow());
        
        //Lanzar la ventana de detalles:
        if(IDArticulo != -1) {
            Stopwatch timer = new Stopwatch().start();
            JInternalFrame wnd = (JInternalFrame)omoikane.principal.Articulos.lanzarDetallesArticulo(this, IDArticulo);
            wnd.addInternalFrameListener(iframeAdapter);
            System.out.println("Tiempo de lanzamiento de detalles de artículo: " + timer.stop());
        }
}//GEN-LAST:event_btnDetallesActionPerformed

    private void txtBusquedaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBusquedaKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode() == evt.VK_ENTER) {
            this.txtBusqueda.selectAll();
        }
        if(evt.getKeyCode() == evt.VK_DOWN)
        {
            int sigFila = jTable1.getSelectedRow()+1;
            if(sigFila < jTable1.getRowCount())
            {
                this.jTable1.setRowSelectionInterval(sigFila, sigFila);
                this.jTable1.scrollRectToVisible(jTable1.getCellRect(sigFila, 1, true));
            }
        }
        if(evt.getKeyCode() == evt.VK_UP)
        {
            int antFila = jTable1.getSelectedRow()-1;
            if(antFila >= 0) {
                this.jTable1.setRowSelectionInterval(antFila, antFila);
                this.jTable1.scrollRectToVisible(jTable1.getCellRect(antFila, 1, true));
            }
        }
        if(evt.getKeyCode() == evt.VK_PAGE_DOWN)
        {
            int nFilas  = (int) this.jScrollPane1.getViewportBorderBounds().getHeight() / jTable1.getRowHeight();
            int sigFila = jTable1.getSelectedRow()+nFilas;
            if(sigFila > jTable1.getRowCount()) {
                sigFila = jTable1.getRowCount()-1;
            }
            if(sigFila < jTable1.getRowCount()) {
                this.jTable1.setRowSelectionInterval(sigFila, sigFila);
                this.jTable1.scrollRectToVisible(jTable1.getCellRect(sigFila, 1, true));
            }
        }
        if(evt.getKeyCode() == evt.VK_PAGE_UP)
        {
            int nFilas  = (int) this.jScrollPane1.getViewportBorderBounds().getHeight() / jTable1.getRowHeight();
            int antFila = jTable1.getSelectedRow()-nFilas;
            if(antFila < 0) {
                antFila = 0;
            }
            this.jTable1.setRowSelectionInterval(antFila, antFila);
            this.jTable1.scrollRectToVisible(jTable1.getCellRect(antFila, 1, true));
        }
        if(evt.getKeyCode() == evt.VK_DELETE)
        {
            this.btnEliminar.doClick();
        }
    }//GEN-LAST:event_txtBusquedaKeyPressed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        // TODO add your handling code here:
        omoikane.principal.Articulos.lanzarImprimir(this);
    }//GEN-LAST:event_btnImprimirActionPerformed

    private void btnAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarActionPerformed

        Boolean isSelected = jTable1.getSelectionModel().getMinSelectionIndex() >= 0;
        if(isSelected) {
            ScrollableTableModel stm = ((ScrollableTableModel)jTable1.getModel());
            int IDArticulo = stm.getIDArticuloFila(this.jTable1.getSelectedRow());
            System.out.println ("btn aceptar");

            if(IDArticulo != -1) {
                IDSeleccionado = IDArticulo; codigoSeleccionado = (String)stm.getValueAt(this.jTable1.getSelectedRow(), 0);
                this.btnCerrar.doClick();
            }
        }
}//GEN-LAST:event_btnAceptarActionPerformed

    private void chkCodigoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkCodigoActionPerformed
        // TODO add your handling code here:
        buscar();
}//GEN-LAST:event_chkCodigoActionPerformed

    private void chkGruposActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkGruposActionPerformed
        // TODO add your handling code here:
        buscar();
    }//GEN-LAST:event_chkGruposActionPerformed

    private void chkLineasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkLineasActionPerformed
        // TODO add your handling code here:
        buscar();
    }//GEN-LAST:event_chkLineasActionPerformed

    public boolean getBuscarCodigoDescripcion()
    {
        return this.chkCodigo.getModel().isSelected();
    }
    public boolean getBuscarLineas() {
        return this.chkLineas.getModel().isSelected();
    }
    public boolean getBuscarGrupos() {
        return this.chkGrupos.getModel().isSelected();
    }
    public void preBuscar()
    {
        if(timerBusqueda != null && timerBusqueda.isAlive()) { timerBusqueda.cancelar(); }
        this.timerBusqueda = new TimerBusqueda(this);
        timerBusqueda.start();
       
    }

    public void buscar()
    {
        boolean xCodDes = getBuscarCodigoDescripcion();
        boolean xLineas = getBuscarLineas();
        boolean xGrupos = getBuscarGrupos();
        String busqueda = this.txtBusqueda.getText();

		//Limpia la cadena de búsqueda
        busqueda = StringEscapeUtils.escapeSql(busqueda);

        if(busqueda==null) { xCodDes = xLineas = xGrupos = false; }

        String query = getMainQuery();

        
        if(xCodDes || xLineas || xGrupos) { query = MessageFormat.format("{0} AND ", query); }
        if(xCodDes) {
                query = MessageFormat.format("{0} a.id_articulo IN (SELECT a.id_articulo FROM articulos a LEFT JOIN codigo_producto as b ON a.id_articulo = b.producto_id_articulo WHERE (a.descripcion like ''%{1}%'' or a.codigo like ''%{2}%'' or b.codigo like ''%{3}%'')) ", query, busqueda, busqueda, busqueda);
        }
        if(xCodDes && (xLineas || xGrupos)) { query = MessageFormat.format("{0}OR ", query); }
        if(xLineas) {
                query = MessageFormat.format("{0} (l.descripcion like ''%{1}%'' ) ", query, busqueda);
        }
        if((xLineas||xCodDes) && xGrupos) { query = MessageFormat.format("{0}OR ", query); }
        if(xGrupos) {
                query = MessageFormat.format("{0} (g.descripcion like ''%{1}%'' ) ", query, busqueda);
        }
        query = MessageFormat.format("{0}GROUP BY a.id_articulo ", query);
        
        setQueryTable(query);
    }

    public String getReportFilteredQuery()
    {
        String query = "SELECT a.id_articulo AS xID, a.`codigo` AS xCodigo, p.porcentajeUtilidad as `xCoeficienteUtilidad`, p.porcentajeDescuentoProducto as `xCoeficienteDescuentoProducto`, p.porcentajeDescuentoLinea as `xCoeficienteDescuentoLinea`, p.porcentajeDescuentoGrupo as `xCoeficienteDescuentoGrupo`, CONCAT( IF(porcentajeDescuentoLinea > 0, CONCAT('Linea: ',porcentajeDescuentoLinea,'%'), ''), IF(porcentajeDescuentoGrupo > 0, CONCAT('Grupo: ',porcentajeDescuentoGrupo,'%'), ''), IF(porcentajeDescuentoProducto > 0, CONCAT('Producto: ',porcentajeDescuentoProducto,'%'), '')) xDescuentos, a.descripcion AS xDescripcion, a.`unidad` AS xUnidad, (SELECT GROUP_CONCAT(i.descripcion) FROM articulos_Impuesto ai JOIN Impuesto i ON ai.impuestos_id = i.id WHERE a.id_articulo = ai.articulos_id_articulo) xImpuestos, (SELECT @sumImpuestos := SUM(i.porcentaje) FROM articulos_Impuesto ai JOIN Impuesto i ON ai.impuestos_id = i.id WHERE a.id_articulo = ai.articulos_id_articulo) sumImpuestos, l.`descripcion` AS xLinea, l.`descripcion` AS xLinea, p.`costo` AS xCosto, @bpp := p.costo + (p.costo*(p.porcentajeUtilidad/100)) as bpp, @coefDesc := 1-(((p.porcentajeDescuentoProducto/100)-1)*((p.porcentajeDescuentoLinea/100)-1)*((p.porcentajeDescuentoGrupo/100)-1)*-1) as coefDesc, @montoImpuestos := (@bpp-(@bpp*@coefDesc))*(@sumImpuestos/100) montoImpuestos, @bpp-(@bpp*@coefDesc)+@montoImpuestos xPrecio FROM articulos a,base_para_precios p,lineas l, grupos g WHERE a.id_articulo = p.id_articulo AND a.id_linea = l.id_linea AND a.id_grupo = g.id_grupo AND a.activo = 1 AND p.id_articulo = a.id_articulo ";

        boolean xCodDes = getBuscarCodigoDescripcion();
        boolean xLineas = getBuscarLineas();
        boolean xGrupos = getBuscarGrupos();
        String busqueda = this.txtBusqueda.getText();

        //Limpia la cadena de búsqueda
        busqueda = StringEscapeUtils.escapeSql(busqueda);

        if(busqueda==null) { xCodDes = xLineas = xGrupos = false; }

        if(xCodDes || xLineas || xGrupos) { query = MessageFormat.format("{0} AND ", query); }
        if(xCodDes) {
            query = MessageFormat.format("{0} a.id_articulo IN (SELECT a.id_articulo FROM articulos a LEFT JOIN codigo_producto as b ON a.id_articulo = b.producto_id_articulo WHERE (a.descripcion like ''%{1}%'' or a.codigo like ''%{2}%'' or b.codigo like ''%{3}%'')) ", query, busqueda, busqueda, busqueda);
        }
        if(xCodDes && (xLineas || xGrupos)) { query = MessageFormat.format("{0}OR ", query); }
        if(xLineas) {
            query = MessageFormat.format("{0}(l.descripcion like ''%{1}%'' ) ", query, busqueda);
        }
        if((xLineas||xCodDes) && xGrupos) { query = MessageFormat.format("{0}OR ", query); }
        if(xGrupos) {
            query = MessageFormat.format("{0}(g.descripcion like ''%{1}%'' ) ", query, busqueda);
        }
        query = MessageFormat.format("{0}GROUP BY a.id_articulo ", query);

        return query;
    }

    Rectangle lastBounds;
    public Boolean areSameBounds(Rectangle r) {
        if(lastBounds == null) { lastBounds = r; return false; }
        else if(lastBounds.equals(r)) { return true; }
        else { lastBounds = r; return false; }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnDetalles;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnNuevo;
    public javax.swing.JProgressBar cargaProgressBar;
    private javax.swing.JCheckBox chkCodigo;
    private javax.swing.JCheckBox chkGrupos;
    private javax.swing.JCheckBox chkLineas;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JTable jTable1;
    public javax.swing.JTextField txtBusqueda;
    // End of variables declaration//GEN-END:variables

    public JButton getBtnCerrar() {
        return btnCerrar;
    }
}

class ArticulosTableModel extends ScrollableTableModel {

    public int IDAlmacen = omoikane.principal.Principal.IDAlmacen;
    NumberFormat numberFormat;

    public ArticulosTableModel(java.util.List colNames, ArrayList colClases) {
        super(colNames, colClases);
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setGroupingUsed(false);

    }
    /*
    @Override
    public String getJdbcUrl() {
        if(Principal.HA) {
            return "jdbc:mysql://localhost/store001?user=root&password=";
        } else {
            return super.getJdbcUrl();
        }
    } */

    public Object getValueAt(int row,int col){
        if(col==5) {
            try {
                BaseParaPrecio bp = new BaseParaPrecio();
                bp.setCosto((Double) super.getValueAt(row, 7));
                bp.setPorcentajeDescuentoLinea((Double) super.getValueAt(row, 9));
                bp.setPorcentajeDescuentoGrupo((Double) super.getValueAt(row, 10));
                bp.setPorcentajeDescuentoProducto((Double) super.getValueAt(row, 11));
                bp.setPorcentajeUtilidad((Double) super.getValueAt(row, 12));

                //Agrupo todos los impuestos en uno mismo, dado que la sumatoria de los impuestos es equivalente a calcular los impuestos por separado,
                // ésta facilidad proviene de la regla en México de que un impuesto no pueda estár gravado por otro
                Collection<Impuesto> impuestos = new ArrayList<>();
                Impuesto impuesto = new Impuesto();
                impuesto.setPorcentaje((BigDecimal) super.getValueAt(row, 14));
                impuestos.add(impuesto);

                PrecioOmoikaneLogic precioOmoikaneLogic = new PrecioOmoikaneLogic(bp, impuestos);

                return numberFormat.format(precioOmoikaneLogic.getPrecio());
            } catch(ClassCastException cce) {
                //cce.printStackTrace();
                System.out.println("Error(1): No se pudo hacer cast en CatalogoArticulo.getValueAt");
                return "ERROR(1)";
            }
        } else if(col == 6) {
            try {
                BigDecimal existencias = (BigDecimal) super.getValueAt(row, 13);
                String display = existencias.compareTo(BigDecimal.ZERO) <= 0 ? "N/D" : numberFormat.format(existencias);
                return display;
            } catch(Exception e) {
                //e.printStackTrace();
                System.out.println("Error(2) Al intentar generar la sexta columna del catálogo (existencias).");
                return "ERROR(2)";
            }
        } else {
            return super.getValueAt(row,col);
        }
    }

}