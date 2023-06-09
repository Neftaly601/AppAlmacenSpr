package com.almacen.alamacen202.XML;

import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class XMLValidEsc extends SoapSerializationEnvelope {

    private String usuario;
    private String contrasena;
    private String producto;
    private String sucursal;

    public XMLValidEsc(int version) {
        super(version);
    }

    public void XMLValid(String usuario, String contrasena,String producto,String sucursal) {
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.producto = producto;
        this.sucursal = sucursal;
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
        writer.startTag(tem, "ValidarEscanInvRequest");


        writer.startTag(tem, "Login");
        writer.startTag(tem, "user");
        writer.text(usuario);
        writer.endTag(tem, "user");
        writer.startTag(tem, "pass");
        writer.text(contrasena);
        writer.endTag(tem, "pass");
        writer.endTag(tem, "Login");


        writer.startTag(tem, "ValidarEscanInvv");

        writer.startTag(tem, "k_Producto");
        writer.text(producto);
        writer.endTag(tem, "k_Producto");

        writer.startTag(tem, "k_Sucursal");
        writer.text(sucursal);
        writer.endTag(tem, "k_Sucursal");

        writer.endTag(tem, "ValidarEscanInvv");


        writer.endTag(tem, "ValidarEscanInvRequest");
        writer.endTag(env, "Body");
        writer.endTag(env, "Envelope");
        writer.endDocument();

    }//vrite
}
