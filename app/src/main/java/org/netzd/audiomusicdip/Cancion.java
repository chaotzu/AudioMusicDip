package org.netzd.audiomusicdip;

/**
 * Created by Alumno12 on 16/02/18.
 */

public class Cancion {
    private long id=0;
    private String titulo=null;
    private String artista=null;

    public Cancion() {
        // TODO Auto-generated constructor stub
    }

    public Cancion(long id, String titulo, String artista){
        this.id=id;
        this.titulo=titulo;
        this.artista=artista;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }
}
