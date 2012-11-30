/*
 * pins.h
 *
 *  Created on: 9 nov 2012
 *      Author: ricda841
 */

#ifndef PINS_H_
#define PINS_H_

#include <Arduino.h>

// LED pin
#define LED 13

// Pins used for motor control
#define rightPWM 2
#define rightDir 4
#define rightEnable 24

#define leftPWM 3
#define leftDir 5
#define leftEnable 25

// Pins used for sensors
// Pin 20 used for I2C
// Pin 21 used for I2C
#define trigPin1 11
#define echoPin1 12
#define trigPin2 22
#define echoPin2 23
#define trigPin3 24
#define echoPin3 25
#define trigPin4 26
#define echoPin4 27

void pinSetup();
void LEDPinsSetup();
void rightMotorPinsSetup();
void leftMotorPinsSetup();
void sensorPinsSetup();
void testPins();

#endif /* PINS_H_ */
