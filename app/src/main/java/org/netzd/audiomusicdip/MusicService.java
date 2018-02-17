package org.netzd.audiomusicdip;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Created by Alumno12 on 16/02/18.
 */


//Extendemos de Service por que la clase actuara como servicio e implementamos interfaces necesarias para manejar audio y video

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{


    private MediaPlayer reproductor = null;
    private List<Cancion> canciones = null;

    private int posicionCancion = 0;
    private String tituloCancion = "";

    private static final int NOTIFY_ID = 1;
    private boolean shuffle = false;
    private Random random = null;

    private final IBinder musicBind = new MusicBinder();


    public void inicializaReproductor(){
        reproductor.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        reproductor.setAudioStreamType(AudioManager.STREAM_MUSIC);
        reproductor.setOnPreparedListener(this);
        reproductor.setOnErrorListener(this);
        reproductor.setOnCompletionListener(this);
    }

    public void setCanciones(List<Cancion> canciones){
        this.canciones = canciones;
    }

    public void reproducirCancion(){
        reproductor.reset();
        Cancion cancion = canciones.get(posicionCancion);
        tituloCancion = cancion.getTitulo();
        long cancionActual = cancion.getId();

        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cancionActual); //Concatena la uri de los archivos de audio con el id
        try{
            reproductor.setDataSource(getApplicationContext(), trackUri);
        }catch (IOException e) {
            e.printStackTrace();
        }

        reproductor.prepareAsync();
    }

    public void setShuffle(){
        //Shuffle es aleatorio
        if(shuffle)
            shuffle = false;
        else
            shuffle = true;
    }

    public int getPosicion(){
        return reproductor.getCurrentPosition();
    }

    public int getDuracion(){
        return reproductor.getDuration();
    }

    public boolean estaReproduciendo(){
        return reproductor.isPlaying();
    }

    public void pausa(){
        reproductor.pause();
    }

    public void seek(int posicion){
        reproductor.seekTo(posicion);
    }

    public void reproducir(){
        reproductor.start();
    }

    public void reproducirAnterior(){
        posicionCancion --;
        if(posicionCancion<0){
            posicionCancion=canciones.size()-1;
        }
        reproducirCancion();
    }

    public void reproducirSiguiente(){
        if(shuffle){
            int cancionnueva = posicionCancion;
            while(cancionnueva==posicionCancion){
                cancionnueva = random.nextInt(canciones.size());
            }
            posicionCancion = cancionnueva;
        }else{
            posicionCancion ++;
            if(posicionCancion>=canciones.size())
                posicionCancion = 0;
            reproducirCancion();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        posicionCancion = 0;
        random = new Random();
        reproductor = new MediaPlayer();
    }


    //Permite implementar la clase como un procedimiento remoto
    public class MusicBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(reproductor.getCurrentPosition()>0){
            mp.reset();
            reproducirSiguiente();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset(); //mp es el objeto media player
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        reproductor.stop();
        reproductor.release();
        return super.onUnbind(intent);
    }
}
