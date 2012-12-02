/*
 * EventHandlers.cpp
 *
 *  Created on: 7 nov 2012
 *      Author: ricda841
 */
#include "EventHandlers.h"
#include "EventManagers.h"
#include <AndroidAccessory.h>
#include <UsbHost.h>
#include <AndroidAccessory.h>
#include "sensors.h"
#include "Streaming.h"
#include "Event.h"

// time event handler
void timeHandler1000(int event, int param)
{
	Serial.print("Time elapsed in seconds: ");
	Serial.println(millis() / 1000);
}

void timeHandler500(int event, int param)
{
	Serial.print("Time elapsed in half seconds: ");
	Serial.println(millis() / 500);
}

void timeHandler100(int event, int param)
{
	Serial.print("Time elapsed in 1/10 seconds: ");
	Serial.println(millis() / 100);
}

void blinkyHandler(int event, int param)
{
	if (blinkyFlag == true)
	{
		digitalWrite(LED, !digitalRead(LED));
	}
}

// analog event handler
void analogHandler(int event, int param)
{
	Serial.print("Analog value: ");
	Serial.println(param);
}

void I2CSensorDataHandler(int event, int target)
{
	Serial << "********************************************************" << endl;
	Serial << "Reading data from I2C Sensors" << endl;
	for(int i = 0; i < I2C_MAX_SENSORS; i++)
	{
		//Requires that all sensors want the commands our compass does.
		//Will need to be changed if other I2C devices requires other commands
		if(I2CSensorList[i].type != "Empty")
		{
			// step 1: instruct sensor to read echoes
			Wire.beginTransmission(I2CSensorList[i].address);  // transmit to device
			// the address specified in the datasheet is 66 (0x42)
			// but i2c adressing uses the high 7 bits so it's 33
			Wire.write('A');        // command sensor to measure angle
			Wire.endTransmission(); // stop transmitting

			// step 2: wait for readings to happen
			delay(10); // datasheet suggests at least 6000 microseconds

			// step 3: request reading from sensor
			Wire.requestFrom(I2CSensorList[0].address, 2); // request 2 bytes from slave device #33

			// step 4: receive reading from sensor
			if (2 <= Wire.available()) // if two bytes were received
			{
				reading = Wire.read(); // receive high byte (overwrites previous reading)
				reading = reading << 8; // shift high byte to be high 8 bits
				reading += Wire.read(); // receive low byte as lower 8 bits
				reading /= 10;
				Serial.print("Sensor Data: ");
				Serial.println(reading); // print the reading
			}
			else
			{
				Serial.print("I2C reading failed");
			}
		}
	}
	q.enqueueEvent(Events::EV_I2C_SENSOR_FINISHED, target);
}

void USSensorHandler(int event, int target)
{
	Serial << "********************************************************" << endl;
	Serial << "Reading data from Ultrasonic Sensors" << endl;
	int duration, distance;
	for(int i = 0; i < MAX_US_SENSORS; i++)
	{
		if(USSensorList[i].type != "Empty")
		{
			digitalWrite(USSensorList[i].triggerpin, HIGH);
			delayMicroseconds(1000);
			digitalWrite(USSensorList[i].triggerpin, LOW);
			duration = pulseIn(USSensorList[i].echopin, HIGH);
			distance = (duration/2) / 29.1;
			Serial << "Sensor " << i << ": ";
			if (distance >= 200 || distance <= 0)
			{
				Serial.println("Out of range");
			}
			else
			{
				Serial.print(distance);
				Serial.println(" cm");
			}
		}
	}
	q.enqueueEvent(Events::EV_US_SENSOR_FINISHED, target);
}

void USBReadHandler(int event, int param)
{
	int i = 0;
	rcvPBmsgLength = 0;
	for(i = 0; i < 3; i++)
	{
		rcvmsgInfo[i] = rcvmsg[i];
	}
	for(i = 3; i < param; i++)
	{
		rcvPBmsg[i-3] = rcvmsg[i];
		rcvPBmsgLength++;
	}
	decodeMsgType();
}

void USBSendUSSensorDataHandler(int event, int target)
{
	Serial << "USBSendUSSensorDataHandler" << endl;
	if (acc.isConnected())
	{
		for(int i = 0; i < MAX_US_SENSORS; i++)
		{
			if(USSensorList[i].type != "Empty")
			{
				if(encodeUSSensorMsg(USSensorList[i]))
				{
					sendMessage(US_SENSOR_COMMAND, target);
				}
				else
				{
					Serial << "Failed to encode sensor " << i << " in function:" << endl << "USBSendSensorDataHandler";
				}
			}
		}
	}
}

void USBSendI2CSensorDataHandler(int event, int target)
{
	Serial << "USBSendI2CSensorDataHandler" << endl;
	if (acc.isConnected())
	{
		for(int i = 0; i < I2C_MAX_SENSORS; i++)
		{
			if(I2CSensorList[i].type != "Empty")
			{
				if(encodeI2CSensorMsg(I2CSensorList[i]))
				{
					sendMessage(I2C_SENSOR_COMMAND, target);
				}
				else
				{
					Serial << "Failed to encode sensor " << i << " in function:" << endl << "USBSendSensorDataHandler";
				}
			}
		}
	}
}
