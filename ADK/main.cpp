#include <Arduino.h>
#include <Max3421e.h>
#include <UsbHost.h>
#include <AndroidAccessory.h>

#define ARRAY_SIZE 12

AndroidAccessory acc("Manufacturer", "Model", "Description","Version", "URI", "Serial");
char hello[ARRAY_SIZE] = {'h','e','l','l','o',' ','w','o','r','l','d','!'};

void setup()
{
	Serial.begin(115200);
	acc.begin();
}

void loop()
{
	if (acc.isConnected())
	{
		for(int x = 0; x < ARRAY_SIZE; x++)
		{
			Serial.print(hello[x]);
			delay(250);
		}
		Serial.println();
		delay(250);
	}
}

int main(void) {

  /* Must call init for arduino to work properly */
	init();
	setup();

	for (;;)
	{
	  loop();
	}
}
