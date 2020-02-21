package omoikane.multisucursal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import omoikane.principal.Principal;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ArticuloMultisucursalController implements Initializable {
    @FXML
    TableView<ArticuloMultisucursal> tableView;

    @FXML
    TableColumn<ArticuloMultisucursal, String> sucursalNombreColumn;

    @FXML
    TableColumn<ArticuloMultisucursal, BigDecimal> precioColumn;

    @FXML
    TableColumn<ArticuloMultisucursal, BigDecimal> costoColumn;

    @FXML
    TableColumn<ArticuloMultisucursal, String> descripcionColumn;

    @FXML
    TableColumn<ArticuloMultisucursal, String> impuestosColumn;

    @FXML
    TableColumn<ArticuloMultisucursal, String> lineaColumn;

    @FXML
    TableColumn<ArticuloMultisucursal, String> codigoColumn;

    public static final Logger logger = Logger.getLogger(ArticuloMultisucursalController.class);

    String urlStatusWS;
    String userStatusWS;
    String passStatusWS;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sucursalNombreColumn.setCellValueFactory(new PropertyValueFactory<ArticuloMultisucursal, String>("nombreSucursal"));
        descripcionColumn.setCellValueFactory(new PropertyValueFactory<ArticuloMultisucursal, String>("descripcionLineaImpuestos"));
        impuestosColumn.setCellValueFactory(new PropertyValueFactory<ArticuloMultisucursal, String>("impuestos"));
        lineaColumn.setCellValueFactory(new PropertyValueFactory<ArticuloMultisucursal, String>("lineaEImpuestos"));
        codigoColumn.setCellValueFactory(new PropertyValueFactory<ArticuloMultisucursal, String>("codigo"));
        precioColumn.setCellValueFactory(new PropertyValueFactory<ArticuloMultisucursal, BigDecimal>("precio"));
        costoColumn.setCellValueFactory(new PropertyValueFactory<ArticuloMultisucursal, BigDecimal>("costo"));

        urlStatusWS = Principal.urlStatusWS;
        userStatusWS = Principal.userStatusWS;
        passStatusWS = Principal.passStatusWS;
    }

    public void loadData(String searchString) {
        if(StringUtils.isBlank(urlStatusWS) || StringUtils.isBlank(userStatusWS) || StringUtils.isBlank(passStatusWS)) {
            logger.error("Webservice Status WS no configurado");
            return ;
        }

        try {
            fetchData(searchString);
        } catch (Exception e) {
            logger.error("No se pudo cargar la informaicón", e);
        }
    }

    private void fetchData(String codigo) throws IOException, InterruptedException {
        List<Map<String, Object>> sucursales = fetchSucursales();

        ExecutorService executorService = Executors.newFixedThreadPool(sucursales.size());
        for(Map<String, Object> sucursal : sucursales) {
            executorService.submit(() -> {
                Integer sucursalId = (Integer) sucursal.get("@id");
                try {
                    ArticuloMultisucursal articuloMultisucursal = fetchArticuloByCodigo(sucursalId, codigo);
                    tableView.getItems().add(articuloMultisucursal);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.debug("Error al cargar datos de la sucursal "+sucursal.get("nombre"));
                }
            });

        }

        executorService.shutdown();
        //executorService.awaitTermination(1, TimeUnit.MINUTES);

    }

    private List<Map<String, Object>> fetchSucursales() throws IOException {
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder()
                .nonPreemptive()
                .credentials(userStatusWS, passStatusWS)
                .build();

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(feature) ;

        Client c = ClientBuilder.newClient(clientConfig);
        String json = c.target(MessageFormat.format("{0}/api/sucursales-plain", urlStatusWS)).request().get(String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        List data = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>(){});
        return data;
    }

    private ArticuloMultisucursal fetchArticuloByCodigo(Integer sucursalId, String codigo) throws IOException {
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder()
                .nonPreemptive()
                .credentials(userStatusWS, passStatusWS)
                .build();

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(feature) ;

        Client c = ClientBuilder.newClient(clientConfig);
        String url = MessageFormat.format("{0}/api/articulo-por-codigo/{1}?codigo={2}", urlStatusWS, sucursalId, codigo);
        System.out.println("URL: "+url);
        String json = c.target(url).request().get(String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        ArticuloMultisucursal data = objectMapper.readValue(json, new TypeReference<ArticuloMultisucursal>(){});
        return data;

    }
}
