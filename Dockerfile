FROM mozilla/sbt

ADD src /build/src
ADD build.sbt /build/build.sbt
ADD project /build/project
WORKDIR /build

RUN sbt compile
CMD sbt run
