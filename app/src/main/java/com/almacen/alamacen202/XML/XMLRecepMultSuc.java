package com.almacen.alamacen202.XML;

import com.almacen.alamacen202.SetterandGetters.Traspasos;

import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.ArrayList;

public class XMLRecepMultSuc extends SoapSerializationEnvelope {

    private String usuario;
    private String contrasena;
    private String suc;
    private ArrayList<Traspasos> datos;

    public XMLRecepMultSuc(int version) {
        super(version);
    }

    public void XMLTrasp(String usuario, String contrasena,String suc,ArrayList<Traspasos> datos) {
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.suc = suc;
        this.datos=datos;
    }//void

    @Override
    public void write(XmlSerializer writer) throws IOException {
        env = "http://schemas.xmlsoap.org/soap/envelope/";
        String tem = "";
        writer.startDocument("UTF-8", true);
        writer.setPrefix("soap", env);
        writer.setPrefix("", tem);
        writer.startTag(env, "Envelope");
        writer.startTag(env, "Body");
        writer.startTag(tem, "RecepcionMultisucursalRequest");


        writer.startTag(tem, "Login");
        writer.startTag(tem, "user");
        writer.text(usuario);
        writer.endTag(tem, "user");
        writer.startTag(tem, "pass");
        writer.text(contrasena);
        writer.endTag(tem, "pass");
        writer.endTag(tem, "Login");


        writer.startTag(tem, "RecepMultiSuc");

        writer.startTag(tem, "k_Sucursal");
        writer.text(suc);
        writer.endTag(tem, "k_Sucursal");

        writer.startTag(tem, "k_Usuario");
        writer.text(usuario);
        writer.endTag(tem, "k_Usuario");

        for(int i=0;i<datos.size();i++){
            writer.startTag(tem, "Datos");
            writer.startTag(tem, "k_Producto");
            writer.text(datos.get(i).getProducto());
            writer.endTag(tem, "k_Producto");

            writer.startTag(tem, "k_Cantidad");
            writer.text(datos.get(i).getCantSurt());
            writer.endTag(tem, "k_Cantidad");
            writer.endTag(tem, "Datos");
        }

        writer.endTag(tem, "RecepMultiSuc");

        writer.endTag(tem, "RecepcionMultisucursalRequest");
        writer.endTag(env, "Body");
        writer.endTag(env, "Envelope");
        writer.endDocument();

    }//vrite
}
