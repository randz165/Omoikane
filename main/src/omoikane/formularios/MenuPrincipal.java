/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MenuPrincipal.java
 *
 * Created on 15/08/2008, 11:31:38 AM
 */

package omoikane.formularios;

import javax.swing.*;
import java.awt.image.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

import omoikane.caja.CajaManager;
import omoikane.configuracion.ConfiguratorAppManager;
import omoikane.etiquetas.ImpresionEtiquetasManager;
import omoikane.inventarios.tomaInventario.ConteoInventarioManager;
import omoikane.mepro.Mepro;
import omoikane.moduloreportes.MenuOmoikane;
import omoikane.proveedores.ProveedoresManager;
import omoikane.sistema.Herramientas;
import omoikane.sistema.StopWatch;

/**
 *
 * @author Octavio
 */
public class MenuPrincipal extends OmJInternalFrame {


    /** Creates new form MenuPrincipal */
    public MenuPrincipal() {
        
        initComponents();

        //Instrucciones para el funcionamiento del fondo semistransparente
        this.setOpaque(false);
        ((JPanel)this.getContentPane()).setOpaque(false);
        this.getLayeredPane().setOpaque(false);
        this.getRootPane().setOpaque(false);
        this.generarFondo();
        
        //Instrucciones para el funcionamiento de las teclas de navegación
        Set newKeys = new HashSet(getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        newKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, newKeys);

        newKeys = new HashSet(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        newKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newKeys);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = this.getPreferredSize();

    }

