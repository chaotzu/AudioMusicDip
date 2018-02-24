package org.netzd.audiomusicdip;


import android.*;
import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.List;


public class ReproductorFragment extends Fragment implements
        MediaController.MediaPlayerControl {

    public static final int WRITE_EXTERNAL_PERMISSION=12334;
    public static final int REQUEST_CODE_PLAY_SERVICES=12354;

    private RecyclerView cancionesRecyclerView=null;
    private List<Cancion> canciones=null;
    private MusicService musicService=null;
    private MusicController musicController=null;
    private Intent playIntent=null;
    private boolean reproductorPausado=false, pausado=false,
            musicBound=false;
    private CancionesAdapter cancionAdapter=null;

    private OnToolbarListener onToolbarListener=null;

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setCanciones(canciones);
            musicBound= true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    public ReproductorFragment() {
        // Required empty public constructor
    }

    public static ReproductorFragment newInstance(String param1, String param2) {
        ReproductorFragment fragment = new ReproductorFragment();
        return fragment;
    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnToolbarListener){
            onToolbarListener=(OnToolbarListener)  context;
        }else{
            throw new RuntimeException("La clase no implementa " +
                    "manejo de Toolbar");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(onToolbarListener!=null)
            onToolbarListener.onChangeTitle("Reproductor Fragment");
        if(playIntent==null){
            playIntent = new Intent(getActivity(),MusicService.class);
            getActivity().bindService(playIntent,musicConnection,Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(checkPlayServices()){
            cancionesRecyclerView = (RecyclerView) view.findViewById(R.id.reciclador);
            if(canciones == null)
                canciones = obtenerCanciones();
            cancionAdapter = new CancionesAdapter(canciones);
            LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            cancionesRecyclerView.setLayoutManager((manager));
            cancionesRecyclerView.setAdapter(cancionAdapter);
        }else{
            Toast.makeText(getContext(), "Se requiere Play Services", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case WRITE_EXTERNAL_PERMISSION:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //canciones = obtenerCanciones();
                    setListaCanciones();
                }else{
                    Toast.makeText(getContext(), "Autoriza permisos", Toast.LENGTH_LONG).show();
                }
                return;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,grantResults);
        }
    }

    //Incompleto
    private  void setListaCanciones(){
        if(canciones == null)
            canciones = obtenerCanciones();
        cancionAdapter = new CancionesAdapter(canciones);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        cancionesRecyclerView.setLayoutManager(manager);
        cancionesRecyclerView.setAdapter(cancionAdapter);
    }

    private void setMusicController(){
        musicController = new MusicController(getActivity());
        musicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        musicController.setMediaPlayer(this);
        musicController.setAnchorView(getActivity().findViewById(R.id.reciclador));
        musicController.setEnabled(true);
    }

    private void playNext(){
        musicService.reproducirSiguiente();
        if(reproductorPausado){
            setMusicController();
            reproductorPausado = false;
        }
        musicController.show();
    }
    private void playPrev(){
        musicService.reproducirAnterior();
        if(reproductorPausado){
            setMusicController();
            reproductorPausado = false;
        }
        musicController.show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onToolbarListener=null;
    }

    private List<Cancion> obtenerCanciones(){
        List<Cancion> canciones = new ArrayList<>();
        if(verificarPermisos()){
            try{
                ContentResolver musicContentResolver = getActivity().getContentResolver();
                Uri musicaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                Cursor musicaCursor = musicContentResolver.query(musicaUri,null, null, null, null);
                if(musicaCursor.moveToFirst()){
                    int tituloColumna = musicaCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int idColumna = musicaCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    int artistaColumna = musicaCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                    do{
                        canciones.add(new Cancion(musicaCursor.getLong(idColumna), musicaCursor.getString(tituloColumna), musicaCursor.getString(artistaColumna)));
                    }while (musicaCursor.moveToNext());
                }
            }catch (Exception e){
                Log.e("[Error]", e.getMessage());
            }
        }
        return canciones;
    }

    @Override
    public void start() {
        musicService.reproducir();
    }

    @Override
    public void pause() {
        reproductorPausado = true;
        musicService.pausa();
    }

    @Override
    public int getDuration() {
        if(musicService!= null && musicBound && musicService.estaReproduciendo()){
            return  musicService.getDuracion();
        }else{
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if(musicService!= null && musicBound && musicService.estaReproduciendo()){
            return  musicService.getPosicion();
        }else{
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicService!=null && musicBound)
            return musicService.estaReproduciendo();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public boolean checkPlayServices(){
        int status= GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(getContext());
        if(status!= ConnectionResult.SUCCESS){
            if(GoogleApiAvailability.getInstance().isUserResolvableError(status)){
                mostrarErrorPlayServices(status);
            }else{
                Toast.makeText(getContext(),
                        "No es posible obtener play services",
                        Toast.LENGTH_LONG).show();
            }
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(pausado){
            setMusicController();
            pausado = false;
        }
    }

    @Override
    public void onStop() {
        musicController.hide();
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        pausado = true;
    }

    @Override
    public void onDestroy() {
        getActivity().stopService(playIntent);
        musicService= null;
        super.onDestroy();
    }

    public void mostrarErrorPlayServices(int status){
        GoogleApiAvailability apiAvailability = GoogleApiAvailability
                .getInstance();
        apiAvailability.getErrorDialog(getActivity(),status,
                REQUEST_CODE_PLAY_SERVICES).show();
    }

    public boolean verificarPermisos(){
        boolean hasPermissionWrite= (ContextCompat
                .checkSelfPermission(getActivity(),
                        Manifest.permission
                                .WRITE_EXTERNAL_STORAGE)== PackageManager
                .PERMISSION_GRANTED);
        if(!hasPermissionWrite){
            if(ActivityCompat.
                    shouldShowRequestPermissionRationale(getActivity(),Manifest
                            .permission.WRITE_EXTERNAL_STORAGE)){
                DialogWarning dialogWarning = DialogWarning.newInstance("Aviso", "Se requiere la autorizacion para la lecutra de la SD", true);
                dialogWarning.setOnDialogWarningListener(new OnDialogWarningListener() {
                    @Override
                    public void onAccept(Dialog dialog) {
                        ActivityCompat.requestPermissions(getActivity(),new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },WRITE_EXTERNAL_PERMISSION);
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancel(Dialog dialog) {
                        dialog.dismiss();
                    }
                });
                dialogWarning.show(getFragmentManager(), "PERMISSION_WARNING");

            }
        }
        return hasPermissionWrite;
    }
}