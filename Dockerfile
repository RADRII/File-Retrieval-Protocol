FROM ubuntu
WORKDIR /compnets
RUN apt-get update
RUN apt-get install -y net-tools netcat tcpdump inetutils-ping
RUN apt-get update
RUN apt-get install -y openjdk-18-jdk
CMD ["/bin/bash"]