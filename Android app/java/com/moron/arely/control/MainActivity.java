package com.moron.arely.control;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

// Elaborado por Arely M.

/*
Nota esta app tiene unos cuantos errores o fallos pero solo es para practicar y aprender el uso del bluetooth en android
asi que no preste mucha atencion a eso y tambien no me quiero complicar mucho.
Pero lo intente hacer lo mejor que pude porque me gusta aprender y hacer las cosas bien aunque solo soy una estudiante
que aun hago esto por hobby.
 */

public class MainActivity extends AppCompatActivity
{

    private ListView DispositivosConectados;
    private ListView DispositivosDisponibles;
    private FloatingActionButton Buscar;
    private ProgressBar progressBar;
    private static Bluetooth bluetooth;
    private ArrayAdapter<String> dispositivos_disponibles;
    private ArrayAdapter<String> dispositivos_conectados;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DispositivosConectados = findViewById(R.id.LV_Vinculados);
        DispositivosDisponibles = findViewById(R.id.LV_Disponibles);
        Buscar = findViewById(R.id.btn_Buscar);
        progressBar = findViewById(R.id.progressBar);
        Intent intent = getIntent();
        if(!Bluetooth.IsCompatible())
            Toast.makeText(getApplicationContext(), "El dispositivo no es compatible", Toast.LENGTH_LONG).show(); // si el dispositivo no tiene bluetooth muestro un texto al usuario
        if(!Bluetooth.IsEnabled())
            Bluetooth.Habilitar_Bt(this); // con el parametro this indico que quiero que en esta activity se muestre el cuadro de dialogo
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // registro los eventos a los que quiero que responda mi Broadcast Receiver
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST));
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        Iniciar();
        if(Bluetooth.IsEnabled())
        {
            bluetooth = new Bluetooth();
            dispositivos_conectados.addAll(bluetooth.Dispositivos_Nombres(bluetooth.DispositivosVinculados()));
            dispositivos_conectados.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStop() // esto lo realizo cuando el usuario sale de mi aplicacion
    {
        super.onStop();
        unregisterReceiver(broadcastReceiver); // cancelo el Broadcast Receiver
        dispositivos_conectados.clear();
        dispositivos_disponibles.clear();
        bluetooth.CancelarBusqueda();
    }

    @Override
    public void  onActivityResult(int requestCode, int resultCode, Intent intent) // aqui obtengo el resultado del cuadro de dialogo que abri para solicitar al usuario la activacion del bluetooth
    {
        if(requestCode == 1)
        {
            if (resultCode == RESULT_CANCELED) // si el usuario no acepto activar el bluetooth le muestro un texto indicandole que lo tiene que activar
                Toast.makeText(getApplicationContext(), "Tiene que activar el Bluetooth para usar esta app", Toast.LENGTH_LONG).show();
            if (resultCode == RESULT_OK)
            {
                bluetooth = new Bluetooth();
            }
        }
    }

    private void Iniciar() // inicio los componentes que voy a utilizar (eventos, atributos etc..)
    {
        dispositivos_disponibles = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        dispositivos_conectados = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        DispositivosDisponibles.setAdapter(dispositivos_disponibles);
        DispositivosConectados.setAdapter(dispositivos_conectados);
        progressBar.setVisibility(View.GONE);
        Buscar.setImageResource(R.drawable.ic_search_black_24dp); // selecciono la imagen que tendra el Floating Action Button

        DispositivosConectados.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                HabilitarScrooll(v, event);
                return true;
            }
        });
        DispositivosConectados.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Conectar_OnItemClick(view, bluetooth.DispositivosVinculados().get(position));
                /*
                al parecer la llamada a este conjunto de metodos produce una ralentizacion
                bluetooth.Conectar(bluetooth.DispositivosVinculados().get(0)), esto se solucionaria
                lanzando este conjunto de metodos en segundo plano pero no se como hacerlo, intente
                hacerlo mediante la clase asynctask pero al parecer no funciona.
                funciono haciendolo con la case thread pero no estoy muy segura de porque no se pudo
                con la clase asynctask si es lo mismo, debo checar el uso de la clase asynctask
                 */

                            /*El error esta en pasar un objeto al activity, para hacer esto el objeto tiene que implementar la interfaz
                            serializable o parcelable pero no funciona implementando la interfaz serializable en la clase Bluetooth
                            asi que probe con la clase parcelable pero mi clase ocupa como campos o propiedades objetos de otras clases y
                            esas clases deben implementar tambien la interfaz parcelable o no estoy segura pero tal vez tambien funcione
                            si implementa la interfaz serializable.
                            Bueno como sea no puedo pasar el objeto de tipo Bluetooth al activity tal vez si paso solo el objeto bluetoothdevic
                            y me conecto a el en el otro activity ya que esa clase creo que implementa la interfaz parcelable o habia pensado en
                            pasar el objeto de la clase bluetoothsocket pero creo esa clase no implementa la interfaz parcelable asi que no se podria
                            habia leido que se podria hacr con un sevicio (service) pero no se como hacer esto.
                            Lo solucione ocupando la variable del socket bluetooth como static.

                            Nota
                            un servicio es un proceso que se ejecuta en segundo plano aunque el usuario salga de la aplicacion, a diferencia
                            de los tareas asincronas que se ejecutan solo cuando el usuario esta en la aplicacion*/
            }
        });

        Buscar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Buscar_onClick(v);
            }
        });

        DispositivosDisponibles.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                HabilitarScrooll(v, event);
                return true;
            }
        });

        DispositivosDisponibles.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id)
            {
                bluetooth.DispositivosDisponibles().get(position).createBond(); // vinculo el dispositivo que se selecciono

            }
        });
    }

    private void Buscar_onClick(View v) // inicio o cancelo la busqueda de nueos dispositivos
    {
        if(!bluetooth.getBluetoothAdapter().isDiscovering() && Bluetooth.IsEnabled()) // checo si no se estan buscando dispositivos para entonces iniciar la busqueda
        {
            dispositivos_disponibles.clear();
            bluetooth.DispositivosDisponibles().clear();
            dispositivos_disponibles.notifyDataSetChanged();
            progressBar.setVisibility(View.VISIBLE);
            Buscar.setImageResource(R.drawable.ic_clear_black_24dp);
            Snackbar.make(v, "Buscando ...", Snackbar.LENGTH_SHORT).show();
            bluetooth.BuscarDispositivos();
        }
        else
        {
            progressBar.setVisibility(View.GONE);
            Buscar.setImageResource(R.drawable.ic_search_black_24dp);
            bluetooth.CancelarBusqueda();
        }
    }

    private void HabilitarScrooll(View v, MotionEvent event) // habilito el scrollview del listview ya que se deshabilito al ponerlo dentro de un scrollview
    {
        int action = event.getAction();
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                // Disallow ScrollView to intercept touch events.
                v.getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_UP:
                // Allow ScrollView to intercept touch events.
                v.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        // Handle ListView touch events.
        v.onTouchEvent(event);
    }

    private void Conectar_OnItemClick(final View view, final BluetoothDevice dispositivo) // me conecto a un dispositivo bluetooth
    {
        bluetooth.CancelarBusqueda();
        progressBar.setVisibility(View.GONE);
        DispositivosConectados.setEnabled(false);
        DispositivosDisponibles.setEnabled(false);
        Buscar.setEnabled(false);
        Snackbar.make(view, "Conectando al dispositivo", Snackbar.LENGTH_SHORT).show();
        new Thread(new Runnable() // ejecuto esto en otro hilo porque el metodo conectar produce un pequeño bloqueo en la app
        {
            @Override
            public void run()
            {
                bluetooth.Conectar(dispositivo);
                if(bluetooth.getSocket().isConnected())
                {
                    Intent intent = new Intent(getApplicationContext(), control.class);
                    startActivity(intent);
                    finish(); // finalizo esta activity con el fin de que el usuario no pueda volver a ella presionando el boton atras
                }
                else
                {
                    Snackbar.make(view, "No se pudo conectar con el dispositivo", Snackbar.LENGTH_LONG).show();
                    //DispositivosConectados.setEnabled(true);
                    //DispositivosDisponibles.setEnabled(true);
                    view.post(new Runnable() {
                        @Override
                        public void run()
                        {
                            Buscar.setEnabled(true);
                            DispositivosDisponibles.setEnabled(true);
                            DispositivosConectados.setEnabled(true);
                        }
                    });
                    /*Buscar.post(new Runnable()
                    { // lo ejecuto en el hilo principal porque si  no lo hacia me cerraba la app pero no se porque si puedo
                      // acceder a los otros controles desde este hilo

                        @Override
                        public void run()
                        {
                            Buscar.setEnabled(true);
                        }
                    });    */
                }
            }
        }).start();
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() // inicio el Broadcast Receiver donde recibire los eventos del sistema que registre previamente
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction(); // obtengo el evento que recibio el Broadcast Receiver

            switch(action) // checo que evento fue el que recibio
            {
                case BluetoothAdapter.ACTION_STATE_CHANGED: // este evento se produce cuando se prende o apaga el bluetooth
                {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if(state == BluetoothAdapter.STATE_ON)
                    {
                        bluetooth = new Bluetooth();
                        dispositivos_conectados.addAll(bluetooth.Dispositivos_Nombres(bluetooth.DispositivosVinculados()));
                        dispositivos_conectados.notifyDataSetChanged();
                    }
                    else if(state == BluetoothAdapter.STATE_OFF)
                    {
                        Toast.makeText(context, "Debe mantener el bluetooth encendido", Toast.LENGTH_LONG).show();
                        dispositivos_conectados.clear();
                        dispositivos_disponibles.clear();
                        dispositivos_conectados.notifyDataSetChanged();
                        dispositivos_disponibles.notifyDataSetChanged();
                    }
                    break;
                }

                case BluetoothDevice.ACTION_FOUND: // este se produce cuando se encuentra un nuevo dispositivo
                {
                    BluetoothDevice dispositivo_encontrado = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // obtengo el dispositivo que se encontro
                    if(dispositivo_encontrado.getBondState() == BluetoothDevice.BOND_NONE)
                    {
                        bluetooth.DispositivosDisponibles_añadir(dispositivo_encontrado); // añado el dispositivo a la lista de dispositivos disponibles
                        dispositivos_disponibles.addAll(bluetooth.Dispositivos_Nombres(bluetooth.DispositivosDisponibles())); // muestro el dispositivo que encontre en el listview
                        dispositivos_disponibles.notifyDataSetChanged();
                    }
                    break;
                }

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: // este se produce cuando se termina la busqueda de nuevos dispositivos
                {
                    progressBar.setVisibility(View.GONE);
                    Buscar.setImageResource(R.drawable.ic_search_black_24dp);
                    break;
                }

                case BluetoothDevice.ACTION_BOND_STATE_CHANGED: // este se produce cuando se vincula o desvincula un dispositivo
                {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(device.getBondState() == BluetoothDevice.BOND_BONDED) // si se vinculo al dispositivo entonces me conecto a el
                        Conectar_OnItemClick(findViewById(R.id.linearLayout), device);
                    break;
                }
            }
        }
    };
}
