# Job Monitor

This is a simple JavaFX application that monitors jobs running in an AWS EC2 instance and reports their statuses. There are 2 EC2 instances here:

- First one is a jump box which is connected from the application using SSH
- Second one is an App Server, which can only be connected from the Jump box, again using SSH.

There is a Custom defined Scheduler already running (not part of this application) on the App server that reads a Job file (Contains Job names, their dependencies, etc). The log files of the jobs are stored in a log directory.

This application executes a Thread in the background to monitor the jobs and updates their status using Colours:

- Green - Job Successfully completed
- Purple - Job failed with error
- Grey - Log file not yet available on the app server, most probably due to job has not completed.  

My main focus was to see how I could develop a JavaFX application rather than what the application could do !! I learnt many things during the development such as 

- Passing data between different Windows (Via Controllers),
- Changing application theme using JavaFX CSS
- Executing threads in the background and controlling the state of the UI on the application thread. 

****

### Screen shots

![](./images/monitor1.gif)



****

