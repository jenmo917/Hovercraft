#include <Arduino.h>
#include <Wire.h>
#include <UsbHost.h>
#include <AndroidAccessory.h>

#define LED_PIN 13
#define LED_1 12
#define LED_2 11

int x;
void receiveEvent(int howMany);

void setup() {
  Wire.begin(9);                // Start I2C Bus as a Slave (Device Number 9)
  Wire.onReceive(receiveEvent); // register event

  pinMode(LED_PIN, OUTPUT);
  pinMode(LED_1, OUTPUT);
  pinMode(LED_2, OUTPUT);

  digitalWrite(LED_PIN, LOW);
  digitalWrite(LED_1, LOW);
  digitalWrite(LED_2, LOW);

  x = 0;
}

void loop() {
  //If value received is 0 blink LED 1
  if (x == 0) {
    digitalWrite(LED_1, HIGH);
    delay(200);
    digitalWrite(LED_1, LOW);
    delay(200);
  }
  //If value received is 1 blink LED 2
  if (x == 1) {
    digitalWrite(LED_PIN, HIGH);
    delay(200);
    digitalWrite(LED_PIN, LOW);
    delay(200);
  }
}

void receiveEvent(int howMany) {
  x = Wire.read();    // receive byte as an integer
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
