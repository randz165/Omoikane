package omoikane.entities;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: octavioruizcastillo
 * Date: 14/07/11
 * Time: 04:04
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Data
public class Preferencia {
    @Id
    @NotEmpty   private String clave;

    @NotEmpty   private String valor;

}
