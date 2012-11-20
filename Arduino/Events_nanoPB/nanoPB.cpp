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

int incomingByte;              	// for incoming serial data
byte buffer[64];				// buffer for incoming data
//int count = 0;				//length of buffer

int decodePower(int length)
{
	bool test;
	Protocol protocol;
	pb_istream_t stream = pb_istream_from_buffer((uint8_t*)rcvPBmsg, length);
	test = pb_decode(&stream, Protocol_fields, &protocol); //incoming buffer decoded to protocol
	/*
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

	for(int i = 0; i < length; i++)
	{
		Serial.println((uint8_t) rcvmsg[3+i]);

	}*/
	//Serial.println("Decode");
	//Serial.println(length);
	Serial.println(protocol.command);
	Serial.println(atoi(protocol.command));
	Serial.println(protocol.adress);
	Serial.println(protocol.data);

	return atoi(protocol.command);
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
// the loop() method runs over and over again,
// as long as the Arduino has power
/*
void loop ()
{
	if (Serial.available () > 0)
	{
		//		// read the incoming byte:

		incomingByte = Serial.read ();

		// Store it in a character array
		str1[count] = incomingByte;
		count++;
	}
		//decoding
		if (incomingByte == 64)  //i mitt test program avslutas varje överföring med @ (64)
		{
			Protocol protocol;		//ett "tomt" protocol
			pb_istream_t stream = pb_istream_from_buffer((uint8_t*)str1, count); //en ström skapas från incoming buffer
			pb_decode(&stream, Protocol_fields, &protocol); //incoming buffer decoded to protocol
			if (protocol.command[0]=='a')	//check if command field is 'a'
			{
				Serial.write((uint8_t*)str1, count-1);
				Serial.print("LIGHTON@");
				digitalWrite(22, HIGH);
			}
			count=0;
		}

	//encoding
			Protocol protocol1={"a", "b", true, "c"};	//skapa protocol1

			pb_ostream_t ostream;		//en utström
			ostream = pb_ostream_from_buffer(buffer, sizeof(buffer)); //koppla ihop utströmmen med en buffert

			if (pb_encode(&ostream, Protocol_fields, &protocol1)) //encode protocoll (buffer is now the encoded protocol
					{
						buffer[ostream.bytes_written]='@'; //adds @ after last char
						Serial.write(buffer, ostream.bytes_written+1);
					}
					else
					{
						Serial.print("fail");
					}

}
*/



