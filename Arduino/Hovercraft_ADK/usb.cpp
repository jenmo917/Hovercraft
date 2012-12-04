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
int connectionCounter;

bool blinkyFlag = false;

/**
* \brief Sets the blinkyflag to true
*
* Sets the blinkyflag to true.
* Used together with the blinkyHandler to use blinky
*
* @return No return
*
* \author Rickard Dahm
*
*/
void startBlinky()
{
	blinkyFlag = true;
}

/**
* \brief Sets the blinkyflag to false
*
* Turns off blinky
* Used together with the blinkyHandler to use blinky
*
* @return No return
*
* \author Rickard Dahm
*
*/
void stopBlinky()
{
	blinkyFlag = false;
	digitalWrite(LED, LOW);
}

/**
* \brief Setup for the USB
*
* Setup for the USB
*
* @return No return
*
* \author Rickard Dahm
*
*/
void USBsetup()
{
	delay(100);
	acc.powerOn();
}

/**
* \brief Reads the rcvmsgInfo bytes and performs the action described by the Command byte
*
* Reads the rcvmsgInfo bytes and performs the action described by the Command byte
*
* @return No return
*
* \author Rickard Dahm
*/
void decodeMsgType()
{
	static int prevPower = 0;
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
			connectionCounter = 0;
			motors = decodeEngines();
			printMotorSignal(&motors);
			motorControl(&motors);
			break;
		case MOTOR_CONTROL_TEST:
			Serial.println("COMMAND: MOTOR_CONTROL_TEST");
			connectionCounter = 0;
			motors = decodeEngines();
			prevPower = prevPower+10;
			if(prevPower > 255)
			{
				prevPower = 0;
			}
			motors.right.power = prevPower;
			motors.left.power = prevPower;
			printMotorSignal(&motors);
			motorControl(&motors);
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
		case ENGINES_REQ_COMMAND:
			q.enqueueEvent(Events::EV_ENGINES_REQ, rcvPBmsg[0]);
			break;
		default:
			Serial.println("COMMAND: Error, message is of unknown type. No action performed");
			break;
		}
	}
}

/**
* \brief Message to send a message using the USB
*
* Message to send a message using the USB
*
* @param command Value for the Command byte
*
* @param target Value for the target byte
*
* @return No return
*
* \author Rickard Dahm
*/
void sendMessage(int command, int target)
{
	if (acc.isConnected())
	{
		//Serial << "SendMessage" << endl;
		//Serial << sendMsgLength;
		//Serial << command << " " << target << " " << sendMsgLength << endl;
		byte fullMsg[255];
		int i;
		fullMsg[0] = command;
		fullMsg[1] = target;
		fullMsg[2] = sendMsgLength;

		for(i = 0; i < sendMsgLength; i++)
		{
			fullMsg[3 + i] = sendMsg[i];
		}
		acc.write(fullMsg, i+3);
	}
}
