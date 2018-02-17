package org.netzd.audiomusicdip;


import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.List;


public class ReproductorFragment extends Fragment implements MediaController.MediaPlayerControl{

    private OnToolbarListener onToolbarListener = null;

    public static final int WRITE_EXTERNAL_PERMISSION=1234;
    public static final int REQuEST_CODE_PLAY_SERVICES=1111;

    private RecyclerView cancionesRecyclerView = null;
    private List<Cancion> canciones = null;
    private MusicService musicService = null;
    private MusicController musicController = null;
    private Intent playIntent=null;
    private boolean reproductorPausado=false, pausado=false, musicBound = false;
    private CancionesAdapter cancionesAdapter = null;

    public ReproductorFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ReproductorFragment newInstance(String param1, String param2) {
        ReproductorFragment fragment = new ReproductorFragment();


        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnToolbarListener){
            onToolbarListener=(OnToolbarListener) context;
        }else{
            throw  new RuntimeException("La clase no implementa el manejo de Toolbar");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(onToolbarListener!= null){
            onToolbarListener.onChangeTitle("Reproductor Fragment");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onToolbarListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reproductor, container, false);
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int pos) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public boolean checkPlayServices(){
        int status= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());
        if(status!= ConnectionResult.SUCCESS){
            if(GoogleApiAvailability.getInstance().isUserResolvableError()){
                mostrarErrorPlayServices(status);
            }else{
                Toast.makeText(getContext(),"No es posible obtener Google Play", Toast.LENGTH_LONG).show();
            }
            return false;
        }else{
            return true;
        }
    }

    public void mostrarErrorPlayServices(int status){
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        apiAvailability.getErrorDialog(getActivity(),status,REQuEST_CODE_PLAY_SERVICES).show();
    }

    public boolean verificarPermisos(){
        boolean hasPermissionWrite=(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if(!hasPermissionWrite){
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                
            }
        }
        return hasPermissionWrite;
    }
}
