#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <dlfcn.h>
#include <fcntl.h>
#include <sys/wait.h>
#include <android/log.h>


#define LOG_TAG "run-as"


#define DEBUG
#ifdef DEBUG

#define LOGV(...) { __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__); printf(__VA_ARGS__); printf("\n"); fflush(stdout); }
#elif PRINT
#define LOGV(...) { __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__); printf(__VA_ARGS__); printf("\n"); fflush(stdout); }
#else
#define LOGV(...)
#endif


char __aeabi_unwind_cpp_pr0[0];




int main(int argc, const char **argv)
{
	int retcode = -1;
	LOGV("%s uid:%d euid:%d", argv[0], getuid(),geteuid());

	if (setresgid(0, 0, 0) || setresuid(0, 0, 0)) {
		LOGV("setresgid/setresuid failed");
		exit(retcode);
	}

	int puid = getuid();
	int peuid = geteuid();
	LOGV("after setresgid/setresuid uid:%d euid:%d", puid,peuid);
	if(puid == 0 && peuid == 0){
		retcode = 0;
	}

	const char * error = dlerror();
#ifdef __aarch64__
	void * selinux = dlopen("/system/lib64/libselinux.so", RTLD_LAZY);
#else
	void * selinux = dlopen("/system/lib/libselinux.so", RTLD_LAZY);
#endif
	if (selinux) {
		typedef int getcon_t(char ** con);
		typedef int setcon_t(const char* con);

		void * getcon = dlsym(selinux, "getcon");
		error = dlerror();
		if (error) {
			LOGV("dlsym getcon error %s", error);

		} else {
			getcon_t * getcon_p = (getcon_t*)getcon;
			char * secontext;
			int ret = (*getcon_p)(&secontext);
			LOGV("getcon result:%d context:%s", ret, secontext);
			void * setcon = dlsym(selinux, "setcon");
			const char *error = dlerror();
			if (error) {
				LOGV("dlsym setcon error %s", error);
			} else {
				setcon_t * setcon_p = (setcon_t*)setcon;
				ret = (*setcon_p)("u:r:shell:s0");
				ret = (*getcon_p)(&secontext);
				LOGV("setcon result:%d %s", ret, secontext);
				retcode = 0;
			}
		}
		dlclose(selinux);
	} else {
		LOGV("not found libselinux.so");
	}

	//system("/system/bin/sh -i");

	if(argc >= 2){
		/*
		pid_t pid = fork();
		if(pid > 0){
			system("/system/bin/sh -i");
			//waitpid(pid,0,0);
			printf("sh error\r\n");
			return -1;
		}else if(pid == 0){
			//sleep(1);
			char szcmd[256];
			sprintf(szcmd,"%s",argv[1]);
			printf("father process run:%s\r\n",szcmd);
			system(szcmd);
		}else{
			LOGV("fork error");
			exit(-1);
		}
		*/
		char szcmd[256];
		sprintf(szcmd,"%s",argv[1]);
		printf("father process run:%s\r\n",szcmd);
		retcode = system(szcmd);
	}else{
		LOGV("param too few,direct into su mode");
		system("/system/bin/sh -i");
	}

	return retcode;
}
