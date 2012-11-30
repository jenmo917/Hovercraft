#include "EventHandlers.h"
#include "EventManagers.h"
#include "I2C.h"
#include "Drive.h"
#include "sensors.h"
#include "usb.h"
#include <UsbHost.h>
#include <AndroidAccessory.h>
#include "nanoPB.h"
#include "Streaming.h"

// the event queue
EventQueue q;

// the event dispatcher
EventDispatcher disp(&q);
// program setup
void setup()
{
	Serial.begin(115200);
	Serial.print("\r\nADK Started\r\n");

	pinSetup();
	sensorSetup();
	I2CSetup();
	USBsetup();
	driveSetup();
	//disp.addEventListener(Events::EV_TIME_1000, timeHandler1000);
	disp.addEventListener(Events::EV_US_SENSOR_REQ, USSensorHandler);
	disp.addEventListener(Events::EV_I2C_SENSOR_REQ, I2CSensorDataHandler);
	//disp.addEventListener(Events::EV_TIME_1000, I2CSensorDataHandler);
	disp.addEventListener(Events::EV_US_SENSOR_FINISHED, USBSendUSSensorDataHandler);
	disp.addEventListener(Events::EV_I2C_SENSOR_FINISHED, USBSendI2CSensorDataHandler);
	disp.addEventListener(Events::EV_TIME_500, blinkyHandler);
	//disp.addEventListener(Events::EV_TIME_100, timeHandler100);
	//disp.addEventListener(Events::EV_ANALOG0, analogHandler);
	disp.addEventListener(Events::EV_SERIAL, USBReadHandler);
}

// loop
void loop()
{
	static bool ErrorMsgSent = false;
	// call the event generating functions
	//if (acc.isConnected())
	//{
		ErrorMsgSent = false;
		timeManager(&q);
		USBReadManager(&q);
		//analogManager(&q);

		// get events from the queue and call the
		// registered function(s)
		disp.run();
	/*}
	else if(ErrorMsgSent != true)
	{
		Serial << "No USB connection to an Android phone was found" << endl;
		ErrorMsgSent = true;
		DriveSignals signal;
		signal.enable = false;
		signal.forward = true;
		signal.power = 0;
		rightMotorControl(&signal);
		leftMotorControl(&signal);
	}*/
}

int main(void)
{
    // Must call init for arduino to work properly
	init();
	setup();
	//encodeEngines();
	//encodeMsg();
	for (;;)
	{
	  loop();
	}
}
