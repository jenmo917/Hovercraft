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
#include "Streaming.h"
#include "Event.h"

AndroidAccessory acc("Manufacturer", "Model", "Description","Version", "URI", "Serial");

byte rcvmsg[255];
byte rcvmsgInfo[3];
byte rcvPBmsg[252];
byte sendMsg[252];
int sendMsgLength;
int rcvPBmsgLength;

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
	Serial << "********************************************************" << endl;
	Serial << "Command: " << rcvmsgInfo[0] << endl;
	Serial << "Target: " << rcvmsgInfo[1] << endl;
	Serial << "Length: " << rcvmsgInfo[2] << endl;
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
			rightMotorControl(&motors.right);
			leftMotorControl(&motors.left);
			break;
		case PRINT_MESSAGE:
			Serial << "Message: " << endl;
			for(int i = 0; i < rcvPBmsgLength; i++)
			{
				Serial.print((char) rcvPBmsg[i]);
				Serial << " "<< (uint8_t) rcvPBmsg[i] << endl;
			}
			break;
		case I2C_SENSOR_REQ:
			q.enqueueEvent(Events::EV_I2C_SENSOR_REQ, rcvPBmsg[0]);
			break;
		case US_SENSOR_REQ:
			q.enqueueEvent(Events::EV_US_SENSOR_REQ, rcvPBmsg[0]);
			break;
		default:
			Serial.println("COMMAND: Error, message is of unknown type. No action performed");
			break;
		}
	}
}

void sendMessage(int command, int target)
{
	if (acc.isConnected())
	{
		byte fullMsg[255];
		int i;
		fullMsg[0] = command;
		fullMsg[1] = target;
		fullMsg[2] = sendMsgLength;
		//Serial << sendMsgLength;

		for(i = 0; i < sendMsgLength; i++)
		{
			fullMsg[3 + i] = sendMsg[i];
		}
		acc.write(fullMsg, i+3);
	}
}