    /** This method is called from within the constructor to
     *< initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        btnCerrar = new javax.swing.JButton();
        btnVender = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        btnCajas = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        btnAlmacenes = new javax.swing.JButton();
        btnMovAlmacen = new javax.swing.JButton();
        btnPreferencias = new javax.swing.JButton();
        btnUsuarios = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnLineas = new javax.swing.JButton();
        btnMepro = new javax.swing.JButton();
        btnCortes = new javax.swing.JButton();
        btnArticulos = new javax.swing.JButton();
        btnTomaInventarios = new javax.swing.JButton();
        btnGrupos = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JSeparator();
        btnDetallesVentas = new javax.swing.JButton();
        btnEtiquetas = new javax.swing.JButton();
        lblVersion = new javax.swing.JLabel();
        btnReportes = new javax.swing.JButton();
        btnProveedores = new javax.swing.JButton();
        btnTomaInventarios1 = new javax.swing.JButton();

        setIconifiable(true);
        setTitle("Menú Principal");
        setFocusCycleRoot(false);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Arial", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("<html><head><style type='text/css'>body { font-family: 'Roboto Thin'; font-size: 36px; }</style></head>\n<body>\nMenú Principal\n</body></html>");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 600, -1));

        btnCerrar.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnCerrar.setText("<html><head><style type='text/css'>body { font-family: 'Roboto Thin'; font-size: 24px; }.icon { font-family: 'linecons'; font-size: 24px; } </style></head>\n<body>\n<span class=\"icon\">&#xe01e;</span>\nCerrar\n</span></body></html>");
        btnCerrar.setIconTextGap(-5);
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });
        getContentPane().add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 10, 200, 70));

        btnVender.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnVender.setText("Vender");
        btnVender.setIconTextGap(-5);
        btnVender.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVenderActionPerformed(evt);
            }
        });
        getContentPane().add(btnVender, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 210, 70));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Artículos");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, -1));

        btnCajas.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnCajas.setText("Cajas");
        btnCajas.setIconTextGap(-5);
        btnCajas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCajasActionPerformed(evt);
            }
        });
        getContentPane().add(btnCajas, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 110, 200, 70));
        getContentPane().add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 840, 10));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Ventas");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Almacen");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, -1, -1));
        getContentPane().add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 840, 10));

        btnAlmacenes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnAlmacenes.setText("Almacenes");
        btnAlmacenes.setIconTextGap(-30);
        btnAlmacenes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlmacenesActionPerformed(evt);
            }
        });
        getContentPane().add(btnAlmacenes, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 310, 210, 70));

        btnMovAlmacen.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnMovAlmacen.setText("<html>Movimientos<br>Almacén</html>");
        btnMovAlmacen.setIconTextGap(-25);
        btnMovAlmacen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMovAlmacenActionPerformed(evt);
            }
        });
        getContentPane().add(btnMovAlmacen, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 310, 200, 70));

        btnPreferencias.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnPreferencias.setText("Config.");
        btnPreferencias.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPreferencias.setIconTextGap(-12);
        btnPreferencias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigActionPerformed(evt);
            }
        });
        getContentPane().add(btnPreferencias, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 410, 90, 70));

        btnUsuarios.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnUsuarios.setText("Usuarios");
        btnUsuarios.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnUsuarios.setIconTextGap(-12);
        btnUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuariosActionPerformed(evt);
            }
        });
        getContentPane().add(btnUsuarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 410, 210, 70));
        getContentPane().add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 400, 840, 10));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Administración");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 380, -1, -1));

        jPanel1.setOpaque(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 30, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 40, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 480, 30, 40));

        btnLineas.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnLineas.setText("Líneas");
        btnLineas.setIconTextGap(-5);
        btnLineas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLineasActionPerformed(evt);
            }
        });
        getContentPane().add(btnLineas, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 210, 200, 70));

        btnMepro.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnMepro.setText("<html>Scripting</html>");
        btnMepro.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnMepro.setIconTextGap(-5);
        btnMepro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMeproActionPerformed(evt);
            }
        });
        getContentPane().add(btnMepro, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 410, 100, 70));

        btnCortes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnCortes.setText("Cortes");
        btnCortes.setIconTextGap(-5);
        btnCortes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCortesActionPerformed(evt);
            }
        });
        getContentPane().add(btnCortes, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 110, 200, 70));

        btnArticulos.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnArticulos.setText("Catálogo");
        btnArticulos.setIconTextGap(-25);
        btnArticulos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnArticulosActionPerformed(evt);
            }
        });
        getContentPane().add(btnArticulos, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 210, 70));

        btnTomaInventarios.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnTomaInventarios.setText("Compras");
        btnTomaInventarios.setIconTextGap(-5);
        btnTomaInventarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTomaInventariosActionPerformed(evt);
            }
        });
        getContentPane().add(btnTomaInventarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 410, 200, 70));

        btnGrupos.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnGrupos.setText("Grupos");
        btnGrupos.setIconTextGap(-15);
        btnGrupos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGruposActionPerformed(evt);
            }
        });
        getContentPane().add(btnGrupos, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 210, 200, 70));
        getContentPane().add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, 840, 10));

        btnDetallesVentas.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnDetallesVentas.setText("<html>Detalles<br>de Ventas</html>");
        btnDetallesVentas.setIconTextGap(-5);
        btnDetallesVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDetallesVentasActionPerformed(evt);
            }
        });
        getContentPane().add(btnDetallesVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 110, 200, 70));

        btnEtiquetas.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnEtiquetas.setText("Etiquetas");
        btnEtiquetas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEtiquetasActionPerformed(evt);
            }
        });
        getContentPane().add(btnEtiquetas, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 210, 200, 70));

        lblVersion.setForeground(new java.awt.Color(255, 255, 255));
        lblVersion.setText("<html><head><style type='text/css'>body { font-family: 'Roboto Thin'; font-size: 10px; }</style></head> <body> Versión </span></body></html>");
        getContentPane().add(lblVersion, new org.netbeans.lib.awtextra.AbsoluteConstraints(727, 490, 140, 20));

        btnReportes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnReportes.setText("<html>Reportes</html>");
        btnReportes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReportes.setIconTextGap(-5);
        btnReportes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReportesActionPerformed(evt);
            }
        });
        getContentPane().add(btnReportes, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 410, 200, 70));

        btnProveedores.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnProveedores.setText("Proveedores");
        btnProveedores.setIconTextGap(-5);
        btnProveedores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProveedoresActionPerformed(evt);
            }
        });
        getContentPane().add(btnProveedores, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 310, 200, 70));

        btnTomaInventarios1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnTomaInventarios1.setText("Toma de Inventarios");
        btnTomaInventarios1.setIconTextGap(-5);
        btnTomaInventarios1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTomaInventarios1ActionPerformed(evt);
            }
        });
        getContentPane().add(btnTomaInventarios1, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 310, 200, 70));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        // TODO add your handling code here:
//        this.dispose();
//        ((omoikane.principal.Escritorio)omoikane.principal.Principal.getEscritorio()).getFrameEscritorio().dispose();
//        System.exit(0);
}//GEN-LAST:event_btnCerrarActionPerformed

    private void btnAlmacenesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlmacenesActionPerformed
        omoikane.principal.Almacenes.lanzarCatalogo();
}//GEN-LAST:event_btnAlmacenesActionPerformed

    private void btnCajasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCajasActionPerformed
        omoikane.principal.Caja.lanzarCatalogo();
}//GEN-LAST:event_btnCajasActionPerformed

    private void btnMovAlmacenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMovAlmacenActionPerformed
        StopWatch timer = new StopWatch().start();
        omoikane.principal.Almacenes.lanzarMovimientos();
        System.out.println(timer.getElapsedTime());
}//GEN-LAST:event_btnMovAlmacenActionPerformed

    private void btnLineasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLineasActionPerformed
        StopWatch timer = new StopWatch().start();
        omoikane.principal.Lineas.lanzarCatalogo();
        System.out.println(timer.getElapsedTime());
}//GEN-LAST:event_btnLineasActionPerformed

    private void btnVenderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVenderActionPerformed
        StopWatch timer = new StopWatch().start();
        //omoikane.principal.Caja.lanzar(); //Caja antigua

        CajaManager manager = new CajaManager();
        JInternalFrame internalFrame = manager.startJFXCaja();
        omoikane.principal.Principal.getEscritorio().getPanelEscritorio().setSelectedFrame(internalFrame);

        System.out.println(timer.getElapsedTime());
}//GEN-LAST:event_btnVenderActionPerformed


    private void btnCortesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCortesActionPerformed
        StopWatch timer = new StopWatch().start();
        omoikane.principal.Cortes.lanzarCatalogoSuc();
        System.out.println(timer.getElapsedTime());
}//GEN-LAST:event_btnCortesActionPerformed

    private void btnArticulosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnArticulosActionPerformed
        StopWatch timer = new StopWatch().start();
        omoikane.principal.Articulos.lanzarCatalogo();
        System.out.println(timer.getElapsedTime());
}//GEN-LAST:event_btnArticulosActionPerformed

    private void btnTomaInventariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTomaInventariosActionPerformed
        omoikane.compras.CompraManager cm = new omoikane.compras.CompraManager();
        JInternalFrame frame = cm.startJFXCompra();
}//GEN-LAST:event_btnTomaInventariosActionPerformed

    private void btnUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuariosActionPerformed
        omoikane.principal.Usuarios.lanzarCatalogo();
}//GEN-LAST:event_btnUsuariosActionPerformed

    private void btnGruposActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGruposActionPerformed
        StopWatch timer = new StopWatch().start();
        omoikane.principal.Grupos.lanzarCatalogo();
        System.out.println(timer.getElapsedTime());
}//GEN-LAST:event_btnGruposActionPerformed

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        this.btnVender.requestFocusInWindow();
}//GEN-LAST:event_formFocusGained

    private void btnDetallesVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetallesVentasActionPerformed
        // TODO add your handling code here:
        StopWatch timer = new StopWatch().start();
        omoikane.principal.Ventas.lanzarCatalogo();
        System.out.println(timer.getElapsedTime());
    }//GEN-LAST:event_btnDetallesVentasActionPerformed

    private void btnConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfigActionPerformed
        ConfiguratorAppManager manager = new ConfiguratorAppManager();
        JInternalFrame frame = manager.startJFXConfigurator();
    }//GEN-LAST:event_btnConfigActionPerformed

    private void btnReportesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReportesActionPerformed
        MenuOmoikane menuOmoikane = new MenuOmoikane();
        menuOmoikane.launch();
    }//GEN-LAST:event_btnReportesActionPerformed

    private void btnProveedoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProveedoresActionPerformed
        ProveedoresManager pm = new ProveedoresManager();
        JInternalFrame frame = pm.startJFXProveedores();
    }//GEN-LAST:event_btnProveedoresActionPerformed

    private void btnTomaInventarios1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTomaInventarios1ActionPerformed
        ConteoInventarioManager im = new ConteoInventarioManager();
        JInternalFrame frame = im.startJFXTomaInventario();
    }//GEN-LAST:event_btnTomaInventarios1ActionPerformed


    private void btnEtiquetasActionPerformed(java.awt.event.ActionEvent evt) {                                         

        ImpresionEtiquetasManager manager = new ImpresionEtiquetasManager();
        JInternalFrame internalFrame = manager.startJFXEtiqueta();

    }

    private void btnMeproActionPerformed(java.awt.event.ActionEvent evt) {                                         

        Mepro mepro = new Mepro();
        JFrame frameMepro = (JFrame) mepro.getMainMenu();
        frameMepro.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAlmacenes;
    private javax.swing.JButton btnArticulos;
    private javax.swing.JButton btnCajas;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnCortes;
    private javax.swing.JButton btnDetallesVentas;
    private javax.swing.JButton btnEtiquetas;
    private javax.swing.JButton btnGrupos;
    private javax.swing.JButton btnLineas;
    public javax.swing.JButton btnMepro;
    private javax.swing.JButton btnMovAlmacen;
    private javax.swing.JButton btnPreferencias;
    private javax.swing.JButton btnProveedores;
    public javax.swing.JButton btnReportes;
    private javax.swing.JButton btnTomaInventarios;
    private javax.swing.JButton btnTomaInventarios1;
    private javax.swing.JButton btnUsuarios;
    private javax.swing.JButton btnVender;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    public javax.swing.JLabel lblVersion;
    // End of variables declaration//GEN-END:variables

}
