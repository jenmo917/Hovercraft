#include <Arduino.h>
#include <Wire.h>
#include <UsbHost.h>
#include <AndroidAccessory.h>

#define LED_PIN 13
#define BUTTON 3
byte x = 0;
int val = 0;

void setup()
{
  Wire.begin(); // Start I2C Bus as Master
  pinMode(LED_PIN, OUTPUT);

  pinMode(BUTTON, INPUT);
  digitalWrite(BUTTON, HIGH);       // turn on pullup resistors

  digitalWrite(LED_PIN, LOW);

}
void loop()
{

  val = digitalRead(BUTTON);

  Wire.beginTransmission(9); // transmit to device #9
  if(val)
  {
    Wire.write(1);
  }
  else
  {
    Wire.write(0);
  }
  Wire.endTransmission();    // stop transmitting
}

int main(void) {

  /* Must call init for arduino to work properly */
	init();
	setup();

	for (;;)
	{
	  loop();
	}
}
