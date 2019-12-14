package com.moron.arely.control;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
// Elaborado por Arely M.

public class Bluetooth
{
    private BluetoothAdapter bluetoothAdapter;
    private static final String uuid = "00001101-0000-1000-8000-00805F9B34FB"; // codigo uuid para poder conectar con el dispositivo
    private static BluetoothSocket socket;
    private ArrayList<BluetoothDevice> DispositivosConectados;
    private ArrayList<BluetoothDevice> DispositivosDisponibles;

    public Bluetooth()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        DispositivosConectados = new ArrayList<BluetoothDevice>();
        DispositivosDisponibles = new ArrayList<BluetoothDevice>();
        DispositivosConectados.addAll(bluetoothAdapter.getBondedDevices()); // obtengo los dispositivos vinculados
    }

    public static boolean IsCompatible() // checo si el dispositivo es compatible con bluetooth
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
            return false;
        else
            return true;
    }

    public static boolean IsEnabled() // checo si el bluetooth esta habilitado
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }

    public ArrayList<BluetoothDevice> DispositivosVinculados() // lista con los dispositivos vinculados
    {
        return DispositivosConectados;
    }

    public ArrayList<BluetoothDevice> DispositivosDisponibles() // lista con los dispositivos que se encuentran diponibles en ese momento
    {
        return DispositivosDisponibles;
    }

    public BluetoothSocket getSocket()
    {
        return socket;
    }

    public BluetoothAdapter getBluetoothAdapter()
    {
        return bluetoothAdapter;
    }

    public String[] Dispositivos_Nombres(ArrayList<BluetoothDevice> dispositivos) // devuelve el nombre de los dispositivos para mostrarlo en el listview
    {
        String[] nombres = new String[dispositivos.size()];
        for(int i = 0; i < nombres.length; i++)
        {
            nombres[i] = dispositivos.get(i).getName();
        }
        return nombres;
    }

    public void DispositivosDisponibles_añadir(BluetoothDevice dispositivo) // añado un dispositivo a la lista de dispositivos disponibles
    {
        DispositivosDisponibles.add(dispositivo);
    }
    public static void Habilitar_Bt(Activity activity) // abre un cuadro de dialogo para solicitar la activacion del bluetooth
    {
        Intent habilitarBt_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(habilitarBt_intent, 1);
    }

    public void BuscarDispositivos()
    {
        if(!bluetoothAdapter.isDiscovering())
            bluetoothAdapter.startDiscovery();
    }

    public void CancelarBusqueda()
    {
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }

    public boolean Conectar(final BluetoothDevice dispositivo) // me conecto al dispositivo
    {
        try
        {
            socket = dispositivo.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid));
            socket.connect();
        }

        catch(IOException e)
        {
        }

        return socket.isConnected(); // checo si se pudo conectar o no
    }

    public void Desconectar() // me desconecto del dispositivo
    {
        try
        {
            socket.close();
        }
        catch (IOException e)
        {

        }
    }

    public void Enviar(int dato) // envio el dato por bluetooth al dispositivo conectado
    {
        if(socket == null)
            return;

        try
        {
            socket.getOutputStream().write(dato);
        }
        catch (IOException e)
        {

        }
    }
}
