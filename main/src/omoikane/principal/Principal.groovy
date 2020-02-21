/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package omoikane.principal

import airbrake.AirbrakeAppender
import groovy.inspect.swingui.ObjectBrowser
import groovy.swing.SwingBuilder
import javafx.application.Application
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import omoikane.configuracion.ConfiguratorAppManager
import omoikane.formularios.CatalogoArticulos
import omoikane.ha.DisconnectionHandler
import omoikane.mepro.FramePrincipal
import omoikane.repository.CajaRepo
import omoikane.repository.PreferenciaRepo
import omoikane.repository.UsuarioRepo
import omoikane.sistema.*
import omoikane.sistema.Usuarios as SisUsuarios

import omoikane.sistema.cortes.ContextoCorte
import org.apache.log4j.Appender
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.spi.Filter
import org.apache.log4j.spi.LoggingEvent
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.flywaydb.core.api.MigrationInfo
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.context.ApplicationContext
import omoikane.exceptions.UEHandler
import omoikane.sistema.huellas.ContextoFPSDK.SDK
import omoikane.sistema.huellas.HuellasCache
import omoikane.sistema.seguridad.AuthContext
import phesus.configuratron.ConfiguratorApp
import phesus.configuratron.model.TipoImpresora

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.swing.Action
import javax.swing.BoxLayout
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JInternalFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingUtilities
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.beans.PropertyChangeListener
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import java.util.concurrent.CountDownLatch

/**
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////////////////////////////////////////////////////////
 *
 * 
 *  * @author Octavio
 */
public class Principal {
        static Escritorio escritorio
        static MenuPrincipal        menuPrincipal;
        static omoikane.sistema.Config      config
        private static def splash;
        public static int                   IDAlmacen
        public static int                   IDCaja
        public static int                   sysAncho
        public static int                   sysAlto
        public static int                   CacheSTableAtras
        public static int                   CacheSTableAdelante
        public static boolean               fondoBlur               = true
        public static String                puertoImpresion
        public static boolean               impresoraActiva
        public static boolean               scannerActivo
        public static boolean               basculaActiva
        public static HashMap               driverBascula
        public static String                URLMySQL
        public static String                loginJasper
        public static String                passJasper
        public static int                   scannerBaudRate
        public static String                scannerPort
        public static SDK                   sdkFingerprint = SDK.ONETOUCH;
        public static ShutdownHandler       shutdownHandler
        public static def                   toFinalizeTracker       = [:]
        public static def                   scanMan
        public static def                   tipoCorte               = ContextoCorte.TIPO_DUAL
        public final  static Boolean        ASEGURADO               = true
        final  static def                   SHOW_UNHANDLED_EXCEPTIONS = true
        public static Logger                logger                  = Logger.getLogger(Principal.class);
        public static ApplicationContext    applicationContext;
        public static final Boolean         DEBUG                   = false
        public static final String          VERSION                 = "4.5.5";
        public static  Boolean              HA                      = false; //Características de alta disponibilidad
        public static def                   authType                = AuthContext.AuthType.NIP;
        public static String                nombreImpresora
        public static TipoImpresora         tipoImpresora
        public static String                loginJasperserver       = ""
        public static String                passJasperserver        = ""
        public static String                urlJasperserver         = ""
        public static boolean               multiSucursal           = false
        public static String                configFilePath          = "config.xml";
        public static boolean               isFlywayActive          = true;
        public static boolean               modoKiosko              = false;
        public static String                dropboxPath             = "";
        public static String                urlStatusWS             = "";
        public static String                userStatusWS            = "";
        public static String                passStatusWS            = "";


