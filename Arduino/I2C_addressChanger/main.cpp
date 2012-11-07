/* Instructions:
 * 1. Change the compassAddress to the address of the sensor.
 * 2. Change newCompassAddress to the wanted address.
 * 3. Build and upload code to Arduino.
 * 4. Open Arduino and use the serial monitor. Push the reset button on the Arduino.
 * 		The number is the sensors address.
 * 		-1 means the sensor cant be found on that address.
 * 		(It should be -1 now, and change to the wanted address after step 5)
 * 5. Reset sensor. (remove the VCC)
 * 6. If the number shown in Arduino has changed to the wanted address the
 * 		reprogramming is finished.
 */

#include <Arduino.h>
#include <Wire.h>
#include <UsbHost.h>
#include <AndroidAccessory.h>

int compassAddress = 0x41 >> 1; // From datasheet compass address is 0x42
int newCompassAddress = 0x42;
// shift the address 1 bit right, the Wire library only needs the 7
// most significant bits for the address
int reading = 0;

void setup()
{
	Wire.begin();       // join i2c bus (address optional for master)
	Serial.begin(9600); // start serial communication at 9600bps
}
void loop()
{
	// step 1: instruct sensor to read echoes

	  Wire.beginTransmission(newCompassAddress >> 1);  // transmit to device
	  Wire.write('r');        // command sensor to measure angle
	  Wire.write(0);
	  Wire.endTransmission(); // stop transmitting

	  // step 2: wait for readings to happen
	  delay(10); // datasheet suggests at least 6000 microseconds

	  // step 3: request reading from sensor
	  Wire.requestFrom(newCompassAddress >> 1, 1); // request 2 bytes from slave device #33

	  // step 4: receive reading from sensor
	  reading = Wire.read();
	  Serial.println(reading); // print the reading

	  delay(500); // wait for half a second
  Serial.println("");
}

int main(void) {

  /* Must call init for arduino to work properly */
	init();
	setup();

	Wire.beginTransmission(compassAddress);  // transmit to device
	// the address specified in the datasheet is 66 (0x42)
	// but i2c adressing uses the high 7 bits so it's 33
	Wire.write('w');        // command sensor to measure angle
	Wire.write(0);
	Wire.write(newCompassAddress);
	Wire.endTransmission(); // stop transmitting

	delay(500);

	for (;;)
	{
	  loop();
	}
}
