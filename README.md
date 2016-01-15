# PINSEDL
PINS is a hardware driven app designed to query a http server and retrieve data.
It then displays and analyzes that data on a graph as it continues to do polling in livetime.
It is designed to search for four data points being posted to a http page in the form 0 0 0 0#

The arduino being paired with it for testing is a Mega 2560 with a CC3000 wifi shield.
It should later be implemented with bluetooth as well for additional functionality and versitility.

DEVELOPED FOR ENGINEERING DEVELOPMENT LABORATORIES PINS PRODUCT FOR CONSUMER FACING SALES

Update: The app now works entirely based around bluetooth. It reads a serial transmission in the format
shown above and graphs it. Bluetooth was shown to maintain a steadier connection and a better fit for the 
user's needs.
The hardware added is an EZ Link Bluetooth Shield V1.3 writing at baud 9600.
