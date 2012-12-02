#include <Arduino.h>
#include <Max3421e.h>
#include <UsbHost.h>
#include <AndroidAccessory.h>

#define ARRAY_SIZE 25

// COMMANDS
#define COMMAND_TEXT 0xF
#define BLINKY_ON 0xA
#define BLINKY_OFF 0xB

#define TARGET_DEFAULT 0xF

AndroidAccessory acc("Manufacturer", "Model", "Description","Version", "URI", "Serial");

char hello[ARRAY_SIZE] = {'H','e','l','l','o',' ','k','k','u','k','k',' ', 'f', 'r', 'o', 'm', ' ','E', 'm', 'i', 'l', 'i', 'n', 'a', '!'};
byte rcvmsg[255];
byte sntmsg[3 + ARRAY_SIZE];

bool blinkyFlag = false;

void startBlinky()
{
	blinkyFlag = true;
}

void stopBlinky()
{
	blinkyFlag = false;
	digitalWrite(13, 0);
}

void setup()
{
	Serial.begin(115200);
	acc.begin();
}

void loop() {
	if (acc.isConnected())
	{
		byte *msgPtr = &rcvmsg[0];
		bool bufferFlag = false;

		while(acc.peek() != -1)
		{
			*msgPtr++ = acc.read();
			bufferFlag = true;
		}

		if (bufferFlag)
		{
			if (rcvmsg[1] == TARGET_DEFAULT)
			{
				/*
				//get the textLength from the checksum byte
				byte textLength = rcvmsg[2];
				int textEndIndex = 3 + textLength;

				Serial.print("MESSAGE RECEIVED: ");
				//print each character to the serial output
				for(int x = 3; x < textEndIndex; x++)
				{
					Serial.print((char)rcvmsg[x]);
				}
				Serial.println();
				*/

				if (rcvmsg[0] == BLINKY_ON)
				{
					Serial.println("COMMAND: BLINKY_ON");
					startBlinky();
				}
				else if (rcvmsg[0] == BLINKY_OFF)
				{
					Serial.println("COMMAND: BLINKY_OFF");
					stopBlinky();
				}
			}

		}

		if(blinkyFlag == true)
		{
			digitalWrite(13, 0);   // set the LED on
			delay(500);                  // wait for a second
			digitalWrite(13, 1);    // set the LED off
			delay(500);
		}
		/*
		sntmsg[0] = COMMAND_TEXT;
		sntmsg[1] = TARGET_DEFAULT;
		sntmsg[2] = ARRAY_SIZE;

		for(int x = 0; x < ARRAY_SIZE; x++)
		{
			sntmsg[3 + x] = hello[x];
		}

		// int len = acc.write(sntmsg, 3 + ARRAY_SIZE);
		delay(250);
		*/
	}
}

int main(void) {

	/* Must call init for arduino to work properly */
	init();

	setup();

	while(true)
	{
	  loop();
	}
}
