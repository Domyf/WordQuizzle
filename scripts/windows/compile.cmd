:: To compile the server (Windows)
javac -d ./bin/WordQuizzle -cp "lib/*;bin/WordQuizzle/" src/com/domenico/shared/*.java src/com/domenico/communication/*.java src/com/domenico/server/*.java src/com/domenico/server/network/*.java src/com/domenico/server/usersmanagement/*.java && xcopy /q /y .\src\resources .\bin\WordQuizzle\resources\

:: To compile the client
javac -d ./bin/WordQuizzle -cp "lib/*;bin/WordQuizzle/" src/com/domenico/shared/*.java src/com/domenico/communication/*.java src/com/domenico/client/*.java
