package com.almacen.alamacen202.SetterandGetters;

public class RecepConten {
    private String num;
    private String producto;
    private String cantidad;
    private String paletCaja;
    private String prioridad;
    private String escanMtrz;
    private String escanCdmx;
    private String escanCul;
    private String escanMty;

    public RecepConten(String  num, String producto, String cantidad, String paletCaja,String prioridad,
                       String escanMtrz,String escanCdmx,String escanCul,String escanMty) {
        this.num = num;
        this.producto = producto;
        this.cantidad = cantidad;
        this.paletCaja = paletCaja;
        this.prioridad = prioridad;
        this.escanMtrz = escanMtrz;
        this.escanCdmx = escanCdmx;
        this.escanCul = escanCul;
        this.escanMty = escanMty;
    }//constructor

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public String getPaletCaja() {
        return paletCaja;
    }

    public void setPaletCaja(String paletCaja) {
        this.paletCaja = paletCaja;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getEscanMtrz() {
        return escanMtrz;
    }

    public void setEscanMtrz(String escanMtrz) {
        this.escanMtrz = escanMtrz;
    }

    public String getEscanCdmx() {
        return escanCdmx;
    }

    public void setEscanCdmx(String escanCdmx) {
        this.escanCdmx = escanCdmx;
    }

    public String getEscanCul() {
        return escanCul;
    }

    public void setEscanCul(String escanCul) {
        this.escanCul = escanCul;
    }

    public String getEscanMty() {
        return escanMty;
    }

    public void setEscanMty(String escanMty) {
        this.escanMty = escanMty;
    }
}//clase
