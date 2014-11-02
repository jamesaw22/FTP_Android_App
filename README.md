FTP_Android_App
===============

Android App for From The Pavilion

to run, serve data.json (from not-in-build) locally (I'm using apache) and repoint the url in /myapplication/Scoreboard.java

update to the data being served - now a node.js script, run in the command line with: 
`$ node data.js`

that serves it on port 9899 (which you can change) on the localhost of your machine (which you will have to update in Scoreboard.java)
