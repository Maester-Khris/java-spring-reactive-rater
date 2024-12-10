## Project Setup
1. Live Data Update:
The project implements live data updates using asynchronous client form submission and response with Axios, along with server-sent events through reactive programming with Reactor Sinks API and MonoFrom runnable to implement a web thread producer.
2. Multi-Threaded Environment:
The project utilizes a multi-threaded environment with thread pool configuration and scheduled thread-safe execution using the CopyOnWriteArrayList API and async annotation to simulate background data producer.
3. RESTful API Design:
The project features a RESTful API design with API documentation using Swagger UI. <br>
Read the entire project description at this address [Reactive Skillrater](nkprod.render.com/reactive-skill-rater)


## Installation and Execution
The application has been entirely package to be used as a self sufficient container immediately available for use. Otherwise you can still clone this repository and use it in your preffered IDE.
Instruction to use the docker container 
- Install docker on your host system
- Retrieve the application docker image available on dockerhub at this address [Spring Reactive Skillrater](dockerhub.com/mrkhris/reactive-skillrater) <br>
    `docker pull reactive-skillrater:latest`
    
- setup a MySQL database (local or distant)
- run the docker container and pass your database connexion parameter as variable <br>
    ```docker run --name my-springboot-app -p8080:8080 reactive-skillrater:latest  --dbhost --dbuser --dbpass --dbname```
    


## Future Improvements
We are open to suggestion and contributions.