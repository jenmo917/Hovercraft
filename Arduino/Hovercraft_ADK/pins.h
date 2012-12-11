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
#define rightDir 3
#define rightEnable 4

#define leftPWM 5
#define leftDir 6
#define leftEnable 8

#define liftFans 9

// Pins used for sensors
// Pin 20 used for I2C
// Pin 21 used for I2C

#define trigPin1 A0
#define echoPin1 A1
#define trigPin2 A2
#define echoPin2 A3
#define trigPin3 A4
#define echoPin3 A5
#define trigPin4 A6
#define echoPin4 A7

void pinSetup();
void LEDPinsSetup();
void rightMotorPinsSetup();
void leftMotorPinsSetup();
void sensorPinsSetup();
void testPins();

#endif /* PINS_H_ */
