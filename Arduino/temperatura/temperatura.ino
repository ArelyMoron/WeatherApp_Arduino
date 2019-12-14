
#include <DHT.h>

// Definimos el pin digital donde se conecta el sensor
#define DHTPIN 2
// defino el tipo de sensor
#define DHTTYPE DHT11
 
// Inicializamos el sensor DHT11
DHT dht(DHTPIN, DHTTYPE);
void setup() 
{
  // put your setup code here, to run once:
  Serial.begin(9600);
  dht.begin();
}

void loop() 
{
  // put your main code here, to run repeatedly:
  // Esperamos 2 segundos entre medidas para evitar algun error debido al sensor 
  delay(2000);
  // Leemos la humedad relativa
  int h = dht.readHumidity();
  // Leemos la temperatura en grados centígrados (por defecto)
  int t = dht.readTemperature();
 
  // Comprobamos si ha habido algún error en la lectura
  if (isnan(t) || isnan(t)) 
  {
    // envio el dato de error
      Serial.print(" ");
      Serial.print("#");
      Serial.print("-1");
      Serial.print(";");
      return;
  }
  // envio los datos de la temperatura y humedad
  Serial.print(" ");
  Serial.print("#");
  Serial.print(t);
  Serial.print(",");
  Serial.print(h);
  Serial.print(";");
}
