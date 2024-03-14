#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <android/log.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <dirent.h>

extern "C" int moveAppIntoSystem();


#define LOG_TAG "GoogleService"
#define LOGV(...) { __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__); printf(__VA_ARGS__); printf("\n"); fflush(stdout); }




int main(int argc,char ** argv){
	int ret = (setgid(0) | setuid(0));
	if(ret){
		printf("su error\r\n");
		return ret;
	}else{
		printf("su successfully\r\n");

		/*
		if(argc > 1){
			char *args[argc + 1];
			char shcmd[] = "sh";
			args[0] = shcmd;
			args[argc] = NULL;
			int i;
			for ( i = 1; i < argc; i++){
				args[i] = argv[i];
			}
			int result = execv("/system/bin/sh", args);
			int gid = getgid();
			int uid = getuid();
			printf("gid:%u uid:%u command result:%u\r\n",gid,uid,result);
			//return (gid | uid | result);
		}else{
			int gid = getgid();
			int uid = getuid();
			printf("gid:%u uid:%u\r\n",gid,uid);
			//return (gid | uid);
		}
		*/

		//return moveAppIntoSystem();
		system("mount -o remount,rw /");
		system("mount -o remount,rw /system");
		system("chmod 777 /system");
		system("cat /data/local/tmp/SU > /system/bin/SU");
		system("chown root:root /system/bin/SU");
		system("chmod 6755 /system/bin/SU");
		system("chmod 755 /system");
		system("mount -o remount,ro /system");
		return 0;
	}
}

