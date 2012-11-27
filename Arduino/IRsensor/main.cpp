/*
 * main.cpp
 *
 *  Created on: 19 nov 2012
 *      Author: ricda841
 */
#include <Arduino.h>

/*
 HC-SR04 Ping distance sensor]
 VCC to arduino 5v GND to arduino GND
 Echo to Arduino pin 13 Trig to Arduino pin 12
 More info at: http://goo.gl/kJ8Gl
 */

#define trigPin 11
#define echoPin 12
#define LED 13

void setup() {
  Serial.begin (115200);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(LED, OUTPUT);
  digitalWrite(LED, LOW);
}

void loop() {
  int duration, distance;
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(1000);
  digitalWrite(trigPin, LOW);
  duration = pulseIn(echoPin, HIGH);
  distance = (duration/2) / 29.1;
  if (distance >= 200 || distance <= 0){
    Serial.println("Out of range");
    digitalWrite(LED, LOW);
  }
  else {
    Serial.print(distance);
    Serial.println(" cm");
    digitalWrite(LED, HIGH);
  }
  delay(500);
}

/*
void readIRSensor();
// The setup() method runs once, when the sketch starts
void setup()
{
	// initialize the digital pin as an output:
	Serial.begin(115200);
	attachInterrupt(echoPin, readIRSensor, CHANGE);
	interrupts();
}

void loop()
{
	delay(1000);
	digitalWrite(trigPin, HIGH);
	delayMicroseconds(1000);
	digitalWrite(trigPin, LOW);
	Serial.println("Loop");
}

void readIRSensor()
{
	Serial.println("IR");
	int val;
	static unsigned long prevMicros = 0;
	unsigned long currMicros;
	currMicros = micros();
	if(digitalRead(A1) == 1)
	{
		prevMicros = currMicros;
	}
	else
	{
		val = (currMicros-prevMicros)/58;
		Serial.print(val);
	}
}
*/
int main(void) {

	init();
	setup();

	while(true)
	{
		loop();
	}
}
