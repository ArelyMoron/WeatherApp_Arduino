package com.moron.arely.control;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

// Elaborado por Arely M.

public class control extends AppCompatActivity
{
    private Button Volver;
    private TextView Temperatura;
    private TextView Humedad;
    private static final int READ_MSG = 0; // un identificador que uso para los datos que enviare desde el hilo los cuales son los que recibo de arduino
    Intent intent = getIntent();
    private static Bluetooth bluetooth;
    private static BluetoothDevice Dispositivo;
    private ListenerData listenerData;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        Volver = findViewById(R.id.btn_volver);
        Temperatura = findViewById(R.id.tv_temperatura);
        Humedad = findViewById(R.id.tv_humedad);
        Iniciar();
        bluetooth = new Bluetooth();
        Dispositivo = bluetooth.getSocket().getRemoteDevice(); // obtengo el dispositivo al que se conecto

        handler = new Handler() // Creo un handler donde recibire los datos enviados desde arduino para poder mostrarlos en la app
        {
            public void handleMessage(Message msg)
            {
                if(msg.what == READ_MSG) // checo que si son los datos del aduino mediante el identificador
                {
                    StringBuilder strReceived;
                    String dataReceived = (String) msg.obj;
                    strReceived = new StringBuilder(dataReceived);
                    int end = strReceived.indexOf(";");
                    int start = strReceived.indexOf("#");
                    int endOfTemp = strReceived.indexOf(",");
                    if(end > 0)
                    {
                        if(strReceived.substring(start + 1, end) != "-1") // si no se envio mensaje de error muestro los datos
                        {
                            String temp = strReceived.substring(start + 1,endOfTemp);
                            String hum = strReceived.substring(endOfTemp + 1, end);
                            strReceived.delete(0, strReceived.length());
                            Temperatura.setText(temp + " °C");
                            Humedad.setText(hum + " %");
                        }
                    }
                }
            }
        };
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Conectar(Dispositivo); // me vuelvo a conectar al dispositivo cuando el usuario vuelve a entrar a la app
        listenerData = new ListenerData(bluetooth.getSocket());
        new Thread(listenerData).start(); // inicio el hilo que estara a la escucha de los datos enviados
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        bluetooth.Desconectar(); // me desconecto del dispositivo cuando el usuario sale de la app
        unregisterReceiver(broadcastReceiver); // cancelo el Broadcast Receiver
        handler.removeCallbacks(listenerData);
    }

    private void Iniciar() // inicio los componentes que voy a utilizar (eventos, atributos etc..)
    {
        Volver.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Volver_onClick();
            }
        });
        Temperatura.setText("---");
        Humedad.setText("---");
    }

    private void Volver_onClick() // vuelvo al activity anterior
    {
        bluetooth.Desconectar();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish(); // finalizo esta activity con el fin de que el usuario no pueda volver a ella presionando el boton atras
    }

    private void Conectar(final BluetoothDevice dispositivo) // en este metodo me reconecto al dispositivo
    {
        if(bluetooth.getSocket().isConnected()) // si ya esta conectado solo muestro a que dispositivo se esta conectado
        {

            Toast.makeText(getBaseContext(), "Conectado a " + dispositivo.getName(), Toast.LENGTH_SHORT).show();
        }

        else
        {
            Toast.makeText(getBaseContext(), "Reconectando con " + dispositivo.getName(), Toast.LENGTH_SHORT).show();
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    bluetooth.Conectar(dispositivo);
                    if(bluetooth.getSocket().isConnected())
                    {
                        Toast.makeText(getBaseContext(), "Conectado a " + dispositivo.getName(), Toast.LENGTH_SHORT).show();
                    }

                    else
                    {
                        Toast.makeText(getBaseContext(), "No conectado", Toast.LENGTH_LONG).show();
                    }
                }
            }).start();

        }
    }

    /*
    creo que no seria buena practica usar 2  Broadcast Receiver pero lo hice asi porque en los eventos uso algunos atributos
    de la clase o el activity en el que estoy. No estoy segura pero seria mejor usar el  Broadcast Receiver en una clase
    aparte y usar solo 1  Broadcast Receiver para todos los activity.

    el  Broadcast Receiver tambien se puede registrar en el android manifest en vez de hacerlo en tiempo real por asi decirlo que
    fue como lo hize aqui usando los eventos onStart y onStop.
     */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            switch (action)
            {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if(state == BluetoothAdapter.STATE_ON)
                    {
                        Conectar(Dispositivo);
                    }
                    if(state == BluetoothAdapter.STATE_OFF)
                    {
                        bluetooth.Desconectar();
                        Toast.makeText(context, "Debe mantener el bluetooth encendido", Toast.LENGTH_LONG).show();
                        Temperatura.setText("---");
                        Humedad.setText("---");
                    }
                    break;
                }

                case BluetoothDevice.ACTION_ACL_DISCONNECTED: // este evento se produce cuando se desconecta del dispositivo
                {
                    Toast.makeText(context, "Se desconecto el dispositivo", Toast.LENGTH_LONG).show();
                    Temperatura.setText("---");
                    Humedad.setText("---");
                    break;
                }
            }
        }
    };

    private class ListenerData implements Runnable // esto se va a ejecutar en un hilo que me permitiria estar a la escucha de los datos enviados
    { // sin detener mi aplicacion
        private InputStream getSendData;

        public ListenerData(BluetoothSocket socket)
        {
            try
            {
                getSendData = socket.getInputStream();
            }
            catch (Exception e)
            {

            }
        }

        public void run()
        {
            int bytesRead;

            while (!Thread.currentThread().isInterrupted())
            {
                try
                {
                    /*
                    No se porque la aplicacion siempre me esta igorando o borrando el primer caracter enviado asi que para asegurarme que
                    me los envie todos al principio envio un espacio en blanco que sera ignorado y de esa forma se reciben todos los datos
                    correctamente.
                     */
                    bytesRead = getSendData.available(); // si hay datos por leer entonces comienzo a leerlos
                    byte[] buffer = new byte[bytesRead];
                    if(bytesRead > 0)
                    {
                        bytesRead = getSendData.read(buffer);
                        String readMessage = new String(buffer, 0, bytesRead);
                        /*
                        aqui debajo comienzo a darle un formato por asi decirlo a los datos leidos para ser interpretados
                        mas adelante en el handler;
                        Los datos comienzan con un "#" y finalizan con un ";" separando cada dato con una ","
                        empezando con la temperatura seguido de la humedad de esta forma
                        #temperatura,humedad; a exepcion de si hay un mensaje de error que quedaria de esta forma
                        #-1; donde -1 es el mensaje de error que envia el arduino cuando no pudo obtener los datos del sensor
                        */
                        readMessage = readMessage.substring(0, readMessage.indexOf(";", 1) + 1);
                        handler.obtainMessage(READ_MSG, bytesRead, -1, readMessage).sendToTarget(); // envio el mensaje al handler/
                    }
                }
                catch (IOException e)
                {
                    break;
                }
            }
        }
    }
}
