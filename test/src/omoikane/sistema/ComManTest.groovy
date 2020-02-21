package omoikane.sistema;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by octavioruizcastillo on 17/01/16.
 */
public class ComManTest {

    @Test
    public void testMaskWeight() throws Exception {
        ComMan comMan = new ComMan("");
        String lecturaBascula = "";
        def mask = /[A-Z]{2,2},[A-Z]{2,2},.{2,2},(?<peso>.{8,8}).{5,5}/;
        def rawWeight = "ST,NT,01,00140000 kgRQ";
        def weight = comMan.maskWeight(rawWeight, mask);
        assertEquals("00140000", weight);

    }

    @Test
    public void testMaskWeight2() throws Exception {
        ComMan comMan = new ComMan("");
        String lecturaBascula = "";
        def mask = null;
        def rawWeight = "00.475KG";
        def weight = comMan.maskWeight(rawWeight, mask);
        assertEquals("00.475", weight);

    }
}
