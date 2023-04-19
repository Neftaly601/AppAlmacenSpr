package com.almacen.alamacen202.SetterandGetters;

public class RecepConten {
    private String num;
    private String producto;
    private String cantidad;
    private String paletCaja;
    private String prioridad;

    public RecepConten(String  num, String producto, String cantidad, String paletCaja,String prioridad) {
        this.num = num;
        this.producto = producto;
        this.cantidad = cantidad;
        this.paletCaja = paletCaja;
        this.prioridad = prioridad;
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
}//clase
