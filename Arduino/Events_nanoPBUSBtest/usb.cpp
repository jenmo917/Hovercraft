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
#include "print.h"
#include "command.pb.h"

AndroidAccessory acc("Manufacturer", "Model", "Description","Version", "URI", "Serial");

byte rcvmsg[255];
byte rcvmsgInfo[3];
byte rcvPBmsg[252];
byte sendMsg[252];

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
	Engines motors;
	if (rcvmsgInfo[1] == TARGET_ADK)
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
		case MOTOR_CONTROL:
			Serial.println("COMMAND: MOTOR_CONTROL");
			motors = decodeEngines();
			printRightMotorSignal(motors);
			printLeftMotorSignal(motors);
			rightMotorControl(motors.right);
			leftMotorControl(motors.left);
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


void sendMessage(int command, int target)
{
	byte fullMsg[255];
	fullMsg[0] = command;
	fullMsg[1] = target;
	fullMsg[2] = sizeof(sendMsg);

	for(int i = 0; i < sizeof(sendMsg); i++)
	{
		fullMsg[3 + i] = sendMsg[i];
	}

	int len = acc.write(fullMsg, sizeof(fullMsg));
	delay(250);
}