    public static void main(args)
        {
            logger.trace( "Prueba de codificación: áéíóú" )
            iniciar()
        }
        public static ApplicationContext getContext() {
            return applicationContext;
        }
        static iniciar()
        {
            try {

                logger.trace("Iniciando sistema. Versión " + VERSION);
                configExceptions()

                //Inicializa el hilo que muestra el splash
                Thread.start {
                    splash = new Splash()
                    splash.iniciar()
                }

                Locale.setDefault(new Locale("es", "MX"));

                shutdownHandler = new ShutdownHandler()
                Runtime.getRuntime().addShutdownHook(shutdownHandler);

                logger.trace("Cargando configuración...")
                config = new omoikane.sistema.Config(configFilePath)

                //Comportamiento multisucursal
                if(Principal.multiSucursal) multiSucursalGUI();

                logger.trace("Cargando ApplicationContext...")
                applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");

                /*
                    Verifico la conexión a la base de datos, si no hay se procede al comportamiento para casos sin conexión
                 */
                logger.trace("Verificando conexión con BD y migraciones...")
                try {
                    //Verificar conexión consultando tabla cajas
                    checkDatabaseAvailability();
                    //Verifica la conexión con la BD y la versión del esquema de la BD
                    if(isFlywayActive) checkDBMigrations();
                } catch (FlywayException fe) {
                    logger.error("Base de datos no actualizada para ésta versión de omoikane.", fe);
                } catch( Exception e ) {
                    logger.trace("Error relacionado con BD", e);
                    disconnectedBehavior();
                    return;
                }

                logger.trace("Cargando huellas en caché...")
                applicationContext.getBean(HuellasCache.class).getHuellasBD();

                logger.trace("Cargando configuración multisucursal status WS")
                loadMultisucursalWSConfig();

                logger.trace("Inicializando JavaFX")
                initJavaFx()

                logger.trace("Inicializando escritorio...");
                //Herramientas.utilImpresionCortes()
                //System.exit(0)
                escritorio = new Escritorio()
                escritorio.iniciar()
                logger.trace("Inicializando menú principal...")
                menuPrincipal = new MenuPrincipal()
                splash.detener()

                iniciarSesion()

                menuPrincipal.iniciar()

                if(scannerActivo){
                    scanMan = new DefaultScanMan()
                    try {
                        println "comienza intento de conexi?n"
                        scanMan.connect(Principal.scannerPort, Principal.scannerBaudRate)
                        println "fin intento de conexi?n"
                    } catch(Exception ex2) { Dialogos.error(ex2.getMessage(), ex2) }

                    toFinalizeTracker.put("scanMan", "")

                }
                logger.trace("Listo");

            } catch(e) {
                //Dialogos.lanzarDialogoError(null, "Al iniciar aplicación: ${e.message}", Herramientas.getStackTraceString(e))
                logger.error("Al iniciar aplicación: ${e.message}".toString(), e)
                System.exit(1);
            }
            ///////////////////////

        }

    static def loadMultisucursalWSConfig() {
        PreferenciaRepo repo = applicationContext.getBean(PreferenciaRepo.class);
        if(repo == null) {
            logger.error("No hay un WS multisucursal configurado");
            return ;
        }

        String urlStatusWS = repo.readByPrimaryKey("url_status_ws")?.valor
        String userStatusWS = repo.readByPrimaryKey("user_status_ws")?.valor
        String passStatusWS = repo.readByPrimaryKey("pass_status_ws")?.valor

        this.urlStatusWS = urlStatusWS
        this.userStatusWS = userStatusWS
        this.passStatusWS = passStatusWS
    }

    static def multiSucursalGUI() {

        def dir = new File('./')

        def swing;
        def mainMenu;
        def pnlScripts;
        swing = SwingBuilder.build {
            lookAndFeel( 'nimbus' )
            mainMenu = new JDialog(title:"Phesus Proxy Sucursales")
            mainMenu.setLayout(new FlowLayout());
            mainMenu.setModal(true)
        }

        //Los archivos de configuración de sucursales deben tener el nombre
        // de la sucursal y la doble extensión .config.xml. Por ejemplo "centro.config.xml".
        dir.eachFileMatch(~/(.*\.config.xml)/) {
            //El nombre de la sucursal es el nombre el archivo sin extensiones
            def nombre  = it.name.split("\\.")[0]
            def archivo = it;

            mainMenu.add(
                    swing.panel() {
                        swing.button(
                                icon: imageIcon("/omoikane/Media2/icons/PNG/32/shop.png"),
                                text: nombre, actionCommand:it, actionPerformed:
                                {
                                    Principal.configFilePath = archivo
                                    logger.trace("Cargando configuración de sucursal elegida...")
                                    config = new omoikane.sistema.Config(configFilePath)
                                    mainMenu.show false
                                },
                                preferredSize:[130,35])
                        //swing.button(text: "GC", actionCommand:it, actionPerformed: { lanzarGC(archivo) })
                        //swing.button(icon: imageIcon("/omoikane/mepro/media/blog_post_edit.png"), actionCommand:it, actionPerformed: { modificarScript(archivo) })
                        //swing.button(icon: imageIcon("/omoikane/mepro/media/remove.png"), actionCommand:it, actionPerformed: { eliminarScript(archivo) } )
                    }
            )
        }
        mainMenu.pack()
        mainMenu.show true
    }
/**
     * Éste método desribe el comportamiento a seguir para el arranque del programa sin conexión a la BD.
     */
    static void disconnectedBehavior() {
        splash.detener();

        String[] options = [ "Abrir configuración", "Cerrar aplicación" ];
        int decision = JOptionPane.showOptionDialog(null, "Servidor caído ¿Que hacer?", "Servidor caído",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        switch(decision) {
            case 0:
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        omoikane.principal.Principal.setConfig(new omoikane.sistema.Config());
                        omoikane.principal.Principal.applicationContext = new ClassPathXmlApplicationContext("applicationContext-test.xml");

                        ConfiguratorAppManager manager = new ConfiguratorAppManager();
                        JInternalFrame frame = manager.startJFXConfigurator();
                        JFrame jFrame = new JFrame("Configurator");
                        jFrame.setContentPane(frame);
                        jFrame.setVisible(true);
                        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    }
                });

