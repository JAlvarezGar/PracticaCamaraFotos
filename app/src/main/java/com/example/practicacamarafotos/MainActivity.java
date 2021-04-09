package com.example.practicacamarafotos;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // CONSTANTES
    static final int CAPTURA_IMAGEN_THUBMNAIL = 100;
    static final int CAPTURA_IMAGEN_REAL = 101;
    static final int GRABAR_VIDEO = 200;

    ImageView imageView;
    VideoView videoView;
    String rutaActualFoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        videoView = (VideoView) findViewById(R.id.videoView);

        videoView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        solicitarPermisos();

    }

    // SOLICITA PERMISOS EN TIEMPO DE EJECUCION
    private void solicitarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAPTURA_IMAGEN_REAL);
        }
    }

    // TOMA UNA FOTO REAL
    public void shootCamera(View view) {
        videoView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.VISIBLE);
        capturarImagen();
    }

    // CAPTURA IMAGEN A TAMAÑO REAL
    private void capturarImagen() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Asegúrese de que haya una actividad de cámara para manejar la intención
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Crea el archivo donde debe ir la foto
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("Err ", ex.toString() + "  Error de I/O");
            }
            // Continua solo si no ha habido problemas en la creacion del fichero
            if (photoFile != null) {
                Uri fotoUri = FileProvider.getUriForFile(this, "com.example.practicacamarafotos.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                startActivityForResult(intent, CAPTURA_IMAGEN_REAL);
            }
        }
        // y de aquí pasa a onActivityForResult()
    }

    // CAPTURA IMAGEN A TAMAÑO THUMBNAILS
    public void capturarImagenThumbnails(View v) {
        videoView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.VISIBLE);
        Intent camara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (camara.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(camara, CAPTURA_IMAGEN_THUBMNAIL);
        }
        // y de aquí pasa a onActivityForResult()
    }


    /* toma una foto y la muestra. codigo de la API android */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // PARA MINIATURAS
        if (requestCode == CAPTURA_IMAGEN_THUBMNAIL && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
        // PARA IMAGENES REALES
        else if (requestCode == CAPTURA_IMAGEN_REAL && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
            Bitmap imageBitmap = BitmapFactory.decodeFile(rutaActualFoto);
            imageView.setImageBitmap(imageBitmap);
        }
        else if(requestCode == GRABAR_VIDEO && resultCode == RESULT_OK){
            videoView.setVideoURI(data.getData());
            videoView.start();
        }
    }


    // CREAR EL ARCHIVO TEMPORAL DE LA FOTO
    private File createImageFile() throws IOException {
        // CREA EL NOMBRE DEL FICHERO
        String fechaStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreFileImagen = "JESUSJPG" + fechaStamp + "_";
        File directorioAlmacenFoto = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // CREAR EL ARCHIVO TEMPORAL DE LA FOTO
        File imagen = File.createTempFile(
                nombreFileImagen,  /* prefix */
                ".jpg",         /* suffix */
                directorioAlmacenFoto      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        rutaActualFoto = imagen.getAbsolutePath();
        return imagen;
    }

    // GRABA VIDEO
    public void grabarVideo(View view) {
        Log.i("VIDEO", "grabarVideo: ESTOY DENTRO");
        imageView.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.VISIBLE);
        Intent camaraVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        camaraVideo.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0);
        camaraVideo.putExtra(MediaStore.EXTRA_DURATION_LIMIT,5);

        // mira a ver si hay una aplicacion que utilice la camara
        if(camaraVideo.resolveActivity(getPackageManager())!=null){
            startActivityForResult(camaraVideo,GRABAR_VIDEO);

            // y de aquí pasa a onActivityForResult()
        }

    }
}