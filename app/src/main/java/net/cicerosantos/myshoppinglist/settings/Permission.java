package net.cicerosantos.myshoppinglist.settings;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permission {

    //recebe um arraay de string
    public static boolean validarPermissoes(String[] permissoes, Activity activity, int requestCode){

        //apenas solicita permissão para versão maior que marchmelonn
        if ( Build.VERSION.SDK_INT >= 23 ){

            List<String> listaPermissoes = new ArrayList<>();

            /*Percorre as permissoes passadas, verifica uma auma se ja tem permissão liberada*/
            for ( String permissao : permissoes ){
                //checa se as permssoes foram aceita na activity que solicitou
                Boolean temPermissao = ContextCompat.checkSelfPermission( activity, permissao ) == PackageManager.PERMISSION_GRANTED;

                //caso a permissao nao tenha sido aceita, ela e adicionada
                if (!temPermissao){ listaPermissoes.add(permissao); }
            }
            //CASOA LISTA ESTEJA VAZIA, NÃO É NECESSARIO SOLICITAR PERMISSOA
            if (listaPermissoes.isEmpty()){return true;}
            String[] novasPermissoes = new String[ listaPermissoes.size() ];
            listaPermissoes.toArray( novasPermissoes );

            //solicita permissao
            ActivityCompat.requestPermissions( activity, novasPermissoes, requestCode );
        }

        return true;
    }

}