                break;
            case 1:
                System.exit(0);
                break;
        }

    }

    static void iniciarSistemaOffline() {

        escritorio = new Escritorio()
        escritorio.iniciar()

        CatalogoArticulos cat = new DisconnectionHandler().handle();
        cat.getBtnCerrar().removeAll();

        cat.getBtnCerrar().addActionListener(new ActionListener() {

            @Override
            void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        })
    }

    static void checkDatabaseAvailability() throws Exception {

        String protocol = omoikane.principal.Principal.URLMySQL +
                "?user="     + omoikane.principal.Principal.loginJasper +
                "&password=" + omoikane.principal.Principal.passJasper  +
                "&useOldAliasMetadataBehavior=true&useCompression=true";

        Connection conn = DriverManager.getConnection(protocol);

        Statement control   = conn.createStatement();
        control.executeQuery("SELECT CURRENT_TIMESTAMP();").first();
        control.close();

        conn.close();
    }

    static def iniciarSesion() throws Exception {
        try{
        while(!SisUsuarios.login().cerrojo(SisUsuarios.CAJERO)) {}  // Aquí se detendrá el programa a esperar login
        escritorio.setNombreUsuario(SisUsuarios.usuarioActivo.nombre)
        } catch(e) { Dialogos.lanzarDialogoError(null, "Error al iniciar sesión en ciclo de huella: "+e.getMessage(), Herramientas.getStackTraceString(e)) }
    }

    static def cerrarSesion(){
        try{
                SisUsuarios.logout()
                escritorio.setNombreUsuario("Sin Sesión")
                Principal.menuPrincipal = new MenuPrincipal()
                Thread.start(){
                Principal.iniciarSesion()
                Principal.menuPrincipal.iniciar()
                }
        } catch(e) { Dialogos.lanzarDialogoError(null, "Error al cerrar secion ciclo de huella", Herramientas.getStackTraceString(e)) }
    }

    static def configExceptions() {
        if(SHOW_UNHANDLED_EXCEPTIONS) {
            Thread.setDefaultUncaughtExceptionHandler(new UEHandler());
        }
    }

    static def initJavaFx() {
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new JFXPanel(); // initializes JavaFX environment
                latch.countDown();
            }
        });
        latch.await();
        Platform.setImplicitExit(false);
    }

    private static void initAWT() {
        System.setProperty("javafx.macosx.embedded", "true");
        java.awt.Toolkit.getDefaultToolkit();
    }

    /**
     * Comprueba base de datos y migraciones
     */
    public static void checkDBMigrations() {

        Flyway flyway = new Flyway();
        flyway.setDataSource(URLMySQL, loginJasper, passJasper);

        //Detectar si Flyway o esquema BD no inicializado
        if(flyway.info().current() == null) {
            splash.detener();
            String[] options = [ "Instalación nueva (desde 0)", "Actualización", "Cancelar" ];
            int decision = JOptionPane.showOptionDialog(null, "El esquema de la BD no ha sido inicializado. ¿Que tipo de instalación es esta?", "Inicializar BD",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[2]);
            splash.iniciar();

            if(decision == 0) {
                //Inicializar desde la primera versión (nueva instalación)
                flyway.setInitVersion("0");
                flyway.init();
                flyway.migrate();
            } else if(decision == 1) {
                // Inicializar en la última versión (BD legada, en funcionamiento)
                MigrationInfo[] mi = flyway.info().pending();
                MigrationInfo lastMigration = mi[mi.length-1];
                flyway.setInitVersion(lastMigration.getVersion());
                flyway.init();
                flyway.migrate();
            } else if(decision == 2) {
                //Hacer nada
                throw new FlywayException("Flyway no inicializado, la tabla de metadatos de flyway no exíste.");
            }
        }

        flyway.migrate();

    }
}
