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
#define leftDir A0
#define leftEnable A1

#define liftFans 12

// Pins used for sensors
// Pin 20 used for I2C
// Pin 21 used for I2C

#define trigPin1 A8
#define echoPin1 A9
#define trigPin2 A10
#define echoPin2 A11
#define trigPin3 A12
#define echoPin3 A13
#define trigPin4 A14
#define echoPin4 A15

void pinSetup();
void LEDPinsSetup();
void rightMotorPinsSetup();
void leftMotorPinsSetup();
void sensorPinsSetup();
void testPins();
void liftFanSetup();

#endif /* PINS_H_ */
