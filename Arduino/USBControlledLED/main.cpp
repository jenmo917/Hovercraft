#include <Max3421e.h>
#include <UsbHost.h>
#include <AndroidAccessory.h>

#define COMMAND_LED 0x2
#define TARGET_PIN_2 0x2
#define VALUE_ON 0x1
#define VALUE_OFF 0x0
#define PIN 2

AndroidAccessory acc("Manufacturer", "Model", "Description", "Version", "URI",
		"Serial");

byte rcvmsg[3];

void setup() {
	Serial.begin(19200);
	acc.begin();
	pinMode(PIN, OUTPUT);
}

void loop()
{
	if (acc.isConnected())
	{
//read the received data into the byte array
		int len = acc.read();
		if (len > 0) {
			if (rcvmsg[0] == COMMAND_LED) {
				if (rcvmsg[1] == TARGET_PIN_2) {
//get the switch state
					byte value = rcvmsg[2];
//set output pin to according state
					if (value == VALUE_ON) {
						digitalWrite(PIN, HIGH);
					} else if (value == VALUE_OFF) {
						digitalWrite(PIN, LOW);
					}
				}
			}
		}
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
