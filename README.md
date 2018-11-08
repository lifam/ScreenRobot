# ScreenRobot
This is a Java program used to handling screen issues. You can use it to capture the screen and save it as a screen-shot, or capture many screen in a fix rate and then make a video as a screen record or transport to the other devices that runs the same program.

---------------------------

## screen-shot
This program use 'java.awt.Robot' to capture the screen, you can directly get a full size screen-shot or get a specific size screen-shot after you choose a specific size and position mannually.

After you get the screen-shot, you can resize it as you like(actually, this feature is not impletemented very well). And you can firstly choose a directory and select a file-name, and then save the screen-shot in multiple formats, including jpg, png, gif, etc.

## screen-record
To generate the screen-record, this program use 'java.awt.Robot' to continually(at the specific fix rate) capture the screen-shot in background, and show each screen-shot after they are successfully captured. If you want to save the screen-record into a video file, then you should firstly select a proper directory and a file-name to store it before you click the 'start' button. However, because I didn't find any other better tools, I chose the 'JMF' to convert those captured screen-shots into screen-record videos. The 'JMF' is an old framework, and it may cause some annoying problems. If you can not run this program successfully, then maybe you should install the '[JMF](https://www.oracle.com/technetwork/java/javase/download-142937.html)' into your device and then have another try. I'm thinking about using another tools to do the jobs that 'JMF' does, but for now I actually don't have that much time.

Because the raw video bit rate is somehow so high/big, I have to first compress the raw-screen-shots and then convert then into screen-record.

## screen-transport
To transport the screen, this program firstly use 'java.awt.Robot' to continually(at the specific fix rate) capture the screen-shot in background, and then send those screen-shots using multicast(if it's the server) or receive the raw bytes data and then reconstruct/display screen-shots from those bytes data(if it's the client).

---------------------------

You are free and welcomed to use the softwares I built and the code I wrote. And if you have any questions, just let me know and I will try my best to answer you questions. Here is my email address: 1825899616@qq.com.
