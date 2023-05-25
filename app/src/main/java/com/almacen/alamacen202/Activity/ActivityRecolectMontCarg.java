package com.almacen.alamacen202.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.almacen.alamacen202.R;
import com.almacen.alamacen202.SetterandGetters.ComprometidasSandG;
import com.almacen.alamacen202.SetterandGetters.UbicacionSandG;
import com.almacen.alamacen202.XML.XMLActualizaOrdenCompra;
import com.almacen.alamacen202.XML.XMLCLArticulo;
import com.almacen.alamacen202.XML.XMLCompromeAlma;
import com.almacen.alamacen202.XML.XMLConsultaXprod;
import com.almacen.alamacen202.XML.XMLUbicacionAlma;
import com.almacen.alamacen202.includes.MyToolbar;
import com.squareup.picasso.Picasso;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import dmax.dialog.SpotsDialog;

public class ActivityRecolectMontCarg extends AppCompatActivity {
    private EditText txtProd,txtDem,txtConsolid,txtTotUbi,txtExistAlmProd,txtCantAcum;
    private Button btnGuarda,btnTrasl;
    private TextView tvClvProdD,tvDescProdD;
    private ImageView ivProdIm;
    private SharedPreferences preference;
    private String strusr,strpass,strbran,strServer,codeBar,clvProducto,Descripcion,mensaje="",Ubicacion,Cantidad,SumUbic,SumAlm,cantAlm;
    private AlertDialog mDialog;
    private ArrayList<UbicacionSandG> listaUbicaciones = new ArrayList<>();
    private ArrayList<ComprometidasSandG> listaComprometidas = new ArrayList<>();
    boolean v=false;
    private String urlImagenes,extImg;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reccolec_mont_carga);

        MyToolbar.show(this, "Recolección Montacargas", true);
        preference = getSharedPreferences("Login", Context.MODE_PRIVATE);
        strusr = preference.getString("user", "null");
        strpass = preference.getString("pass", "null");
        strbran = preference.getString("codBra", "null");
        strServer = preference.getString("Server", "null");
        codeBar = preference.getString("codeBar", "null");
        urlImagenes=preference.getString("urlImagenes", "null");
        extImg=preference.getString("ext", "null");

        mDialog = new SpotsDialog.Builder().setContext(ActivityRecolectMontCarg.this).
                setMessage("Espere un momento...").build();

        /*txtProd   = findViewById(R.id.txtProd);
        tvClvProdD      = findViewById(R.id.tvClvProdR);
        tvDescProdD     = findViewById(R.id.tvDescProdR);

        txtDem         = findViewById(R.id.txtDem);
        txtConsolid    = findViewById(R.id.txtConsolid);*/

        txtProd.setInputType(InputType.TYPE_NULL);
        txtProd.requestFocus();

        txtProd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().equals("")) {
                    if (codeBar.equals("Zebra")) {
                        //firtMet();
                        txtProd.setText("");
                    } else {
                        for (int i = 0; i < editable.length(); i++) {
                            char ban;
                            ban = editable.charAt(i);
                            if (ban == '\n') {
                                //firtMet();
                                txtProd.setText("");
                            }//if
                        }//for
                    }//else
                }//if
            }//afterTextChange
        });


    }//onCreate
}//clase principal
