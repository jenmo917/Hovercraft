/*
 * usb.cpp
 *
 *  Created on: 8 nov 2012
 *      Author: ricda841
 */

#include <Arduino.h>
#include "usb.h"
#include "Drive.h"
#include "nanoPB.h"

AndroidAccessory acc("Manufacturer", "Model", "Description","Version", "URI", "Serial");

byte rcvmsg[255];
byte rcvmsgInfo[3];
byte rcvPBmsg[252];
byte sntmsg[3 + ARRAY_SIZE];

bool blinkyFlag = false;

void startBlinky()
{
	blinkyFlag = true;
}

void stopBlinky()
{
	blinkyFlag = false;
	digitalWrite(LED, LOW);
}

void USBsetup()
{
	delay(100);
	acc.powerOn();
}

void decodeMsgType()
{
	int power;
	if (rcvmsgInfo[1] == TARGET_DEFAULT)
	{
		switch(rcvmsgInfo[0])
		{
		case BLINKY_ON:
			Serial.println("COMMAND: BLINKY_ON");
			startBlinky();
			break;
		case BLINKY_OFF:
			Serial.println("COMMAND: BLINKY_OFF");
			stopBlinky();
			break;
		case RIGHT_DIR_FORWARD:
			Serial.println("COMMAND: RIGHT_DIR_FORWARD");
			setRightDir(1);
			break;
		case RIGHT_DIR_BACK:
			Serial.println("COMMAND: RIGHT_DIR_BACK");
			setRightDir(0);
			break;
		case RIGHT_POWER:
			Serial.println("COMMAND: RIGHT_POWER");
			Serial.println("Power = ");
			power = decodePower((int) rcvmsgInfo[2]);
			//Serial.println((int) rcvmsg[3]);
			//setRightPower(&rcvmsg[3]);
			setRightPower(&power);
			break;
		case LEFT_DIR_FORWARD:
			Serial.println("COMMAND: LEFT_DIR_FORWARD");
			setLeftDir(1);
			break;
		case LEFT_DIR_BACK:
			Serial.println("COMMAND: LEFT_DIR_BACK");
			setLeftDir(0);
			break;
		case LEFT_POWER:
			Serial.println("COMMAND: LEFT_POWER");
			Serial.println("Power = ");
			Serial.println((int) rcvmsgInfo[2]);
			setLeftPower(&rcvmsgInfo[2]);
			break;
		case MOTOR_STOP:
			Serial.println("COMMAND: ENGINE_STOP");
			setLeftPower(0);
			setRightPower(0);
			break;
		case ENABLE_MOTORS:
			Serial.println("COMMAND: ENABLE_ENGINES");
			enableMotors();
			break;
		default:
			Serial.println("COMMAND: Error, message is of unknown type. No action performed");
			break;
		}
	}
}
