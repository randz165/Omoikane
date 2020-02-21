package omoikane.multisucursal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

import java.math.BigDecimal;
import java.text.MessageFormat;


@Data
public class ArticuloMultisucursal {

    @JsonProperty("Sucursal")
    String nombreSucursal;
    @JsonProperty("Precio")
    BigDecimal precio;
    @JsonProperty("Línea")
    String linea;
    @JsonProperty("Código")
    String codigo;
    @JsonProperty("Impuestos")
    String impuestos;
    @JsonProperty("Costo")
    BigDecimal costo;
    @JsonProperty("Descripción")
    String descripcion;

    public String getCodigoMasDescripcion() {
        return MessageFormat.format("{0}\n{1}", getCodigo(), getDescripcion());
    }

    public String getLineaEImpuestos() {
        return MessageFormat.format("{0}\n{1}", getLinea(), getImpuestos());
    }

}
