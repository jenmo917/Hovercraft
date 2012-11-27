
//#include "EventHandlers.h"
//#include "EventManagers.h"
//#include "I2C.h"
//#include "Drive.h"
//#include "sensors.h"
//#include "usb.h"
#include <UsbHost.h>
#include <AndroidAccessory.h>

#define LEDE 13

AndroidAccessory accE("Manufacturer", "Model", "Description","Version", "URI", "Serial");
int buttonState = 0;

// the event queue
//EventQueue q;

// the event dispatcher
//EventDispatcher disp(&q);

// program setup
void setup() {
    Serial.begin(115200);
    //pinSetup();
    //sensorSetup();
    //I2CSetup();
    pinMode(LEDE, OUTPUT);
    accE.powerOn();
    //USBsetup();
    //driveSetup();
    //disp.addEventListener(Events::EV_TIME_1000, timeHandler1000);
    //disp.addEventListener(Events::EV_TIME_1000, usbSoftwareTestHandler);
    //disp.addEventListener(Events::EV_TIME_1000, IRSensorHandler);
    //disp.addEventListener(Events::EV_TIME_1000, I2CSensorDataHandler);
    //disp.addEventListener(Events::EV_TIME_500, timeHandler500);
    //disp.addEventListener(Events::EV_TIME_100, timeHandler100);
    //disp.addEventListener(Events::EV_ANALOG0, analogHandler);
    //disp.addEventListener(Events::EV_SERIAL, USBReadHandler);
}

// loop
void loop() {
    // call the event generating functions
    //timeManager(&q);
    //USBReadManager(&q);
    //analogManager(&q);

	// get events from the queue and call the
	// registered function(s)
	//disp.run();

	byte data[2];
	if (accE.isConnected()) {
		int len = accE.read(data, sizeof(data), 1);
		if (len > 0) {
			if (data[0] == 0x1) {
				digitalWrite(LEDE, data[1] ? HIGH : LOW);
				Serial.print("Toggle LED\r\n");
				data[0] = 0x2;
				accE.write(data, 2);
			}
		}
	}
	delay(10);
}

int main(void)
{
  /* Must call init for arduino to work properly */

	init();
	setup();
	//encodeMsg();
	for (;;)
	{
	  loop();
	}
}
