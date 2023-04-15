package com.almacen.alamacen202.SetterandGetters;

public class RecepConten {
    private String num;
    private String producto;
    private String cantidad;
    private String paletCaja;

    public RecepConten(String  num, String producto, String cantidad, String paletCaja) {
        this.num = num;
        this.producto = producto;
        this.cantidad = cantidad;
        this.paletCaja = paletCaja;
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
}//clase
