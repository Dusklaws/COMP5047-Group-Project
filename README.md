# COMP-5047-Group-Project
A University Project for Pervasive Computing. Basically an Android Application which connects to a Micro:bit via Bluetooth low energy and 
Raspberry Pi via MQTT. 

The purpose of this is to design a device which will help user fix their bad sitting habit or posture. To do this the Micro:bit which will
be attached to a cap (via a 3D printed clip) will read its built in accelerometer value and transmit it to an App. Once the app recieve a 
bad value the user will be alerted to fix their bad habbit. The Raspberry Pi will be equipped with a force sensor which will be placed inside
the back rest of the user to make sure that their back is straight. The Raspberry is connected through MQTT and similarly if the app recieve
a bad value it will alert the user to fix their back.

[Micro:bit Script](https://makecode.microbit.org/_VKjEPJ5siDc8)
