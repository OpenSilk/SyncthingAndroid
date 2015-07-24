FROM buildpack-deps:jessie

ENV NDK_VERSION 10e

#Download ndk and install toolchain
RUN curl -L -o /tmp/ndk.bin https://dl.google.com/android/ndk/android-ndk-r${NDK_VERSION}-linux-x86_64.bin
RUN mkdir -p /opt/android/ndk && \
	cd /opt/android/ndk && \
	chmod a+x /tmp/ndk.bin && \
	/tmp/ndk.bin && \
	sleep 1 && \
	rm /tmp/ndk.bin
RUN bash /opt/android/ndk/android-ndk-r${NDK_VERSION}/build/tools/make-standalone-toolchain.sh \
		--platform=android-16 --arch=arm \
		--install-dir=/opt/android/ndk/toolchain-arm && \
	rm -r /tmp/ndk-* /opt/android/ndk/android-ndk-r${NDK_VERSION}

ENV GO_VERSION 1.4.2

#Download go
RUN curl -L -o /tmp/golang.tar.gz https://storage.googleapis.com/golang/go${GO_VERSION}.src.tar.gz
RUN tar xaf /tmp/golang.tar.gz -C /opt/ && rm /tmp/golang.tar.gz

ENV TOOLCHAIN_ROOT /opt/android/ndk/toolchain-arm

#Build go for android-arm
RUN cd /opt/go/src && \
	CC_FOR_TARGET=${TOOLCHAIN_ROOT}/bin/arm-linux-androideabi-gcc \
	CXX_FOR_TARGET=${TOOLCHAIN_ROOT}/bin/arm-linux-androideabi-g++ \
	CGO_ENABLED=1 \
	GOOS=android \
	GOARCH=arm \
	GOARM=7 \
	bash make.bash

ENV GOROOT /opt/go
ENV PATH ${GOROOT}/bin:${PATH}

#Bind mount syncthing repo here
VOLUME /opt/workspace
WORKDIR /opt/workspace

ENV DOCKER_BUILD true

COPY do-build.sh /usr/local/bin/
RUN chmod 755 /usr/local/bin/do-build.sh
ENTRYPOINT ["/usr/local/bin/do-build.sh"]

