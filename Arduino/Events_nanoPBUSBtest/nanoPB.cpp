/* Program som visar hur nanopb skall användas

I ditt programm så måste du inkludera ha följande filer i din src map:
	command.pb.c
	command.pb.h
Följande filer som ligger i mappen nanopb måste finnas med under includepath
	 <pb.h>
	<pb_encode.h>
	<pb_decode.h>
	
Till sist måste AVRnanopb biblioteket länkas in detta finns i AVRnanopb/release
*/

#include <Arduino.h>
#include "command.pb.h"
#include <pb.h>
#include <pb_encode.h>
#include <pb_decode.h>
#include "usb.h"
#include "nanoPB.h"
#include <UsbHost.h>
#include <AndroidAccessory.h>
#include "print.h"
#include "Drive.h"
#include "command.pb.h"
#include "Streaming.h"
#include "sensors.h"

int incomingByte;					// for incoming serial data
//byte outBuffer[255];				// buffer for incoming data
//int count = 0;					//length of buffer

/**
* \brief Decodes a Engines Protocol message
*
* Decodes a Engines Protocol message
*
* @return The decoded Engines object
*
* \author Rickard Dahm
*
*/
Engines decodeEngines()
{
	Engines engines;
	int length = (int) rcvmsgInfo[2];
	pb_istream_t stream = pb_istream_from_buffer((uint8_t*)rcvPBmsg, length);
	if(pb_decode(&stream, Engines_fields, &engines)) //incoming buffer decoded to protocol
	{
		return engines;
	}
	else
	{
		Serial << "Failed to decode an Engines object";
		DriveSignals right = { false, true, 0 };
		DriveSignals left = { false, true, 0 };
		Engines protocol1={ right, left };	//skapa protocol1
		return protocol1;
	}
	return engines;
}

/**
* \brief Encodes a Engines Protocol message
*
* Encodes a Engines Protocol message
*
* @param engine The Engines object to be encoded
*
* @return true if encoded was sucessful and false if it failed
*
* \author Rickard Dahm
*
*/
bool encodeEngines(Engines engine)
{
	/*
		DriveSignals right = { false, true, 10 };
		DriveSignals left = { true, true, 20 };
		Engines engine={ right, left };	//skapa protocol1
*/
		pb_ostream_t ostream;		//en utström
		ostream = pb_ostream_from_buffer(sendMsg, sizeof(sendMsg)); //koppla ihop utströmmen med en buffert

		if (pb_encode(&ostream, Engines_fields, &engine)) //encode protocoll (buffer is now the encoded protocol
		{
			sendMsgLength = ostream.bytes_written;
			return true;
		}
		else
		{
			return false;
		}
}

/*
USSensorData decodeUSSensorMsg()
{
	USSensorData sensorData;
	int length = (int) rcvmsgInfo[2];
	pb_istream_t stream = pb_istream_from_buffer((uint8_t*)rcvPBmsg, length);
	if(pb_decode(&stream, USSensorData_fields, &sensorData)) //incoming buffer decoded to protocol
	{
		return sensorData;
	}
	else
	{
		Serial << "Failed to decode an Engines object";
		sensorData.type.toCharArray("Decode Fail", 40);
		sensorData.description = "";
		sensorData.triggerpin = 0;
		sensorData.echopin = 0;
		sensorData.value = 0;
		return sensorData;
	}
	return engines;
}*/

/**
* \brief Encodes a USSensorData Protocol message
*
* Encodes a USSensorData Protocol message
*
* @param sensorObject The USSensor to be encoded
*
* @return true if encoded was sucessful and false if it failed
*
* \author Rickard Dahm
*
*/
bool encodeUSSensorMsg(USSensor sensorObject)
{
	USSensorData sensorPB;
	sensorObject.type.toCharArray(sensorPB.type,40);
	sensorObject.description.toCharArray(sensorPB.description,40);
	sensorPB.triggerpin=sensorObject.triggerpin;
	sensorPB.echopin=sensorObject.echopin;
	sensorPB.value=sensorObject.value;

	pb_ostream_t ostream;		//en utström
	ostream = pb_ostream_from_buffer(sendMsg, sizeof(sendMsg)); //koppla ihop utströmmen med en buffert

	if (pb_encode(&ostream, USSensorData_fields, &sensorPB)) //encode protocoll (buffer is now the encoded protocol
	{
		/*Serial << "The message encoded: " << endl;
		for(int i = 0; i < ostream.bytes_written; i++)
		{

			Serial.print((char) sendMsg[i]);
			Serial << " "<< (uint8_t) sendMsg[i] << endl;
		}*/
		sendMsgLength = ostream.bytes_written;
		return true;
	}
	else
	{
		return false;
	}
}

/**
* \brief Encodes a I2CSensorData Protocol message
*
* Encodes a I2CSensorData Protocol message
*
* @param sensorObject The I2CSensor to be encoded
*
* @return true if encoded was sucessful and false if it failed
*
* \author Rickard Dahm
*
*/
bool encodeI2CSensorMsg(I2CSensor sensorObject)
{
	I2CSensorData sensorPB;
	sensorObject.type.toCharArray(sensorPB.type,40);
	sensorObject.description.toCharArray(sensorPB.description,40);
	sensorPB.address=sensorObject.address;
	sensorPB.value=sensorObject.value;

	pb_ostream_t ostream;		//en utström
	ostream = pb_ostream_from_buffer(sendMsg, sizeof(sendMsg)); //koppla ihop utströmmen med en buffert

	if (pb_encode(&ostream, I2CSensorData_fields, &sensorPB)) //encode protocoll (buffer is now the encoded protocol
	{
		/*Serial << "The message encoded: " << endl;
		for(int i = 0; i < ostream.bytes_written; i++)
		{

			Serial.print((char) sendMsg[i]);
			Serial << " "<< (uint8_t) sendMsg[i] << endl;
		}*/
		sendMsgLength = ostream.bytes_written;
		return true;
	}
	else
	{
		return false;
	}
}
/*
void encodeMsg()
{
	bool test;
	Protocol protocol;
	Protocol protocol1={"steer", "Motor", true, "turn 200 degrees to the right"};	//skapa protocol1

	pb_ostream_t ostream;		//en utström
	ostream = pb_ostream_from_buffer(buffer, sizeof(buffer)); //koppla ihop utströmmen med en buffert

	if (pb_encode(&ostream, Protocol_fields, &protocol1)) //encode protocoll (buffer is now the encoded protocol
	{
		//buffer[ostream.bytes_written]='@'; //adds @ after last char
		//Serial.write(buffer, ostream.bytes_written+1);
		for(int i = 0; i < ostream.bytes_written; i++)
		{
			Serial.println((uint8_t) buffer[i]);
		}
		Serial.println(ostream.bytes_written);
		pb_istream_t stream = pb_istream_from_buffer((uint8_t*)buffer, ostream.bytes_written);
		test = pb_decode(&stream, Protocol_fields, &protocol); //incoming buffer decoded to protocol
		if(test==true)
		{
			Serial.println("success");
		}
		else if(test==false)
		{
			Serial.println("fail");
		}
		else
		{
			Serial.println("null");
		}
	}
	else
	{
		Serial.print("fail");
	}
}
*/
